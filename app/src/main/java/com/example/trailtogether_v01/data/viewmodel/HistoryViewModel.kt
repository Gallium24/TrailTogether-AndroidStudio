package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Event
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * HistoryViewModel.kt
 *
 * Gère l'affichage de l'historique des randonnées de l'utilisateur.
 *
 * Fonctionnalités:
 * - Chargement de l'historique complet des randonnées
 * - Affichage des statistiques (total randonnées, distance totale)
 * - Formatage des données pour affichage
 *
 * StateFlows exposés:
 * - history: Liste des randonnées effectuées (HistoryItem)
 * - isLoading: Indicateur de chargement
 *
 * Structure HistoryItem:
 * - trailName: Nom du sentier
 * - date: Date de la randonnée
 * - distance: Distance parcourue (formatée)
 * - duration: Durée de la randonnée
 *
 * Méthodes:
 * - loadHistory(userId): Charge l'historique d'un utilisateur
 * - addToHistory(trailId, userId): Ajoute une randonnée à l'historique
 * - getStats(userId): Récupère les statistiques globales
 *
 * Intégration profil:
 * - Met à jour trailsCompleted et totalDistance dans le profil utilisateur
 * - Calculs automatiques lors de l'ajout d'une randonnée
 *
 * Utilisation:
 * - Utilisé par HistoryScreen
 * - Affichage dans ProfileScreen (nombre total de randonnées)
 * - Peut être appelé depuis TrailDetailScreen pour ajouter à l'historique
 */

class HistoryViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()

    init {
        loadUserHistory()
    }

    private fun loadUserHistory() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getUserEvents(userId).collect { events ->
                _userEvents.value = events.sortedByDescending { it.date }
            }
        }
    }
}