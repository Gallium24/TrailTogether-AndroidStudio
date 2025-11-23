package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Event
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    // Date sélectionnée (par défaut aujourd'hui)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Liste de tous les événements (pour afficher les points sur le calendrier)
    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    val allEvents: StateFlow<List<Event>> = _allEvents.asStateFlow()

    // Événements filtrés pour la date sélectionnée
    private val _eventsForSelectedDate = MutableStateFlow<List<Event>>(emptyList())
    val eventsForSelectedDate: StateFlow<List<Event>> = _eventsForSelectedDate.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            repository.getEvents().collect { events ->
                _allEvents.value = events
                filterEventsByDate(_selectedDate.value)
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        filterEventsByDate(date)
    }

    private fun filterEventsByDate(date: LocalDate) {
        // Formatage de la date pour correspondre au format stocké (ex: "2025-01-15")
        // Assurez-vous que le format ici correspond à celui utilisé lors de la création (voir CreateEventViewModel)
        val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        _eventsForSelectedDate.value = _allEvents.value.filter { event ->
            event.date == formattedDate
        }
    }
}