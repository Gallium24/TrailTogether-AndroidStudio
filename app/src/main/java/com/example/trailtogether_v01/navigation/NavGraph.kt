package com.example.trailtogether_v01.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.navigation.Screen
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel
import com.example.trailtogether_v01.ui.screens.auth.LoginScreen
import com.example.trailtogether_v01.ui.screens.auth.RegisterScreen
import com.example.trailtogether_v01.ui.screens.calendar.CalendarScreen
import com.example.trailtogether_v01.ui.screens.feed.CreatePostScreen
import com.example.trailtogether_v01.ui.screens.feed.FeedScreen
import com.example.trailtogether_v01.ui.screens.home.HomeScreen
import com.example.trailtogether_v01.ui.screens.home.TrailDetailScreen
import com.example.trailtogether_v01.ui.screens.profile.EditProfileScreen
import com.example.trailtogether_v01.ui.screens.profile.EmergencyContactScreen
import com.example.trailtogether_v01.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // fixe, pas dynamique
    ) {
        // --- LOGIN ---
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel // passe le ViewModel ici
            )
        }

        // --- REGISTER ---
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // --- HOME ---
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTrailDetail = { trailId ->
                    navController.navigate(Screen.TrailDetail.createRoute(trailId))
                }
            )
        }

        composable(
            route = Screen.TrailDetail.route,
            arguments = listOf(navArgument("trailId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trailId = backStackEntry.arguments?.getString("trailId") ?: ""
            TrailDetailScreen(
                trailId = trailId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Feed
        composable(Screen.Feed.route) {
            FeedScreen(
                onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) }
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Calendar
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateToEventDetail = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            // EventDetailScreen would go here
            navController.popBackStack()
        }

        // Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToEmergencyContact = { navController.navigate(Screen.EmergencyContact.route) }
            )
        }

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
    }

    if (authState is AuthState.Success) {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }
}