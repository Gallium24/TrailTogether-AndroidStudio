package com.example.trailtogether_v01.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Comment.kt
 *
 * Modèle de données représentant un commentaire sur un post du fil d'actualité.
 *
 * Structure:
 * - id: Identifiant unique du commentaire
 * - postId: ID du post associé
 * - authorId: ID de l'auteur du commentaire
 * - authorName: Nom affiché de l'auteur
 * - content: Contenu textuel du commentaire
 * - timestamp: Date et heure de publication
 *
 * Utilisation:
 * - Stocké dans Firestore sous-collection posts/{postId}/comments
 * - Utilisé par CommentsViewModel et CommentsScreen
 * - Créé lors de l'ajout d'un commentaire via CommentsScreen
 */

@IgnoreExtraProperties
data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()
)