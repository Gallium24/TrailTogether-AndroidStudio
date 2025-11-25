package com.example.trailtogether_v01.data.viewmodel

import com.google.firebase.Timestamp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Notification
import com.example.trailtogether_v01.data.models.NotificationType
import com.example.trailtogether_v01.data.models.Post
import com.example.trailtogether_v01.data.repository.FirestoreRepository // Changé de MockRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * FeedViewModel est responsable de la logique du flux d'actualités (FeedScreen).
 * Ses principales responsabilités
 * - Charger les posts depuis le FirestoreRepository.
 * - Gérer les likes sur les posts.
 * - Exposer l'état des posts (posts) et du chargement (isLoading) que l'UI peut observer.
 */
class FeedViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPosts()
    }

    // Pour la sélection de sentier dans CreatePost
    data class TrailSelection(val id: String, val name: String)

    private val _userHistoryTrails = MutableStateFlow<List<TrailSelection>>(emptyList())
    val userHistoryTrails: StateFlow<List<TrailSelection>> = _userHistoryTrails.asStateFlow()

    fun loadUserTrailsForPost() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getUserEvents(userId).collect { events ->
                // On extrait les sentiers uniques des événements
                val distinctTrails = events
                    .map { TrailSelection(it.trailId, it.trailName) }
                    .distinctBy { it.id } // On évite les doublons si on a fait 2 fois la même rando
                _userHistoryTrails.value = distinctTrails
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPosts().collect { postList ->
                _posts.value = postList
                _isLoading.value = false
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            val currentPost = _posts.value.find { it.id == postId } ?: return@launch
            repository.toggleLikePost(postId, currentPost.isLiked)
            // Le snapshot listener mettra à jour _posts automatiquement

            //envoie notif
            if (!currentPost.isLiked && currentPost.authorId != auth.currentUser?.uid) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val notification = Notification(
                        recipientId = currentPost.authorId,
                        senderId = currentUser.uid,
                        senderName = currentUser.displayName ?: "Utilisateur",
                        type = NotificationType.LIKE,
                        postId = postId,
                        content = "${currentUser.displayName ?: "Quelqu'un"} a aimé votre publication"
                    )
                    repository.createNotification(notification)
                }
            }
        }
    }

    fun createPost(trailId: String, trailName : String, content: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // 'auth' est maintenant défini
        val currentUser = auth.currentUser ?: run {
            onFailure(Exception("Utilisateur non connecté"))
            return
        }

        // Pour l'instant, on utilise le displayName de l'objet User de Firebase Auth
        val authorName = currentUser.displayName ?: "Utilisateur anonyme"

        val post = Post(
            id = "", // Firestore génére l'ID
            authorId = currentUser.uid,
            authorName = authorName,
            authorUsername = currentUser.email?.substringBefore('@') ?: "anonyme",
            trailId = trailId,
            trailName = trailName,
            content = content,
            likesCount = 0,
            commentsCount = 0,
            timestamp = Timestamp.now()
        )

        db.collection("posts").add(post)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}