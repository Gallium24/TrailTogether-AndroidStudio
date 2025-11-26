package com.example.trailtogether_v01.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.trailtogether_v01.data.viewmodel.UserProfileViewModel
import com.example.trailtogether_v01.ui.components.PostCard
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * UserProfileScreen.kt
 *
 * Écran affichant le profil public d'un autre utilisateur (lecture seule).
 *
 * Fonctionnalités:
 * - Affichage des informations publiques
 * - Statistiques de randonnées (publiques)
 * - Pas d'édition (vue lecture seule)
 *
 * Informations affichées:
 * - Photo de profil (ou initiales)
 * - Nom de l'utilisateur
 * - Bio (si renseignée et publique)
 * - Statistiques:
 *   - Nombre de randonnées
 *   - Distance totale parcourue
 *
 * Informations masquées (privées):
 * - Email
 * - Contact d'urgence
 * - Historique détaillé
 *
 * Layout:
 * 1. Header:
 *    - Bouton retour
 *    - Titre "Profil"
 *
 * 2. Section profil:
 *    - Photo circulaire
 *    - Nom (grand titre)
 *    - Bio (si disponible)
 *
 * 3. Statistiques publiques:
 *    - Carte avec stats
 *    - Présentation en colonnes
 *
 * 4. Actions possibles (futures):
 *    - Suivre l'utilisateur
 *    - Voir les posts de l'utilisateur
 *    - Envoyer un message
 *
 * États:
 * - isLoading: CircularProgressIndicator
 * - Utilisateur chargé: Affichage complet
 * - Utilisateur introuvable: Message d'erreur + bouton retour
 *
 * Différences avec ProfileScreen:
 * - Pas de bouton "Éditer"
 * - Pas de bouton "Déconnexion"
 * - Pas d'informations privées
 * - Pas d'accès à l'historique complet
 *
 * Navigation:
 * - Depuis FeedScreen (clic sur auteur de post)
 * - Depuis CommentsScreen (clic sur auteur de commentaire)
 * - Depuis EventDetailScreen (clic sur participant)
 *
 * Paramètre:
 * - userId: ID de l'utilisateur à afficher
 *
 * Intégration:
 * - UserProfileViewModel pour chargement des données
 * - Données chargées depuis Firestore par userId
 *
 * Utilisation:
 * - Partie de MainNavGraph avec paramètre userId
 * - Route: "user_profile/{userId}"
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: UserProfileViewModel = viewModel()
) {
    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    val user by viewModel.user.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val isCurrentUser by viewModel.isCurrentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.username ?: "Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBeige)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBeige)
                .padding(paddingValues)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user?.name ?: "Utilisateur",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (!user?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user?.bio ?: "",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn(value = user?.trailsCount.toString(), label = "Randonnées")
                        StatColumn(value = user?.followersCount.toString(), label = "Abonnés")
                        StatColumn(value = user?.followingCount.toString(), label = "Abonnements")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bouton Suivre / Ne plus suivre (si ce n'est pas nous-même)
                    if (!isCurrentUser) {
                        Button(
                            onClick = { viewModel.toggleFollow() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color.Gray else TrailGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isFollowing) "Ne plus suivre" else "S'abonner")
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Publications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }

            items(posts) { post ->
                // On désactive le clic sur le user ici pour éviter une boucle de navigation
                PostCard(
                    post = post,
                    onLikeClick = { viewModel.likePost(post.id) },
                    onUserClick = { /* Ne rien faire sur le profil même */ }
                )
            }
        }
    }
}