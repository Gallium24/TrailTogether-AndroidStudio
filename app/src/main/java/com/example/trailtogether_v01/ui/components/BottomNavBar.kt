package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.trailtogether_v01.navigation.Screen
import com.example.trailtogether_v01.ui.theme.TrailGreen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)
/**
 * BottomNavBar est une composante qui représente la barre de navigation inférieure de l'application.
 * @param navController NavController représente la navigation entre les différents écrans de l'application.
 * @param content Slot qui contient le contenu de l'écran actuel.
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Feed.route, Icons.Default.List, "Feed"),
        BottomNavItem(Screen.Home.route, Icons.Default.Home, "Accueil"),
        BottomNavItem(Screen.Profile.route, Icons.Default.Person, "Profil")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFD4C5A0),
                modifier = Modifier.height(110.dp)
            ) {
                items.forEachIndexed { index, item ->
                    // On considère l'item sélectionné si la route correspond
                    // OU si on est sur l'Accueil et qu'on est dans une sous-route (Calendrier ou Détail)
                    val isSelected = currentRoute == item.route ||
                            (item.route == Screen.Home.route &&
                                    (currentRoute == Screen.Calendar.route || currentRoute?.startsWith("trail_detail") == true))

                    val isCenter = index == 1

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop jusqu'au début du graphe pour éviter d'empiler les écrans
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true

                                // CORRECTION ICI :
                                // On restaure l'état UNIQUEMENT si ce n'est pas l'écran Home.
                                // Pour Home, on veut "reset" à la racine (la liste des trails) et ne pas revenir sur le Calendrier.
                                restoreState = item.route != Screen.Home.route
                            }
                        },
                        icon = {
                            if (isCenter) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .background(
                                            TrailGreen,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        label = { if (!isCenter) Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TrailGreen,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ){ innerPadding ->
        content(innerPadding)
    }
}