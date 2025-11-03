package com.example.trailtogether_v01.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object TrailDetail : Screen("trail_detail/{trailId}") {
        fun createRoute(trailId: String) = "trail_detail/$trailId"
    }
    object Feed : Screen("feed")
    object CreatePost : Screen("create_post")
    object Calendar : Screen("calendar")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object EmergencyContact : Screen("emergency_contact")
}