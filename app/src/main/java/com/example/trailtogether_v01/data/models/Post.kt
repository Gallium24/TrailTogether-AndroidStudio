package com.example.trailtogether_v01.data.models

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val trailId: String,
    val trailName: String,
    val content: String,
    val imageUrl: String = "",
    val likesCount: Int,
    val commentsCount: Int,
    val timestamp: String,
    val isLiked: Boolean = false
)