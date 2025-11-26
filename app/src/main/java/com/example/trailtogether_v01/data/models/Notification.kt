package com.example.trailtogether_v01.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Notification.kt
 *
 * Modèle de données représentant une notification in-app.
 *
 * Structure:
 * - id: Identifiant unique de la notification
 * - recipientId: ID de l'utilisateur destinataire
 * - senderId: ID de l'utilisateur qui a déclenché la notification
 * - senderName: Nom de l'expéditeur
 * - type: Type de notification (LIKE ou COMMENT via NotificationType enum)
 * - postId: ID du post concerné
 * - content: Contenu/message de la notification
 * - read: Statut lu/non lu (mappé avec @PropertyName("read"))
 * - timestamp: Date et heure de création
 *
 * Important:
 * - Le champ isRead est mappé vers "read" dans Firestore avec @PropertyName
 * - Nécessite un index composite Firestore: recipientId (ASC), timestamp (DESC)
 *
 * Utilisation:
 * - Stocké dans collection Firestore 'notifications'
 * - Géré par NotificationViewModel
 * - Affiché dans NotificationsScreen avec badge sur BottomNavBar
 */

data class Notification(
    val id: String = "",
    val recipientId: String = "", // L'utilisateur qui reçoit la notification
    val senderId: String = "", // L'utilisateur qui a fait l'action
    val senderName: String = "",
    val type: NotificationType = NotificationType.LIKE,
    val postId: String = "",
    val content: String = "", // Texte de la notification
    @get:PropertyName("read")
    @set:PropertyName("read")
    var isRead: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
)

enum class NotificationType {
    LIKE,      // Quelqu'un a liké votre post
    COMMENT    // Quelqu'un a commenté votre post
}