package com.example.trailtogether_v01.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * User.kt
 *
 * Modèle de données représentant un utilisateur de l'application.
 *
 * Structure:
 * - id: Identifiant unique (UID Firebase)
 * - name: Nom affiché de l'utilisateur
 * - email: Adresse email
 * - bio: Biographie/description personnelle
 * - emergencyContact: Numéro de téléphone d'urgence
 * - profilePictureUrl: URL de la photo de profil
 * - trailsCompleted: Nombre de randonnées effectuées
 * - totalDistance: Distance totale parcourue (km)
 *
 * Utilisation:
 * - Stocké dans collection Firestore 'users'
 * - Géré par ProfileViewModel, AuthViewModel
 * - Affiché dans ProfileScreen, EditProfileScreen, UserProfileScreen
 * - Utilisé pour l'authentification et les statistiques personnelles
 */

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val trailsCount: Int = 0
)