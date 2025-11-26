package com.example.trailtogether_v01.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.data.viewmodel.EventDetailViewModel
import com.example.trailtogether_v01.ui.components.DifficultyBadge
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import com.example.trailtogether_v01.ui.theme.TrailGreen
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import com.example.trailtogether_v01.workers.EmergencyWorker
import android.widget.Toast

@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    viewModel: EventDetailViewModel = viewModel()
) {
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    val event by viewModel.event.collectAsState()
    val associatedTrail by viewModel.associatedTrail.collectAsState()
    val isFuture by viewModel.isEventInFuture.collectAsState()
    val context = LocalContext.current

    // Récupérer l'email d'urgence depuis le ViewModel
    val emergencyEmail by viewModel.currentUserEmergencyEmail.collectAsState()

    // Confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (isFuture) "Annuler la sortie ?" else "Supprimer l'historique ?") },
            text = {
                Text(if (isFuture)
                    "Êtes-vous sûr de vouloir annuler cette sortie ? Les autres participants ne seront plus notifiés."
                else "Voulez-vous retirer cet événement de votre historique ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent {
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Retour") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                text = "Détails de la sortie",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- CARTE (Si on a le trail associé) ---
                if (associatedTrail?.hasValidCoordinates() == true) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)

                                    val startPoint = associatedTrail!!.getTrailStartPoint()
                                    startPoint?.let { point ->
                                        controller.setZoom(14.0)
                                        controller.setCenter(point)

                                        // Marker de départ
                                        val marker = Marker(this).apply {
                                            position = point
                                            title = associatedTrail!!.name
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        }
                                        overlays.add(marker)

                                        // --- AJOUT DU TRACÉ (POLYLINE) ---
                                        val pathPoints = associatedTrail!!.getPathAsGeoPoints()
                                        if (pathPoints.isNotEmpty()) {
                                            val polyline = Polyline().apply {
                                                setPoints(pathPoints)
                                                // Couleur selon la difficulté
                                                outlinePaint.color = when (associatedTrail!!.difficulty) {
                                                    Difficulty.EASY -> android.graphics.Color.GREEN
                                                    Difficulty.MODERATE -> android.graphics.Color.rgb(255, 152, 0)
                                                    Difficulty.HARD -> android.graphics.Color.RED
                                                    Difficulty.EXPERT -> android.graphics.Color.BLACK
                                                }
                                                outlinePaint.strokeWidth = 8f
                                            }
                                            overlays.add(polyline)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color.Gray)
                            Text("Carte non disponible", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                // --- INFO PRINCIPALES ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event!!.trailName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // Utilise la difficulté du trail associé pour être cohérent
                    val difficulty = associatedTrail?.difficulty ?: event!!.difficulty
                    DifficultyBadge(difficulty = difficulty)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${event!!.date} à ${event!!.time}", fontSize = 16.sp, color = Color.Gray)
                }

                // --- STATS ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // On priorise les infos du Trail complet (distance/durée précises)
                        // Sinon on utilise celles de l'event (souvent vides ou par défaut)
                        val displayDistance = associatedTrail?.distance?.toString()?.takeIf { it.isNotBlank() }
                            ?: event!!.distance.toString().takeIf { it.isNotBlank() }
                            ?: "?"
                        val displayDuration = associatedTrail?.duration?.ifEmpty { event!!.duration } ?: "?"

                        StatColumn(Icons.Default.DirectionsWalk, "Distance", displayDistance)
                        StatColumn(Icons.Default.Schedule, "Durée", displayDuration)
                        StatColumn(Icons.Default.Group, "Participants", "${event!!.participantsCount}/${event!!.maxParticipants}")
                    }
                }

                Divider()

                // --- DESCRIPTION DE L'EVENT ---
                Text(
                    text = "Message de l'organisateur",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (event!!.description.isNotBlank()) {
                    Text(
                        text = event!!.description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "Aucune description fournie.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- ZONE D'ACTIONS ---
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // BOUTON 1 : PARTIR MAINTENANT (Visible si futur/aujourd'hui et pas encore parti/fini)
                    if (isFuture && event?.status == "PLANNED") { // On suppose que le statut par défaut est "PLANNED"
                        Button(
                            onClick = {
                                if (emergencyEmail.isNullOrBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Veuillez configurer un email d'urgence dans votre profil",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    // 1. Lancer le Worker de 24h
                                    val data = workDataOf(
                                        "eventId" to eventId,
                                        "recipientEmail" to emergencyEmail
                                    )

                                    val alertWork = OneTimeWorkRequestBuilder<EmergencyWorker>()
                                        .setInitialDelay(30, TimeUnit.SECONDS)
                                        .setInputData(data)
                                        .addTag(eventId)
                                        .build()

                                    WorkManager.getInstance(context).enqueue(alertWork)

                                    // 2. Mettre à jour le statut dans Firestore
                                    viewModel.startHike()

                                    Toast.makeText(
                                        context,
                                        "Bonne rando ! Sécurité activée (24h)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrailGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Partir maintenant")
                        }
                    }

                    // BOUTON 2 : JE SUIS RENTRÉ (Visible si la rando a commencé)
                    if (event?.status == "STARTED") {
                        Button(
                            onClick = {
                                viewModel.markAsSafe(context)
                                Toast.makeText(context, "Bon retour ! Alerte de sécurité désactivée.", Toast.LENGTH_LONG).show() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Vert vif
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Je suis bien rentré")
                        }
                    }

                    // BOUTON 3 : ANNULER / SUPPRIMER
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isFuture) Icons.Default.Cancel else Icons.Default.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFuture) "Annuler la sortie" else "Supprimer de l'historique")
                    }
                }
            }
        }
    }
}

// Composant local pour éviter les problèmes d'import
@Composable
private fun StatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TrailGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}