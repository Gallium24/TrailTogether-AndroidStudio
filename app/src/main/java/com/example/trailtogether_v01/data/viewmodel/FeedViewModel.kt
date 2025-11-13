package com.example.trailtogether_v01.data.viewmodel

import com.google.firebase.Timestamp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        }
    }

    fun createPost(trailId: String, content: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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
            trailName = "Nom de la rando", // TODO: Récupérer le vrai nom de la rando
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