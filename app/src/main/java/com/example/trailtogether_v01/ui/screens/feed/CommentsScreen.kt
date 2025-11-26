package com.example.trailtogether_v01.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.models.Comment
import com.example.trailtogether_v01.data.viewmodel.CommentsViewModel
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * CommentsScreen.kt
 *
 * Écran affichant les commentaires d'un post et permettant d'en ajouter.
 *
 * Fonctionnalités:
 * - Affichage de la liste des commentaires
 * - Ajout de nouveaux commentaires
 * - Affichage de l'auteur et timestamp de chaque commentaire
 * - Scroll automatique vers le bas lors de l'ajout
 *
 * Layout:
 * - Header: Titre "Commentaires" avec bouton retour
 * - Liste: Commentaires avec avatars et noms
 * - Bottom bar: Zone de saisie + bouton envoyer
 *
 * Affichage commentaire:
 * - Avatar de l'auteur (ou initiale)
 * - Nom de l'auteur
 * - Contenu du commentaire
 * - Timestamp relatif (ex: "Il y a 5 min")
 *
 * Zone de saisie:
 * - TextField multilignes
 * - Bouton "Envoyer" (icône Send)
 * - Désactivé si texte vide
 * - Se vide après envoi
 *
 * États:
 * - isLoading: CircularProgressIndicator
 * - Liste vide: "Aucun commentaire pour l'instant"
 * - Avec commentaires: LazyColumn scrollable
 *
 * Notifications:
 * - Création automatique de notification pour l'auteur du post
 * - Type: COMMENT
 * - Pas de notification si l'auteur commente son propre post
 *
 * Interactions:
 * - Clic sur nom d'auteur → UserProfileScreen (si implémenté)
 * - Envoi → Crée commentaire + notification + incrémente compteur
 *
 * Utilisation:
 * - Navigation depuis FeedScreen (clic sur icône commentaires)
 * - Paramètre: postId
 * - CommentsViewModel géré automatiquement avec postId
 * - Partie de MainNavGraph
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    viewModel: CommentsViewModel = viewModel()
) {
    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    val comments by viewModel.comments.collectAsState()
    var newCommentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Commentaires") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            // Zone de saisie du commentaire
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Ajouter un commentaire...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                viewModel.sendComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        enabled = newCommentText.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Envoyer",
                            tint = if (newCommentText.isNotBlank()) TrailGreen else Color.Gray
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBeige)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            if (comments.isEmpty()) {
                item {
                    Text(
                        "Soyez le premier à commenter !",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }
            items(comments) { comment ->
                CommentItem(comment)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.onSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = android.text.format.DateFormat.format("dd MMM HH:mm", comment.timestamp.toDate()).toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}