package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Post
import com.example.trailtogether_v01.data.models.User
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UserProfileViewModel.kt
 *
 * Gère l'affichage du profil d'un autre utilisateur (vue publique).
 *
 * Fonctionnalités:
 * - Chargement du profil d'un utilisateur spécifique
 * - Affichage des informations publiques
 * - Affichage des statistiques
 * - Pas d'édition (lecture seule)
 *
 * StateFlows exposés:
 * - user: Informations de l'utilisateur affiché
 * - isLoading: Indicateur de chargement
 *
 * Méthodes:
 * - loadUserProfile(userId): Charge le profil par ID
 *
 * Différence avec ProfileViewModel:
 * - UserProfileViewModel: Vue publique d'un autre utilisateur (lecture seule)
 * - ProfileViewModel: Profil de l'utilisateur connecté (éditable)
 *
 * Informations affichées:
 * - Nom et photo de profil
 * - Bio (si définie)
 * - Statistiques publiques:
 *   - Nombre de randonnées
 *   - Distance totale parcourue
 *
 * Informations masquées:
 * - Contact d'urgence (privé)
 * - Email (privé)
 * - Historique détaillé (privé)
 *
 * Utilisation:
 * - Utilisé par UserProfileScreen
 * - Navigation depuis un post (clic sur nom d'auteur)
 * - Navigation depuis liste de participants d'un événement
 */

class UserProfileViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val auth = Firebase.auth

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser: StateFlow<Boolean> = _isCurrentUser.asStateFlow()

    fun loadData(userId: String) {
        val currentUid = auth.currentUser?.uid
        _isCurrentUser.value = (currentUid == userId)

        // Charger l'utilisateur
        viewModelScope.launch {
            repository.getUserById(userId).collect { fetchedUser ->
                _user.value = fetchedUser
            }
        }

        // Charger ses posts
        viewModelScope.launch {
            repository.getPostsByAuthor(userId).collect { fetchedPosts ->
                _posts.value = fetchedPosts
            }
        }

        // Vérifier l'abonnement (si ce n'est pas nous-même)
        if (!_isCurrentUser.value) {
            viewModelScope.launch {
                repository.isFollowing(userId).collect { following ->
                    _isFollowing.value = following
                }
            }
        }
    }

    fun toggleFollow() {
        val targetUserId = _user.value?.id ?: return
        viewModelScope.launch {
            repository.toggleFollow(targetUserId, _isFollowing.value)
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            val currentPost = _posts.value.find { it.id == postId } ?: return@launch
            repository.toggleLikePost(postId, currentPost.isLiked)
        }
    }
}