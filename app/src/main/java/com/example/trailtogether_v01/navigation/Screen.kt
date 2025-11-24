package com.example.trailtogether_v01.navigation

/**
 * Screen est une sealed class qui représente les différents écrans de l'application.
 * Chaque écran a une route unique qui est utilisée pour identifier l'écran dans la navigation.
 */
sealed class Screen(val route: String) {
    // Écrans d'authentification
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")

    // Écran principal qui contient la barre de navigation
    // (Cette route n'est plus utilisée directement si MainActivity fait l'aiguillage, mais on la garde par propreté)
    object Main : Screen("main_screen")

    // Écrans accessibles depuis la barre de navigation
    object Home : Screen("home_screen")
    object Feed : Screen("feed_screen")
    object Profile : Screen("profile_screen")
    object Calendar : Screen("calendar_screen")

    object CreateEvent : Screen("create_event_screen/{trailId}") {
        fun createRoute(trailId: String) = "create_event_screen/$trailId"
    }
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }

    // Écrans accessibles depuis l'onglet Home
    object TrailDetail : Screen("trail_detail_screen/{trailId}") {
        fun createRoute(trailId: String) = "trail_detail_screen/$trailId"
    }

    // Écrans accessibles depuis l'onglet Feed
    object CreatePost : Screen("create_post_screen")


    // Écrans accessibles via l'onglet Profile
    object EditProfile : Screen("edit_profile_screen")

}