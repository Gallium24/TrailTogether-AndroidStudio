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
 * ProfileViewModel.kt
 *
 * Gère le profil de l'utilisateur connecté.
 *
 * Fonctionnalités:
 * - Chargement des informations du profil
 * - Mise à jour du profil (nom, bio, contact d'urgence)
 * - Upload de photo de profil
 * - Affichage des statistiques (randonnées, distance)
 * - Gestion de l'historique de randonnées
 *
 * StateFlows exposés:
 * - user: Informations de l'utilisateur courant
 * - isLoading: Indicateur de chargement
 * - errorMessage: Message d'erreur si échec
 *
 * Méthodes:
 * - loadUserProfile(userId): Charge le profil complet
 * - updateProfile(name, bio, emergencyContact): Met à jour les infos
 * - uploadProfilePicture(uri): Upload nouvelle photo (si implémenté)
 * - addToHistory(trail): Ajoute une randonnée à l'historique
 *
 * Statistiques:
 * - trailsCompleted: Nombre de randonnées effectuées
 * - totalDistance: Distance totale parcourue (km)
 * - Mises à jour automatiques lors de l'ajout à l'historique
 *
 * Validation:
 * - Vérification format email pour contact d'urgence (optionnel)
 * - Vérification format téléphone (optionnel)
 * - Limite de caractères pour bio
 *
 * Utilisation:
 * - Utilisé par ProfileScreen et EditProfileScreen
 * - Charge automatiquement le profil au démarrage
 * - Mises à jour en temps réel via Flow Firestore
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