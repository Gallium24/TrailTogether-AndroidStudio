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
                    val isSelected = currentRoute == item.route
                    val isCenter = index == 1

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (isCenter) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight() // Remplit toute la hauteur disponible
                                        .aspectRatio(1f) // Force la largeur à être égale à la hauteur, créant un carré
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
                        label = { if (!isCenter) Text(item.label) }, // Ne pas afficher de label pour le bouton central
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