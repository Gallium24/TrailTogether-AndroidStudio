package com.example.trailtogether_v01.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.TrailDetailViewModel
import com.example.trailtogether_v01.ui.components.DifficultyBadge
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * TrailDetailScreen est la composante de l'écran de détail d'une randonnée.
 * @param trailId L'ID de la randonnée à afficher.
 * @param onNavigateBack Une fonction lambda appelée lorsque l'utilisateur clique sur le bouton "Retour".
 * @param onPlanEventClick Une fonction lambda appelée lorsque l'utilisateur clique sur le bouton "Planifier une sortie".
 * @param detailViewModel Le ViewModel de détail de la randonnée.
 */
@Composable
fun TrailDetailScreen(
    trailId: String,
    onNavigateBack: () -> Unit,
    onPlanEventClick: () -> Unit,
    detailViewModel: TrailDetailViewModel = viewModel()
) {
    LaunchedEffect(key1 = trailId) {
        detailViewModel.fetchTrailById(trailId)
    }


    // On observe l'état de la randonnée et du chargement depuis le ViewModel.
    val trail by detailViewModel.trail.collectAsState()
    val isLoading by detailViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBeige)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Détails du sentier",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading) {
            // Si c'est en cours de chargement, on affiche une roue.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Une fois le chargement terminé, on vérifie si on a bien une randonnée.
            trail?.let { t ->
                // Si la randonnée existe, on affiche ses détails.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.LightGray, RoundedCornerShape(16.dp))
                    )

                    // Titre and difficultés
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        DifficultyBadge(difficulty = t.difficulty)
                    }

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = TrailGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = t.location, fontSize = 16.sp, color = Color.Gray)
                    }

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn(
                            icon = Icons.Default.DirectionsWalk,
                            label = "Distance",
                            value = "${t.distance} km"
                        )
                        StatColumn(
                            icon = Icons.Default.Schedule,
                            label = "Durée",
                            value = t.duration
                        )
                        StatColumn(
                            icon = Icons.Default.Star,
                            label = "Note",
                            value = "${t.rating}/5"
                        )
                    }
                    Divider()
                    Text(
                        text = "Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = t.description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    if (t.tags.isNotEmpty()) {
                        Text(
                            text = "Tags",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            t.tags.forEach { tag ->
                                AssistChip(onClick = {}, label = { Text(tag) })
                            }
                        }
                    }
                    Button(
                        onClick = onPlanEventClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TrailGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Planifier une sortie")
                    }
                    OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sauvegarder")
                    }
                }
            } ?: run {
                // Si la randonnée n'a pas été trouvée après le chargement, on affiche un message.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Désolé, cette randonnée n'a pas été trouvée.")
                }
            }
        }
    }
}

@Composable
fun StatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TrailGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
