package com.example.trailtogether_v01.ui.screens.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trailtogether_v01.navigation.Screen
import com.example.trailtogether_v01.ui.components.BottomNavBar
import com.example.trailtogether_v01.ui.screens.feed.FeedScreen
import com.example.trailtogether_v01.ui.screens.home.HomeScreen
import com.example.trailtogether_v01.ui.screens.profile.ProfileScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val mainNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = mainNavController)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onNavigateToTrailDetail = {})
            }
            composable(Screen.Feed.route) {
                FeedScreen(onNavigateToCreatePost = {})
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToEditProfile = {},
                    onNavigateToEmergencyContact = {}
                )
            }
        }
    }
}