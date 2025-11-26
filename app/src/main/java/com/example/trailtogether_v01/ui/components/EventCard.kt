package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailtogether_v01.data.models.Event
import com.example.trailtogether_v01.ui.theme.SurfaceBeige
import com.example.trailtogether_v01.ui.theme.TextDark

/**
 * EventCard.kt
 *
 * Carte affichant les informations d'un événement de randonnée.
 *
 * Informations affichées:
 * - Nom de l'événement
 * - Nom du sentier associé (cliquable → TrailDetailScreen)
 * - Date et heure
 * - Nom de l'organisateur
 * - Nombre de participants / maximum
 * - Badge de difficulté
 *
 * Interactions:
 * - Clic sur la card → EventDetailScreen (détails + inscription)
 * - Clic sur nom du sentier → TrailDetailScreen
 *
 * Indicateurs visuels:
 * - Badge de difficulté avec couleur appropriée
 * - Icônes: Date, Heure, Organisateur, Participants
 * - Barre de progression participants (si implémenté)
 *
 * Design:
 * - Card Material 3 avec élévation
 * - Coins arrondis (16.dp)
 * - Padding interne (16.dp)
 * - Couleur de fond: Surface
 *
 * Paramètres:
 * - event: Objet Event à afficher
 * - onClick: Action lors du clic sur la card
 * - onTrailClick: Action lors du clic sur le nom du sentier
 *
 * Utilisation:
 * - Utilisé dans CalendarScreen (liste d'événements)
 * - Affiché dans LazyColumn avec espacement
 */

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                )

                Column {
                    Text(
                        text = event.trailName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${event.date} ${event.time}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${event.participantsCount}/${event.maxParticipants}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = event.distance,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            DifficultyBadge(difficulty = event.difficulty)
        }
    }
}