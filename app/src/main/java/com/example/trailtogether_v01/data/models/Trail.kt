package com.example.trailtogether_v01.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Trail(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val distance: Double = 0.0,
    val duration: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val tags: List<String> = emptyList()
)

enum class Difficulty {
    EASY, MODERATE, HARD, EXPERT
}