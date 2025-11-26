package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailtogether_v01.data.models.Post
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * PostCard.kt
 *
 * Carte affichant un post du fil d'actualité.
 *
 * Informations affichées:
 * - Nom de l'auteur (cliquable → UserProfileScreen)
 * - Timestamp relatif (ex: "Il y a 2 heures")
 * - Contenu textuel du post
 * - Image (si présente)
 * - Sentier lié (si présent, cliquable → TrailDetailScreen)
 * - Compteur de likes avec bouton
 * - Compteur de commentaires avec bouton
 *
 * Interactions:
 * - Clic sur nom auteur → UserProfileScreen
 * - Clic sur bouton like → Toggle like/unlike
 * - Clic sur bouton commentaires → CommentsScreen
 * - Clic sur nom sentier → TrailDetailScreen
 *
 * Indicateurs visuels:
 * - Icône cœur rouge si utilisateur a liké
 * - Icône cœur gris sinon
 * - Compteurs likes et commentaires
 *
 * Design:
 * - Card Material 3 avec élévation
 * - Image en 16:9 si présente
 * - Coins arrondis
 * - Padding interne
 *
 * Paramètres:
 * - post: Objet Post à afficher
 * - currentUserId: ID utilisateur courant (pour vérifier si liké)
 * - onLikeClick: Callback like/unlike
 * - onCommentClick: Callback commentaires
 * - onAuthorClick: Callback profil auteur
 * - onTrailClick: Callback sentier lié (optionnel)
 *
 * Utilisation:
 * - Utilisé dans FeedScreen (liste de posts)
 * - Affiché dans LazyColumn avec espacement
 */

@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onUserClick: (String) -> Unit = {},
    onCommentClick: () -> Unit = {} // <-- NOUVEAU
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable { onUserClick(post.authorId) }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
                Column {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = android.text.format.DateFormat.format("dd MMM yyyy HH:mm", post.timestamp.toDate()).toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
        }

        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.secondary)
        )

        // Footer
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = TrailGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = post.trailName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.content,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onLikeClick)
                ) {
                    Icon(
                        if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likesCount} likes", fontSize = 14.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() } // <-- ACTION ICI
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentsCount} comments", fontSize = 14.sp)
                }
            }
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
    }
}