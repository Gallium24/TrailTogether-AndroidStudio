package com.example.trailtogether_v01.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Event.kt
 *
 * Modèle de données représentant un événement de randonnée en groupe.
 *
 * Structure:
 * - id: Identifiant unique de l'événement
 * - name: Nom de l'événement
 * - trailId: ID du sentier associé
 * - trailName: Nom du sentier pour affichage
 * - date: Date de l'événement (format String)
 * - time: Heure de départ
 * - organizerId: ID de l'organisateur
 * - organizerName: Nom de l'organisateur
 * - description: Description de l'événement
 * - maxParticipants: Nombre maximum de participants
 * - participants: Liste des IDs des participants inscrits
 * - difficulty: Niveau de difficulté du sentier
 *
 * Utilisation:
 * - Stocké dans collection Firestore 'events'
 * - Géré par CalendarViewModel et EventDetailViewModel
 * - Affiché dans CalendarScreen et EventDetailScreen
 */
@IgnoreExtraProperties
data class Event(
    val id: String = "",
    val trailId: String = "",
    val trailName: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val date: String = "",
    val time: String = "",
    val duration: String = "",
    val distance: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val participantsCount: Int = 0,
    val maxParticipants: Int = 20,
    val description: String = "",
    val meetingPoint: String = "",
    val status: String = "PLANNED", // "PLANNED", "STARTED", "SAFE", "COMPLETED"
    val alertScheduled: Boolean = false
)