package com.example.trailtogether_v01.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.trailtogether_v01.data.models.Event
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventDetailViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _associatedTrail = MutableStateFlow<Trail?>(null)
    val associatedTrail: StateFlow<Trail?> = _associatedTrail.asStateFlow()

    private val _isEventInFuture = MutableStateFlow(false)
    val isEventInFuture: StateFlow<Boolean> = _isEventInFuture.asStateFlow()

    private val _currentUserEmergencyEmail = MutableStateFlow<String?>(null)
    val currentUserEmergencyEmail: StateFlow<String?> = _currentUserEmergencyEmail.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            repository.getEventById(eventId).collect { loadedEvent ->
                _event.value = loadedEvent
                checkIfFuture(loadedEvent?.date)

                // Charger le sentier associé pour la carte
                loadedEvent?.trailId?.let { trailId ->
                    repository.getTrailById(trailId).collect { trail ->
                        _associatedTrail.value = trail
                    }
                }
            }
        }

        // Charger l'email d'urgence de l'utilisateur courant
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                _currentUserEmergencyEmail.value = user?.emergencyContact
            }
        }
    }

    private fun checkIfFuture(dateString: String?) {
        if (dateString == null) return
        try {
            val eventDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            val today = LocalDate.now()
            _isEventInFuture.value = eventDate.isAfter(today) || eventDate.isEqual(today)
        } catch (e: Exception) {
            _isEventInFuture.value = false
        }
    }

    // Fonction pour démarrer la randonnée
    fun startHike() {
        _event.value?.let { event ->
            viewModelScope.launch {
                val startedEvent = event.copy(status = "STARTED")
                repository.createEvent(startedEvent) // Mise à jour Firestore
            }
        }
    }

    // Fonction pour signaler qu'on est rentré (ANNULE L'ALERTE)
    fun markAsSafe(context: Context) {
        _event.value?.let { event ->
            viewModelScope.launch {
                // 1. Mettre à jour Firestore
                val safeEvent = event.copy(status = "SAFE")
                repository.createEvent(safeEvent)

                // 2. Annuler le Worker Android (l'email ne partira pas)
                try {
                    WorkManager.getInstance(context).cancelAllWorkByTag(event.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Fonction pour supprimer/annuler l'événement
    fun deleteEvent(context: Context? = null, onSuccess: () -> Unit) {
        _event.value?.let { event ->
            viewModelScope.launch {
                // 1. Supprimer de Firestore
                repository.deleteEvent(event.id)

                // 2. Annuler le Worker si le contexte est fourni
                if (context != null) {
                    try {
                        WorkManager.getInstance(context).cancelAllWorkByTag(event.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                onSuccess()
            }
        }
    }

    // Surcharge pour garder la compatibilité si appelé sans contexte
    fun deleteEvent(onSuccess: () -> Unit) {
        deleteEvent(null, onSuccess)
    }
}