package com.example.trailtogether_v01.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.trailtogether_v01.navigation.Screen
import com.example.trailtogether_v01.ui.screens.calendar.CalendarScreen
import com.example.trailtogether_v01.ui.screens.feed.FeedScreen
import com.example.trailtogether_v01.ui.screens.home.HomeScreen
import com.example.trailtogether_v01.ui.screens.profile.ProfileScreen
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel

// Modèle pour les éléments de la barre de navigation
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val mainNavController = rememberNavController()

    // Liste des écrans de la barre de navigation
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Home.route),
        BottomNavItem("Feed", Icons.Default.Feed, Screen.Feed.route),
        BottomNavItem("Calendar", Icons.Default.DateRange, Screen.Calendar.route),
        BottomNavItem("Profile", Icons.Default.Person, Screen.Profile.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            mainNavController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(mainNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when re-selecting the same item
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost interne pour les écrans principaux
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                // NOTE: La logique de navigation vers TrailDetail est maintenant gérée par le NavGraph principal.
                // Si vous avez besoin de naviguer, vous devrez passer le NavController principal ici.
                // Pour l'instant, on simplifie.
                HomeScreen(onNavigateToTrailDetail = {})
            }
            composable(Screen.Feed.route) {
                FeedScreen(onNavigateToCreatePost = {})
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(onNavigateToEventDetail = {})
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