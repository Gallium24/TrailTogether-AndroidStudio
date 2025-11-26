package com.example.trailtogether_v01.ui.components

/**
 * EmergencyDialog.kt
 *
 * Dialogue pour l'envoi d'un message d'urgence avec position GPS.
 *
 * Fonctionnalités:
 * - Affichage d'un dialogue d'alerte
 * - Envoi de SMS d'urgence avec coordonnées GPS
 * - Envoi d'email d'urgence (backup)
 * - Utilisation de WorkManager pour tâches en arrière-plan
 *
 * Contenu du dialogue:
 * - Titre: "Alerte d'urgence"
 * - Message: Explication de l'envoi
 * - Boutons:
 *   - "Envoyer": Déclenche l'envoi d'urgence
 *   - "Annuler": Ferme le dialogue
 *
 * Message d'urgence:
 * - Format: "URGENCE RANDONNÉE - Position: [lat], [lon]"
 * - Lien Google Maps vers la position
 * - Nom de l'utilisateur
 *
 * Envoi:
 * - SMS via EmergencyWorker (WorkManager)
 * - Email via EmailService (backup)
 * - Contact récupéré depuis le profil utilisateur
 *
 * Permissions:
 * - SEND_SMS: Requis pour envoi SMS
 * - ACCESS_FINE_LOCATION: Pour position précise
 *
 * Utilisation:
 * - Déclenché depuis un bouton dans un écran (ex: HomeScreen, TrailDetailScreen)
 * - Paramètres: onDismiss, onSend, currentLocation, emergencyContact
 */

class EmergencyDialog {
}