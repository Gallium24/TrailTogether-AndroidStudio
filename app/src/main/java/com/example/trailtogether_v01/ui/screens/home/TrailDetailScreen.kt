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
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.data.models.Trail
import android.util.Log

@Composable
fun TrailDetailScreen(
    trailId: String,
    preloadedTrail: Trail? = null,
    onNavigateBack: () -> Unit,
    onPlanEventClick: () -> Unit,
    detailViewModel: TrailDetailViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = trailId, key2 = preloadedTrail) {
        Log.d("TrailDetailScreen", "LaunchedEffect déclenché")
        Log.d("TrailDetailScreen", "  - trailId: $trailId")
        Log.d("TrailDetailScreen", "  - preloadedTrail: ${preloadedTrail?.name ?: "null"}")

        if (preloadedTrail != null) {
            Log.d("TrailDetailScreen", "Utilisation du preloadedTrail")
            detailViewModel.setTrail(preloadedTrail)
        } else {
            Log.d("TrailDetailScreen", "Pas de preloadedTrail, recherche Firestore")
            detailViewModel.fetchTrailById(trailId)
        }
    }

    val trail by detailViewModel.trail.collectAsState()
    val isLoading by detailViewModel.isLoading.collectAsState()

    Log.d("TrailDetailScreen", "Recomposition - trail: ${trail?.name ?: "null"}, isLoading: $isLoading")

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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Log.d("TrailDetailScreen", "Affichage du loading")
            }
        } else {
            trail?.let { t ->
                Log.d("TrailDetailScreen", "Affichage du trail: ${t.name}")

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // CARTE avec tracé
                    if (t.hasValidCoordinates()) {
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
                                        maxZoomLevel = 18.0
                                        minZoomLevel = 6.0

                                        val startPoint = t.getTrailStartPoint()
                                        startPoint?.let { point ->
                                            controller.setZoom(14.0)
                                            controller.setCenter(point)

                                            val marker = Marker(this).apply {
                                                position = point
                                                title = t.name
                                                icon = createDetailMarkerIcon(ctx, t.difficulty)
                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            }
                                            overlays.add(marker)

                                            val pathPoints = t.getPathAsGeoPoints()
                                            if (pathPoints.isNotEmpty()) {
                                                val polyline = Polyline().apply {
                                                    setPoints(pathPoints)
                                                    outlinePaint.color = when (t.difficulty) {
                                                        Difficulty.EASY -> android.graphics.Color.GREEN
                                                        Difficulty.MODERATE -> android.graphics.Color.rgb(255, 152, 0) // Orange
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
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Landscape,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Text("Carte non disponible", color = Color.Gray)
                            }
                        }
                    }

                    // Titre et difficulté
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

                    // Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatColumn(
                                    icon = Icons.Default.DirectionsWalk,
                                    label = "Distance",
                                    value = t.distance
                                )
                                StatColumn(
                                    icon = Icons.Default.Schedule,
                                    label = "Durée",
                                    value = t.duration
                                )

                                // Rating OU Dénivelé
                                if (t.source == "firestore" && t.rating > 0) {
                                    StatColumn(
                                        icon = Icons.Default.Star,
                                        label = "Note",
                                        value = "${t.rating}/5"
                                    )
                                } else if (t.elevation.isNotEmpty()) {
                                    StatColumn(
                                        icon = Icons.Default.TrendingUp,
                                        label = "Dénivelé",
                                        value = t.elevation.split("/").firstOrNull()?.trim() ?: t.elevation
                                    )
                                } else {
                                    StatColumn(
                                        icon = Icons.Default.Star,
                                        label = "Note",
                                        value = "N/A"
                                    )
                                }
                            }

                            if (t.source == "firestore" && t.reviewsCount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${t.reviewsCount} avis",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }

                    Divider()

                    // Description
                    Text(
                        text = "Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (t.description.isNotEmpty()) {
                        Text(
                            text = t.description,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text = "Aucune description disponible pour ce sentier.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    // Dénivelé complet si OSM
                    if (t.source == "osm" && t.elevation.isNotEmpty()) {
                        Divider()
                        Text(
                            text = "Informations",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        InfoRow("Dénivelé", t.elevation)
                        if (t.tags.isNotEmpty()) {
                            InfoRow("Type", t.tags.firstOrNull() ?: "path")
                            if (t.tags.size > 1) {
                                InfoRow("Surface", t.tags[1])
                            }
                        }
                    }

                    // Tags Firestore
                    if (t.tags.isNotEmpty() && t.source == "firestore") {
                        Text(
                            text = "Tags",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            t.tags.take(5).forEach { tag ->
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
                Log.e("TrailDetailScreen", "Affichage de 'Sentier introuvable'")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sentier introuvable", fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ID: $trailId", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Retour")
                        }
                    }
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = TrailGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private fun createDetailMarkerIcon(context: android.content.Context, difficulty: Difficulty): BitmapDrawable {
    val size = 60
    val height = 80

    val bitmap = Bitmap.createBitmap(size, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = when (difficulty) {
            Difficulty.EASY -> android.graphics.Color.GREEN
            Difficulty.MODERATE -> android.graphics.Color.rgb(255, 152, 0) // Orange
            Difficulty.HARD -> android.graphics.Color.RED
            Difficulty.EXPERT -> android.graphics.Color.BLACK
        }
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    val centerX = size / 2f
    val radius = (size / 2f) - 5f

    canvas.drawCircle(centerX, centerX, radius, paint)
    val path = android.graphics.Path().apply {
        moveTo(centerX, radius * 2 + 10f)
        lineTo(centerX - radius, radius)
        lineTo(centerX + radius, radius)
        close()
    }
    canvas.drawPath(path, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(centerX, centerX, radius, paint)

    return BitmapDrawable(context.resources, bitmap)
}