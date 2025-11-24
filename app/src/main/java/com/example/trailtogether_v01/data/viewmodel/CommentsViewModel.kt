package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Comment
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentsViewModel : ViewModel() {
    private val repository = FirestoreRepository()

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

        viewModelScope.launch {
            repository.addComment(currentPostId, content)
        }
    }
}