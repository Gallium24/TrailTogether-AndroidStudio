package com.example.trailtogether_v01.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val trailsCount: Int = 0
)