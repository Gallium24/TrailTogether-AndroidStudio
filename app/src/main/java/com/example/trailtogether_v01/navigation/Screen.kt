package com.example.trailtogether_v01.navigation

/**
 * Screen.kt
 *
 * Définit toutes les routes de navigation de l'application de manière centralisée et type-safe.
 *
 * Pattern sealed class:
 * - Garantit que toutes les routes sont connues à la compilation
 * - Évite les erreurs de typage dans les strings de routes
 * - Facilite la maintenance (ajout/suppression de routes)
 *
 * Routes définies:
 *
 * Auth:
 * - Login: "login"
 * - Register: "register"
 *
 * Main:
 * - Home: "home"
 * - Feed: "feed"
 * - Calendar: "calendar"
 * - Profile: "profile"
 * - Settings: "settings"
 *
 * Details:
 * - TrailDetail: "trail_detail/{trailId}"
 * - EventDetail: "event_detail/{eventId}"
 * - UserProfile: "user_profile/{userId}"
 *
 * Create:
 * - CreatePost: "create_post"
 * - CreateEvent: "create_event/{trailId}"
 *
 * Other:
 * - Comments: "comments/{postId}"
 * - Notifications: "notifications"
 * - EditProfile: "edit_profile"
 * - History: "history"
 *
 * Utilisation:
 * - Référencé dans tous les NavGraph (AuthNavGraph, MainNavGraph, RootNavGraph)
 * - Navigation: navController.navigate(Screen.TrailDetail.createRoute(trailId))
 * - Extraction paramètres: navBackStackEntry.arguments?.getString("trailId")
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
    object History : Screen("history_screen")

    object Setting : Screen("settings_screen")

    object CreateEvent : Screen("create_event_screen/{trailId}?trailName={trailName}") {
        fun createRoute(trailId: String, trailName: String) = "create_event_screen/$trailId?trailName=$trailName"
    }
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object Comments : Screen("comments_screen/{postId}") {
        fun createRoute(postId: String) = "comments_screen/$postId"
    }
    object EventDetail : Screen("event_detail_screen/{eventId}") {
        fun createRoute(eventId: String) = "event_detail_screen/$eventId"
    }

    // Écrans accessibles depuis l'onglet Home
    object TrailDetail : Screen("trail_detail_screen/{trailId}") {
        fun createRoute(trailId: String) = "trail_detail_screen/$trailId"
    }

    // Écrans accessibles depuis l'onglet Feed
    object CreatePost : Screen("create_post_screen")


    // Écrans accessibles via l'onglet Profile
    object EditProfile : Screen("edit_profile_screen")

    object Notifications : Screen("notifications")
}