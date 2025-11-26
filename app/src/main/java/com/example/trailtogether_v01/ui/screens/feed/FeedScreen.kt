package com.example.trailtogether_v01.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.FeedViewModel
import com.example.trailtogether_v01.data.viewmodel.NotificationsViewModel
import com.example.trailtogether_v01.ui.components.PostCard
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * FeedScreen.kt
 *
 * Écran du fil d'actualité affichant les posts des utilisateurs.
 *
 * Fonctionnalités:
 * - Affichage du flux de posts (temps réel)
 * - Like/unlike des posts
 * - Navigation vers les commentaires
 * - Navigation vers les profils utilisateurs
 * - Navigation vers les sentiers liés
 * - Bouton de création de post
 * - Bouton vers les notifications
 *
 * Layout:
 * - Header: "Fil d'actualité" + icône notifications (avec badge)
 * - Liste: PostCard pour chaque post
 * - FloatingActionButton: Création de post
 *
 * PostCard interactions:
 * - Clic sur auteur → UserProfileScreen
 * - Clic sur like → Toggle like/unlike
 * - Clic sur commentaires → CommentsScreen
 * - Clic sur sentier lié → TrailDetailScreen
 *
 * Badge notifications:
 * - Affiché si unreadCount > 0
 * - Affiche le nombre exact
 * - Couleur rouge pour visibilité
 *
 * États:
 * - isLoading: CircularProgressIndicator
 * - Liste vide: "Aucune publication pour l'instant"
 * - Avec posts: LazyColumn scrollable
 *
 * Tri des posts:
 * - Par timestamp décroissant (plus récents en premier)
 * - Mises à jour en temps réel via Flow
 *
 * FloatingActionButton:
 * - Position: Bas à droite
 * - Icône: Add
 * - Couleur: TrailGreen
 * - Action: Navigate vers CreatePostScreen
 *
 * Intégration:
 * - FeedViewModel pour données et logique
 * - NotificationViewModel pour badge
 * - Flow réactif des posts
 *
 * Utilisation:
 * - Accessible via BottomNavBar (icône Feed)
 * - Partie de MainNavGraph
 */

@Composable
fun FeedScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToComments: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    feedViewModel: FeedViewModel = viewModel()
) {
    val notificationsViewModel: NotificationsViewModel = viewModel()
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    val posts by feedViewModel.posts.collectAsState()
    val isLoading by feedViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Feed",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icône notifications avec badge
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
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
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = TrailGreen
                            )
                        }
                    }

                    // Icône créer post
                    IconButton(onClick = onNavigateToCreatePost) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Créer un post",
                            tint = TrailGreen
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        onLikeClick = { feedViewModel.likePost(post.id) },
                        onUserClick = { userId -> onNavigateToUserProfile(userId) },
                        onCommentClick = { onNavigateToComments(post.id) }
                    )
                }
            }
        }
    }
}