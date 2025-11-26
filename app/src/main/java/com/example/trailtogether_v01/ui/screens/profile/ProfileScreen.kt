package com.example.trailtogether_v01.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.ProfileViewModel
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * ProfileScreen.kt
 *
 * Écran du profil de l'utilisateur connecté.
 *
 * Fonctionnalités:
 * - Affichage des informations du profil
 * - Statistiques de randonnées
 * - Accès à l'édition du profil
 * - Accès à l'historique complet
 * - Bouton de déconnexion
 *
 * Layout:
 * 1. Header avec photo de profil:
 *    - Photo circulaire (ou initiales si pas de photo)
 *    - Nom de l'utilisateur
 *    - Email
 *
 * 2. Bouton "Éditer le profil":
 *    - Navigate vers EditProfileScreen
 *    - Icône Edit
 *
 * 3. Bio:
 *    - Affichée si renseignée
 *    - Texte multiligne
 *
 * 4. Carte des statistiques:
 *    - Nombre de randonnées effectuées
 *    - Distance totale parcourue (formatée)
 *    - Présentation en colonnes
 *
 * 5. Contact d'urgence:
 *    - Affiché si configuré
 *    - Icône téléphone
 *    - Lien pour appeler (si sur téléphone)
 *
 * 6. Boutons d'action:
 *    - "Voir l'historique" → HistoryScreen
 *    - "Paramètres" → SettingsScreen
 *    - "Se déconnecter" (couleur rouge)
 *
 * Affichage conditionnel:
 * - Si pas de bio: Section masquée
 * - Si pas de contact d'urgence: Section masquée
 * - Si 0 randonnées: Message encourageant + bouton vers HomeScreen
 *
 * États:
 * - isLoading: CircularProgressIndicator
 * - Données chargées: Affichage complet
 * - Erreur: Message d'erreur
 *
 * Formatage:
 * - Distance: km ou miles selon préférences
 * - Statistiques: Nombres formatés (ex: "12,5 km")
 *
 * Déconnexion:
 * - Dialogue de confirmation
 * - Appel à AuthViewModel.signOut()
 * - Navigation automatique vers AuthNavGraph
 *
 * Intégration:
 * - ProfileViewModel pour données utilisateur
 * - AuthViewModel pour déconnexion
 * - Flow réactif du profil (mises à jour en temps réel)
 *
 * Utilisation:
 * - Accessible via BottomNavBar (icône Profile)
 * - Partie de MainNavGraph
 */

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by profileViewModel.user.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { onNavigateToSettings() }) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user?.name ?: "Utilisateur",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${user?.username ?: "username"}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (user?.bio?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.bio ?: "",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(
                value = user?.trailsCount.toString(),
                label = "Randonnées"
            )
            StatColumn(
                value = user?.followersCount.toString(),
                label = "Abonnés"
            )
            StatColumn(
                value = user?.followingCount.toString(),
                label = "Abonnements"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Button(
            onClick = onNavigateToEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TrailGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Modifier le profil")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Randonnées sauvegardées")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateToHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Historique")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error, // Couleur rouge pour la déconnexion
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Déconnexion"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Déconnexion")
        }
    }
}

@Composable
fun StatColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}