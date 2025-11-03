package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.trailtogether_v01.data.models.User
import com.example.trailtogether_v01.data.repository.MockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val repository = MockRepository()

    private val _user = MutableStateFlow(repository.getCurrentUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    fun updateEmergencyContact(name: String, phone: String) {
        _user.value = _user.value?.copy(
            emergencyContact = name,
            emergencyPhone = phone
        )
    }

    fun updateProfile(name: String, bio: String) {
        _user.value = _user.value?.copy(
            name = name,
            bio = bio
        )
    }
}