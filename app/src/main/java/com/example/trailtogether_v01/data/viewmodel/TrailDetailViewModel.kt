package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Trail
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrailDetailViewModel : ViewModel() {

    // Un StateFlow pour contenir la randonnée que nous allons récupérer.
    // Il peut être nul au début ou si la randonnée n'est pas trouvée.
    private val _trail = MutableStateFlow<Trail?>(null)
    val trail: StateFlow<Trail?> = _trail

    // Un état pour savoir si les données sont en cours de chargement.
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // La fonction qui va chercher les données dans Firestore.
    fun fetchTrailById(trailId: String) {
        // Si l'ID est vide, on ne fait rien.
        if (trailId.isBlank()) return

        _isLoading.value = true
        val db = Firebase.firestore

        // On va chercher le document dans la collection "trails" qui a le bon ID.
        db.collection("trails").document(trailId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Si le document existe, on le convertit en objet Trail.
                    val fetchedTrail = document.toObject<Trail>()
                    _trail.value = fetchedTrail
                } else {
                    // Le document n'a pas été trouvé.
                    _trail.value = null
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                // En cas d'erreur, on s'assure que le trail est nul.
                _trail.value = null
                _isLoading.value = false
            }
    }
}
