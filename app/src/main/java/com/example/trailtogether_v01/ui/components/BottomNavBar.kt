package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.trailtogether_v01.data.viewmodel.NotificationsViewModel
import com.example.trailtogether_v01.navigation.Screen
import com.example.trailtogether_v01.ui.theme.TrailGreen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavBar(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    val notificationsViewModel: NotificationsViewModel = viewModel()
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

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
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.height(110.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = currentRoute == item.route ||
                            (item.route == Screen.Home.route &&
                                    (currentRoute == Screen.Calendar.route || currentRoute?.startsWith("trail_detail") == true))

                    val isCenter = index == 1

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
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
                                if (item.route == Screen.Feed.route && unreadCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = Color.Red,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = if (unreadCount > 99) "99+" else "$unreadCount",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
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
    ) { innerPadding ->
        content(innerPadding)
    }
}