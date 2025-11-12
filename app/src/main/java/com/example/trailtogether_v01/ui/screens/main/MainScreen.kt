package com.example.trailtogether_v01.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trailtogether_v01.navigation.Screen
import com.example.trailtogether_v01.ui.components.BottomNavBar
import com.example.trailtogether_v01.ui.screens.calendar.CalendarScreen
import com.example.trailtogether_v01.ui.screens.feed.CreatePostScreen
import com.example.trailtogether_v01.ui.screens.feed.FeedScreen
import com.example.trailtogether_v01.ui.screens.home.HomeScreen
import com.example.trailtogether_v01.ui.screens.home.TrailDetailScreen
import com.example.trailtogether_v01.ui.screens.profile.EditProfileScreen
import com.example.trailtogether_v01.ui.screens.profile.EmergencyContactScreen
import com.example.trailtogether_v01.ui.screens.profile.ProfileScreen

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    // On appelle BottomNavBar qui contient maintenant le Scaffold
    BottomNavBar(navController = navController) { innerPadding ->
        // Et on passe le NavHost comme contenu
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Écrans principaux ---
            composable(Screen.Home.route) {
                HomeScreen(onNavigateToTrailDetail = {
                    navController.navigate(Screen.TrailDetail.createRoute(it))
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
                    onNavigateToEmergencyContact = {
                        navController.navigate(Screen.EmergencyContact.route)
                    }
                )
            }

            // --- Écrans additionnels ---
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.EmergencyContact.route) {
                EmergencyContactScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.CreatePost.route) {
                CreatePostScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.TrailDetail.route) { backStackEntry ->
                val trailId = backStackEntry.arguments?.getString("trailId")
                if (trailId != null) {
                    TrailDetailScreen(
                        trailId = trailId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}