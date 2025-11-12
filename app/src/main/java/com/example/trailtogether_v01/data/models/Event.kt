package com.example.trailtogether_v01.data.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Event(
    val id: String = "",
    val trailId: String = "",
    val trailName: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val date: String = "",
    val time: String = "",
    val duration: String = "",
    val distance: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val participantsCount: Int = 0,
    val maxParticipants: Int = 20,
    val description: String = "",
    val meetingPoint: String = ""
)