package com.example.trailtogether_v01.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.models.Notification
import com.example.trailtogether_v01.data.models.NotificationType
import com.example.trailtogether_v01.data.viewmodel.NotificationsViewModel
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * NotificationsScreen.kt
 *
 * Écran affichant toutes les notifications de l'utilisateur.
 *
 * Fonctionnalités:
 * - Liste des notifications (likes, commentaires)
 * - Distinction visuelle lues/non lues
 * - Marquage comme lu au clic
 * - Navigation vers le post concerné
 * - Bouton "Tout marquer comme lu"
 *
 * Layout:
 * 1. Header:
 *    - Titre "Notifications"
 *    - Bouton retour
 *    - Bouton "Tout marquer comme lu"
 *
 * 2. Liste de notifications:
 *    - Card pour chaque notification
 *    - Icône selon type:
 *      - LIKE: ❤️ Cœur rouge
 *      - COMMENT: 💬 Bulle de commentaire verte
 *    - Nom de l'expéditeur
 *    - Contenu de la notification
 *    - Timestamp relatif
 *
 * Distinction lues/non lues:
 * - Non lues: Background vert clair + badge vert
 * - Lues: Background blanc/gris clair
 *
 * Interactions:
 * - Clic sur notification:
 *   1. Marque comme lue
 *   2. Navigate vers le post concerné (FeedScreen)
 * - Clic sur "Tout marquer comme lu":
 *   - Marque toutes les notifications comme lues
 *   - Reset du badge sur BottomNavBar
 *
 * États:
 * - isLoading: CircularProgressIndicator
 * - Liste vide: "Aucune notification"
 * - Avec notifications: LazyColumn scrollable
 *
 * Tri:
 * - Par timestamp décroissant (plus récentes en premier)
 * - Mises à jour en temps réel via Flow
 *
 * Badge:
 * - Compte des notifications non lues
 * - Affiché sur BottomNavBar (icône Feed)
 * - Format: nombre exact ou "99+"
 *
 * Intégration:
 * - NotificationViewModel pour données et logique
 * - Flow réactif des notifications
 * - Mise à jour automatique du badge
 *
 * Utilisation:
 * - Navigation depuis FeedScreen (clic sur icône notifications)
 * - Accessible aussi via BottomNavBar si implémenté
 * - Partie de MainNavGraph
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateToPost: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Tout marquer comme lu", color = TrailGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBeige)
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Aucune notification",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBeige)
                    .padding(paddingValues)
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            viewModel.markAsRead(notification.id)
                            onNavigateToPost(notification.postId)
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (notification.isRead) Color.Transparent else Color(0xFFE8F5E9))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône selon le type
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    when (notification.type) {
                        NotificationType.LIKE -> Color(0xFFFF4444)
                        NotificationType.COMMENT -> TrailGreen
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when (notification.type) {
                    NotificationType.LIKE -> Icons.Default.Favorite
                    NotificationType.COMMENT -> Icons.Default.ChatBubble
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.content,
                fontSize = 14.sp,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = android.text.format.DateFormat.format(
                    "dd MMM yyyy HH:mm",
                    notification.timestamp.toDate()
                ).toString(),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(TrailGreen, CircleShape)
            )
        }
    }
}