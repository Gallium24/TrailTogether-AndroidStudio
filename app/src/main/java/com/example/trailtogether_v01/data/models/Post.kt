package com.example.trailtogether_v01.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Post.kt
 *
 * Modèle de données représentant une publication dans le fil d'actualité.
 *
 * Structure:
 * - id: Identifiant unique du post
 * - authorId: ID de l'auteur
 * - authorName: Nom affiché de l'auteur
 * - content: Contenu textuel du post
 * - imageUrl: URL de l'image associée (optionnel)
 * - timestamp: Date et heure de publication
 * - likesCount: Nombre total de likes
 * - likedBy: Liste des IDs des utilisateurs ayant liké
 * - commentsCount: Nombre total de commentaires
 * - relatedTrailId: ID du sentier lié (optionnel)
 *
 * Utilisation:
 * - Stocké dans collection Firestore 'posts'
 * - Géré par FeedViewModel
 * - Affiché dans FeedScreen via PostCard component
 * - Peut être lié à un sentier pour contexte
 */

@IgnoreExtraProperties
data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val trailId: String = "",
    val trailName: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timestamp: Timestamp = Timestamp.now(),
    val isLiked: Boolean = false
)