package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.repository.FirestoreRepository // Changé
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _trails = MutableStateFlow<List<Trail>>(emptyList())
    val trails: StateFlow<List<Trail>> = _trails.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredTrails = MutableStateFlow<List<Trail>>(emptyList())
    val filteredTrails: StateFlow<List<Trail>> = _filteredTrails.asStateFlow()

    init {
        loadTrails()
    }

    private fun loadTrails() {
        viewModelScope.launch {
            // Collecte les trails en continu
            launch {
                repository.getTrails().collect { trailList ->
                    _trails.value = trailList
                }
            }

            // Combine les filtres et met à jour la liste affichée
            combine(_trails, _selectedDifficulty, _searchQuery) { trails, diff, query ->
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


    fun setDifficultyFilter(difficulty: Difficulty?) {
        _selectedDifficulty.value = difficulty
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getTrailById(trailId: String): Trail? {
        // Cette fonction recherche dans la liste complète des randonnées
        // et retourne celle qui correspond à l'ID, ou null si non trouvée.
        return _trails.value.find { it.id == trailId }
    }

}