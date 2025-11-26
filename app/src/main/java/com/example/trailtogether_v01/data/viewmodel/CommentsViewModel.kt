package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Comment
import com.example.trailtogether_v01.data.models.Notification
import com.example.trailtogether_v01.data.models.NotificationType
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CommentsViewModel.kt
 *
 * Gère l'affichage et l'ajout de commentaires sur les posts.
 *
 * Fonctionnalités:
 * - Chargement des commentaires d'un post (Flow réactif)
 * - Ajout de nouveau commentaire
 * - Création de notification pour l'auteur du post
 * - Incrémentation du compteur de commentaires
 *
 * StateFlows exposés:
 * - comments: Liste des commentaires du post actuel
 * - isLoading: Indicateur de chargement
 *
 * Méthodes:
 * - loadComments(postId): Charge les commentaires d'un post
 * - sendComment(content): Envoie un nouveau commentaire
 *   - Crée le commentaire dans Firestore
 *   - Incrémente commentsCount du post
 *   - Crée notification pour l'auteur du post
 *
 * Logique de notification:
 * - Ne crée pas de notification si l'auteur commente son propre post
 * - Notification de type COMMENT
 * - Contient le nom du commentateur et le début du commentaire
 *
 * Initialisation:
 * - Nécessite le postId passé lors de la création
 * - Charge automatiquement les commentaires au démarrage
 *
 * Utilisation:
 * - Utilisé par CommentsScreen
 * - Un ViewModel par post (nouvelle instance pour chaque écran de commentaires)
 */

class CommentsViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    // ID du post actuel pour savoir où envoyer le commentaire
    private var currentPostId: String = ""

    fun loadComments(postId: String) {
        currentPostId = postId
        viewModelScope.launch {
            repository.getComments(postId).collect {
                _comments.value = it
            }
        }
    }

    fun sendComment(content: String) {
        if (content.isBlank() || currentPostId.isBlank()) return

        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            repository.addComment(currentPostId, content)

            val postAuthorId = repository.getPostAuthorId(currentPostId)
            if (postAuthorId != null && postAuthorId != currentUser.uid) {
                val notification = Notification(
                    recipientId = postAuthorId,
                    senderId = currentUser.uid,
                    senderName = currentUser.displayName ?: "Utilisateur",
                    type = NotificationType.COMMENT,
                    postId = currentPostId,
                    content = "${currentUser.displayName ?: "Quelqu'un"} a commenté votre publication"
                )
                repository.createNotification(notification)
            }
        }
    }
}