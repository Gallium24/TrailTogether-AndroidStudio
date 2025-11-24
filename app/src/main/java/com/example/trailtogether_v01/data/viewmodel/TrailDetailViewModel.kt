package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

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

        Log.d("TrailDetailViewModel", "✅ Trail défini avec succès")
    }

    fun fetchTrailById(trailId: String) {
        if (trailId.isBlank()) {
            Log.e("TrailDetailViewModel", "❌ trailId vide")
            return
        }

        Log.d("TrailDetailViewModel", "🔍 fetchTrailById pour: $trailId")
        _isLoading.value = true

        viewModelScope.launch {
            try {
                firestoreRepository.getTrailById(trailId).collect { fetchedTrail ->
                    if (fetchedTrail != null) {
                        Log.d("TrailDetailViewModel", "✅ Trail Firestore trouvé: ${fetchedTrail.name}")
                        _trail.value = fetchedTrail
                    } else {
                        Log.e("TrailDetailViewModel", "❌ Trail Firestore introuvable: $trailId")
                        _trail.value = null
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("TrailDetailViewModel", "❌ Erreur fetch trail: ${e.message}", e)
                _trail.value = null
                _isLoading.value = false
            }
        }
    }
}