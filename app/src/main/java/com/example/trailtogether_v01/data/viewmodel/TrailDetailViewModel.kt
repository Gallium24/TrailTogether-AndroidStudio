package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * TrailDetailViewModel.kt
 *
 * Gère l'affichage des détails d'un sentier.
 *
 * Fonctionnalités:
 * - Chargement des détails d'un sentier (Firestore ou preloadé)
 * - Affichage des informations complètes
 * - Gestion de la carte du sentier avec tracé
 * - Bouton de planification d'événement
 *
 * StateFlows exposés:
 * - trail: Sentier actuellement affiché
 * - isLoading: Indicateur de chargement
 *
 * Méthodes:
 * - setTrail(trail): Définit directement le sentier (depuis HomeScreen)
 * - fetchTrailById(trailId): Charge depuis Firestore si pas preloadé
 *
 * Modes de chargement:
 * 1. Preloaded: Sentier passé directement depuis HomeScreen (sentiers OSM)
 * 2. Fetch: Chargé depuis Firestore par ID (sentiers custom)
 *
 * Affichage:
 * - Toutes les informations du sentier
 * - Carte avec marqueur au point de départ
 * - Tracé du sentier si pathCoordinates disponible
 * - Statistiques: distance, durée, dénivelé, difficulté
 * - Bouton "Planifier un événement" → CreateEventScreen
 *
 * Gestion des coordonnées:
 * - Utilise getTrailStartPoint() et getPathAsGeoPoints()
 * - Compatibilité Firestore et données OSM
 *
 * Utilisation:
 * - Utilisé par TrailDetailScreen
 * - Navigation depuis HomeScreen (clic sur sentier)
 * - Navigation depuis EventCard (clic sur nom du sentier)
 */
class TrailDetailViewModel : ViewModel() {
    private val firestoreRepository = FirestoreRepository()

    private val _trail = MutableStateFlow<Trail?>(null)
    val trail: StateFlow<Trail?> = _trail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setTrail(trail: Trail) {
        Log.d("TrailDetailViewModel", "📥 setTrail appelé:")
        Log.d("TrailDetailViewModel", "  - ID: ${trail.id}")
        Log.d("TrailDetailViewModel", "  - Nom: ${trail.name}")
        Log.d("TrailDetailViewModel", "  - Source: ${trail.source}")
        Log.d("TrailDetailViewModel", "  - Distance: ${trail.distance}")
        Log.d("TrailDetailViewModel", "  - Durée: ${trail.duration}")

        _trail.value = trail
        _isLoading.value = false
    }

    fun fetchTrailById(trailId: String) {
        if (trailId.isBlank()) {
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                firestoreRepository.getTrailById(trailId).collect { fetchedTrail ->
                    if (fetchedTrail != null) {
                        _trail.value = fetchedTrail
                    } else {
                        _trail.value = null
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _trail.value = null
                _isLoading.value = false
            }
        }
    }
}