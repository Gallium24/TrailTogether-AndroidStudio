package com.example.trailtogether_v01.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

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