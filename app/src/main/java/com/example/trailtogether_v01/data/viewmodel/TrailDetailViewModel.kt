package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.trailtogether_v01.data.models.Trail
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * TrailDetailViewModel est responsable de la logique des détails d'un itinéraire (TrailDetailScreen).
 * Ses principales responsabilités
 * - Charger les détails de l'itinéraire depuis Firestore.
 * - Exposer l'état de l'itinéraire (trail)
 * - Exposer l'état du chargement (isLoading)
 */
class TrailDetailViewModel : ViewModel() {

    private val _trail = MutableStateFlow<Trail?>(null)
    val trail: StateFlow<Trail?> = _trail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchTrailById(trailId: String) {
        if (trailId.isBlank()) return

        _isLoading.value = true
        val db = Firebase.firestore

        db.collection("trails").document(trailId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fetchedTrail = document.toObject<Trail>()
                    _trail.value = fetchedTrail
                } else {
                    _trail.value = null
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _trail.value = null
                _isLoading.value = false
            }
    }
}
