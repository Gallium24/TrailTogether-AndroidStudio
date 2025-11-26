package com.example.trailtogether_v01.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.HistoryViewModel
import com.example.trailtogether_v01.ui.components.EventCard
import com.example.trailtogether_v01.ui.theme.BackgroundBeige

/**
 * HistoryScreen.kt
 *
 * Écran affichant l'historique complet des randonnées de l'utilisateur.
 *
 * Fonctionnalités:
 * - Liste de toutes les randonnées effectuées
 * - Statistiques globales
 * - Tri par date (plus récentes en premier)
 * - Navigation vers les détails des sentiers
 *
 * Layout:
 * 1. Header:
 *    - Titre "Historique"
 *    - Bouton retour
 *
 * 2. Carte des statistiques:
 *    - Nombre total de randonnées
 *    - Distance totale parcourue (formatée)
 *    - Temps total estimé (si disponible)
 *
 * 3. Liste des randonnées:
 *    - Card pour chaque randonnée
 *    - Informations affichées:
 *      - Nom du sentier (cliquable → TrailDetailScreen)
 *      - Date de la randonnée
 *      - Distance parcourue
 *      - Durée
 *      - Badge de difficulté
 *
 * Interactions:
 * - Clic sur nom du sentier → TrailDetailScreen
 * - Scroll infini si nombreuses randonnées
 *
 * États:
 * - isLoading: CircularProgressIndicator
 * - Historique vide: "Aucune randonnée effectuée"
 *   - Message encourageant
 *   - Bouton vers HomeScreen
 * - Avec historique: LazyColumn scrollable
 *
 * Tri et filtres:
 * - Par défaut: Date décroissante (plus récentes en premier)
 * - Options futures: Par distance, par difficulté
 *
 * Formatage:
 * - Dates: Format lisible (ex: "15 nov. 2024")
 * - Distances: km ou miles selon préférences
 * - Durée: Format "Xh XXmin"
 *
 * Statistiques:
 * - Calculées automatiquement depuis l'historique
 * - Mises à jour en temps réel
 * - Affichées dans ProfileScreen aussi
 *
 * Intégration:
 * - HistoryViewModel pour données
 * - ProfileViewModel pour stats globales
 * - Flow réactif de l'historique
 *
 * Utilisation:
 * - Navigation depuis ProfileScreen (bouton "Voir l'historique")
 * - Partie de MainNavGraph
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val events by viewModel.userEvents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Historique") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (events.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Aucune sortie planifiée pour le moment.", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onClick = { onNavigateToEventDetail(event.id) } // <--- Connexion
                    )
                }
            }
        }
    }
}