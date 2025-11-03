package com.example.trailtogether_v01.data.models

data class Trail(
    val id: String,
    val name: String,
    val location: String,
    val distance: Double,
    val duration: String,
    val difficulty: Difficulty,
    val rating: Float,
    val reviewsCount: Int,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String = "",
    val tags: List<String> = emptyList()
)

enum class Difficulty {
    EASY, MODERATE, HARD, EXPERT
}