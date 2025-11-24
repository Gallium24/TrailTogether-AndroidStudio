package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import com.example.trailtogether_v01.data.repository.CompleteTrailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import android.util.Log

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private val firestoreRepository = FirestoreRepository()
    private val completeTrailRepository = CompleteTrailRepository()

    private val _firestoreTrails = MutableStateFlow<List<Trail>>(emptyList())
    private val _osmTrails = MutableStateFlow<List<Trail>>(emptyList())
    private val _allTrails = MutableStateFlow<List<Trail>>(emptyList())
    val allTrails: StateFlow<List<Trail>> = _allTrails.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredTrails = MutableStateFlow<List<Trail>>(emptyList())
    val filteredTrails: StateFlow<List<Trail>> = _filteredTrails.asStateFlow()

    private val _isLoadingMapTrails = MutableStateFlow(false)
    val isLoadingMapTrails: StateFlow<Boolean> = _isLoadingMapTrails.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedTrail = MutableStateFlow<Trail?>(null)
    val selectedTrail: StateFlow<Trail?> = _selectedTrail.asStateFlow()

    private val _trailStats = MutableStateFlow(TrailStats(0, 0.0, 0))
    val trailStats: StateFlow<TrailStats> = _trailStats.asStateFlow()

    // 🔥 SAUVEGARDE D'ÉTAT: Position et zoom de la carte
    private val _mapCenter = MutableStateFlow(
        savedStateHandle.get<Pair<Double, Double>>("mapCenter")?.let {
            GeoPoint(it.first, it.second)
        } ?: GeoPoint(48.4284, -71.0598)
    )
    val mapCenter: StateFlow<GeoPoint> = _mapCenter.asStateFlow()

    private val _mapZoom = MutableStateFlow(savedStateHandle.get<Double>("mapZoom") ?: 12.0)
    val mapZoom: StateFlow<Double> = _mapZoom.asStateFlow()

    // 🔥 SAUVEGARDE: État du switch tracés
    private val _showTrailPath = MutableStateFlow(savedStateHandle.get<Boolean>("showTrailPath") ?: true)
    val showTrailPath: StateFlow<Boolean> = _showTrailPath.asStateFlow()

    init {
        loadTrails()
    }

    private fun loadTrails() {
        viewModelScope.launch {
            launch {
                firestoreRepository.getTrails().collect { trailList ->
                    _firestoreTrails.value = trailList
                    updateAllTrails()
                }
            }

            combine(_allTrails, _selectedDifficulty, _searchQuery) { trails, diff, query ->
                var filtered = trails
                diff?.let { filtered = filtered.filter { it.difficulty == diff } }
                if (query.isNotEmpty()) {
                    filtered = filtered.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.location.contains(query, ignoreCase = true)
                    }
                }
                filtered
            }.collect { _filteredTrails.value = it }
        }
    }

    private fun updateAllTrails() {
        val firestoreTrails = _firestoreTrails.value
        val osmTrails = _osmTrails.value

        _allTrails.value = firestoreTrails + osmTrails
        updateStats()

        Log.d("HomeViewModel", "📊 Total: ${_allTrails.value.size} trails")
    }

    private fun updateStats() {
        val osmTrails = _osmTrails.value
        val totalTrails = osmTrails.size
        val totalDistance = osmTrails.sumOf { trail ->
            trail.distance.replace(" km", "").toDoubleOrNull() ?: 0.0
        }
        val totalElevation = osmTrails.sumOf { trail ->
            trail.elevation.split("m")[0].toIntOrNull() ?: 0
        }

        _trailStats.value = TrailStats(totalTrails, totalDistance, totalElevation)
    }

    fun loadMapTrails(center: GeoPoint, radiusMeters: Float = 5000f) {
        viewModelScope.launch {
            _isLoadingMapTrails.value = true
            _errorMessage.value = null

            try {
                val result = completeTrailRepository.getEnrichedTrails(center, radiusMeters)

                result.fold(
                    onSuccess = { trails ->
                        _osmTrails.value = trails
                        updateAllTrails()

                        if (trails.isEmpty()) {
                            _errorMessage.value = "Aucun sentier trouvé dans ce rayon."
                        } else {
                            Log.d("HomeViewModel", "${trails.size} sentiers")
                        }
                    },
                    onFailure = { error ->
                        Log.e("HomeViewModel", "Erreur", error)
                        _errorMessage.value = "Erreur: ${error.message}"
                    }
                )
            } finally {
                _isLoadingMapTrails.value = false
            }
        }
    }

    // 🔥 NOUVEAUTÉ: Sauvegarder la position de la carte
    fun updateMapPosition(center: GeoPoint, zoom: Double) {
        _mapCenter.value = center
        _mapZoom.value = zoom
        savedStateHandle["mapCenter"] = Pair(center.latitude, center.longitude)
        savedStateHandle["mapZoom"] = zoom
    }

    // 🔥 NOUVEAUTÉ: Toggle tracés
    fun toggleShowTrailPath() {
        _showTrailPath.value = !_showTrailPath.value
        savedStateHandle["showTrailPath"] = _showTrailPath.value
    }

    fun setShowTrailPath(show: Boolean) {
        _showTrailPath.value = show
        savedStateHandle["showTrailPath"] = show
    }

    fun refreshTrailsWithRadius(center: GeoPoint, radiusMeters: Float) {
        loadMapTrails(center, radiusMeters)
    }

    fun selectTrail(trailId: String) {
        _selectedTrail.value = _allTrails.value.find { it.id == trailId }
    }

    fun clearSelectedTrail() {
        _selectedTrail.value = null
    }

    fun setDifficultyFilter(difficulty: Difficulty?) {
        _selectedDifficulty.value = difficulty
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getTrailById(trailId: String): Trail? {
        return _allTrails.value.find { it.id == trailId }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

data class TrailStats(
    val totalTrails: Int,
    val totalDistance: Double,
    val totalElevation: Int
)