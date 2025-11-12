package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.User
import com.example.trailtogether_v01.data.repository.FirestoreRepository // Changé
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCurrentUser().collect {
                _user.value = it
            }
        }
    }

    fun updateEmergencyContact(name: String, phone: String) {
        viewModelScope.launch {
            _user.value?.let { current ->
                val updated = current.copy(
                    emergencyContact = name,
                    emergencyPhone = phone
                )
                repository.updateUser(updated)
                // Snapshot mettra à jour _user
            }
        }
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch {
            _user.value?.let { current ->
                val updated = current.copy(
                    name = name,
                    bio = bio
                )
                repository.updateUser(updated)
            }
        }
    }
}