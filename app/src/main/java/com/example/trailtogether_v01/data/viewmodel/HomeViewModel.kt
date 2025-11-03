package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.repository.MockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = MockRepository()

    private val _trails = MutableStateFlow<List<Trail>>(emptyList())
    val trails: StateFlow<List<Trail>> = _trails.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadTrails()
    }

    private fun loadTrails() {
        viewModelScope.launch {
            repository.getTrails().collect { trailList ->
                _trails.value = trailList
            }
        }
    }

    fun setDifficultyFilter(difficulty: Difficulty?) {
        _selectedDifficulty.value = difficulty
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredTrails(): List<Trail> {
        var filtered = _trails.value

        _selectedDifficulty.value?.let { diff ->
            filtered = filtered.filter { it.difficulty == diff }
        }

        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(_searchQuery.value, ignoreCase = true) ||
                        it.location.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        return filtered
    }
}