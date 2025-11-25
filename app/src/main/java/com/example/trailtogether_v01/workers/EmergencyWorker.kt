package com.example.trailtogether_v01.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import com.example.trailtogether_v01.utils.EmailService
import kotlinx.coroutines.flow.firstOrNull

class EmergencyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val repository = FirestoreRepository()

    override suspend fun doWork(): Result {
        val eventId = inputData.getString("eventId") ?: return Result.failure()
        val recipientEmail = inputData.getString("recipientEmail") ?: return Result.failure()

        // 1. Vérifier dans Firestore si l'event existe toujours et n'est PAS "COMPLETED"
        val event = repository.getEventById(eventId).firstOrNull()

        // Si l'event a été supprimé ou marqué comme terminé/safe, on n'envoie rien.
        if (event == null || event.status == "COMPLETED" || event.status == "SAFE") {
            return Result.success()
        }

        // 2. Envoyer l'email
        EmailService.sendEmergencyEmail(
            recipientEmail = recipientEmail,
            userName = event.organizerName,
            trailName = event.trailName,
            date = "${event.date} à ${event.time}"
        )

        return Result.success()
    }
}