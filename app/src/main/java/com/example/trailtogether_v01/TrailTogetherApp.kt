package com.example.trailtogether_v01

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel
import com.example.trailtogether_v01.navigation.NavGraph
import com.example.trailtogether_v01.navigation.Screen
import com.example.trailtogether_v01.ui.components.BottomNavBar

/**
 * TrailTogetherApp est la composante principale de l'application.
 * Elle gère l'affichage des différents écrans de l'application.
 */
@Composable
fun TrailTogetherApp(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    val showBottomBar = authState is AuthState.Success

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute in listOf(
                        Screen.Home.route,
                        Screen.Feed.route,
                        Screen.Profile.route,
                        Screen.Calendar.route
                    )) {
                    BottomNavBar(
                        navController = navController,
                        content = TODO()
                    )
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            
            navController = navController,
            authViewModel = authViewModel
        )
    }
}