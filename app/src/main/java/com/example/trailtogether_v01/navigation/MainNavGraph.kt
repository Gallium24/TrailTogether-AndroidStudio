package com.example.trailtogether_v01.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.trailtogether_v01.ui.screens.calendar.CalendarScreen
import com.example.trailtogether_v01.ui.screens.feed.CreatePostScreen
import com.example.trailtogether_v01.ui.screens.feed.FeedScreen
import com.example.trailtogether_v01.ui.screens.home.HomeScreen
import com.example.trailtogether_v01.ui.screens.home.TrailDetailScreen
import com.example.trailtogether_v01.ui.screens.profile.EditProfileScreen
import com.example.trailtogether_v01.ui.screens.profile.ProfileScreen

/**
 * MainNavGraph est le graphe de navigation interne de l'application.
 * Il contient les écrans principaux de l'application (Home, Feed, Calendar, Profile).
 */
@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier, onLogout: () -> Unit) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // --- Écrans principaux ---
        composable(Screen.Home.route) {
            HomeScreen(onNavigateToTrailDetail = { trailId ->
                navController.navigate("trail_detail_screen/$trailId")
            })
        }
        composable(Screen.Feed.route) {
            FeedScreen(
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateToEventDetail = {
                    navController.navigate(Screen.Calendar.route)
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = onLogout,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
            )
        }

        // --- Écrans additionnels ---
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "trail_detail_screen/{trailId}",
            arguments = listOf(navArgument("trailId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trailId = backStackEntry.arguments?.getString("trailId") ?: return@composable
            TrailDetailScreen(
                trailId = trailId,
                onNavigateBack = { navController.popBackStack() },
                // --- LOGIQUE POUR ALLER AU CALENDRIER ---
                onPlanEventClick = {
                    // On navigue vers la route du Calendrier
                    navController.navigate(Screen.Calendar.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}