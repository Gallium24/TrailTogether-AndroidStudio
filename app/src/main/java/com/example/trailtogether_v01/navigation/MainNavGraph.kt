package com.example.trailtogether_v01.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.viewmodel.HomeViewModel
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
    val homeViewModel: HomeViewModel = viewModel()
    val trailCache = remember { mutableMapOf<String, Trail>() }

    val allTrails by homeViewModel.allTrails.collectAsState()

    LaunchedEffect(allTrails) {
        // Mettre à jour le cache chaque fois que allTrails change
        allTrails.forEach { trail ->
            trailCache[trail.id] = trail
        }
        Log.d("MainNavGraph", "🔄 Cache mis à jour: ${trailCache.size} trails")
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // --- Écrans principaux ---
        composable(Screen.Home.route) {
            HomeScreen(
                homeViewModel = homeViewModel, // 🔥 Passer la même instance
                onNavigateToTrailDetail = { trailId ->
                    Log.d("MainNavGraph", "🔍 Navigation vers trail: $trailId")
                    Log.d("MainNavGraph", "📦 Cache contient ${trailCache.size} trails: ${trailCache.keys.take(5)}")

                    val trail = trailCache[trailId]

                    if (trail != null) {
                        Log.d("MainNavGraph", "✅ Trail trouvé dans le cache:")
                        Log.d("MainNavGraph", "  - Nom: ${trail.name}")
                        Log.d("MainNavGraph", "  - Source: ${trail.source}")
                        Log.d("MainNavGraph", "  - Distance: ${trail.distance}")
                    } else {
                        Log.e("MainNavGraph", "❌ Trail non trouvé: $trailId")
                        Log.e("MainNavGraph", "   IDs disponibles: ${trailCache.keys.toList()}")
                    }

                    navController.navigate("trail_detail_screen/$trailId")
                }
            )

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
            val trailId = backStackEntry.arguments?.getString("trailId") ?: ""

            Log.d("MainNavGraph", "📄 TrailDetailScreen ouvert pour: $trailId")

            val preloadedTrail = trailCache[trailId]

            if (preloadedTrail != null) {
                Log.d("MainNavGraph", "✅ Trail récupéré du cache: ${preloadedTrail.name}")
            } else {
                Log.e("MainNavGraph", "❌ Trail non trouvé dans le cache")
            }

            TrailDetailScreen(
                trailId = trailId,
                preloadedTrail = preloadedTrail,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlanEventClick = {
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