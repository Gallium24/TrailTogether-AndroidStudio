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