package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.ui.theme.SurfaceBeige
import com.example.trailtogether_v01.ui.theme.TextDark
import com.example.trailtogether_v01.utils.FormatUtils

@Composable
fun TrailCard(
    trail: Trail,
    onClick: () -> Unit,
    useImperialUnits: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trail.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = trail.location,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                DifficultyBadge(difficulty = trail.difficulty)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TrailStat(
                    icon = Icons.Default.DirectionsWalk,
                    value = FormatUtils.formatDistance(trail.distance, useImperialUnits)
                )
                TrailStat(
                    icon = Icons.Default.Schedule,
                    value = trail.duration
                )

                // Afficher rating uniquement si disponible (trails Firestore notamment)
                if (trail.rating > 0 || trail.reviewsCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFC107)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${trail.rating} (${trail.reviewsCount})",
                            fontSize = 14.sp
                        )
                    }
                } else if (trail.source == "osm") {
                    // Pour les trails OSM, afficher le dénivelé s'il est disponible
                    if (trail.elevation.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trail.elevation.split("/").firstOrNull()?.trim() ?: trail.elevation,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrailStat(icon: ImageVector, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DifficultyBadge(difficulty: Difficulty) {
    val (color, text) = when (difficulty) {
        Difficulty.EASY -> Color(0xFF4CAF50) to "Facile"
        Difficulty.MODERATE -> Color(0xFFFFC107) to "Modéré"
        Difficulty.HARD -> Color(0xFFFF9800) to "Difficile"
        Difficulty.EXPERT -> Color(0xFFF44336) to "Expert"
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
