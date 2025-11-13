package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.User
import com.example.trailtogether_v01.data.repository.FirestoreRepository // Changé
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ProfileViewModel est responsable de la logique du profil de l'utilisateur.
 * Ses principales responsabilités sont :
 * - Charger les détails du profil de l'utilisateur depuis le FirestoreRepository.
 * - Mettre à jour les informations du profil de l'utilisateur dans le FirestoreRepository.
 * - Exposer l'état du profil (user) et du chargement (isLoading) que l'UI peut observer.
 */
class ProfileViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCurrentUser().collect { currentUser ->
                _user.value = currentUser
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(name: String, bio: String,emergencyContact: String, emergencyPhone: String) {
        viewModelScope.launch {
            _user.value?.let { current ->
                val updated = current.copy(
                    name = name,
                    bio = bio,
                    emergencyContact = emergencyContact,
                    emergencyPhone = emergencyPhone
                )
                repository.updateUser(updated)
            }
        }
    }
}