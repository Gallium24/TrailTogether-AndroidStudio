package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            repository.getEventById(eventId).collect { loadedEvent ->
                _event.value = loadedEvent
                checkIfFuture(loadedEvent?.date)

                // Charger le sentier associé pour avoir la carte et les infos
                loadedEvent?.trailId?.let { trailId ->
                    repository.getTrailById(trailId).collect { trail ->
                        _associatedTrail.value = trail
                    }
                }
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

    fun deleteEvent(onSuccess: () -> Unit) {
        _event.value?.let { event ->
            viewModelScope.launch {
                repository.deleteEvent(event.id)
                onSuccess()
            }
        }
    }
}