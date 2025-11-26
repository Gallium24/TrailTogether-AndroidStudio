package com.example.trailtogether_v01.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.trailtogether_v01.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.HomeViewModel
import com.example.trailtogether_v01.ui.components.DifficultyFilterChips
import com.example.trailtogether_v01.ui.components.TrailCard
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.compose.ui.draw.clip
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.compose.ui.graphics.Color
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import android.util.Log
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.trailtogether_v01.data.models.Difficulty
import kotlinx.coroutines.launch
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap
import com.example.trailtogether_v01.data.viewmodel.SettingsViewModel
import com.example.trailtogether_v01.utils.FormatUtils
import com.example.trailtogether_v01.utils.MapUtils.OpenTopoMapSource
import com.example.trailtogether_v01.utils.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * HomeScreen.kt
 *
 * Écran principal de l'application affichant la carte interactive et la liste des sentiers.
 *
 * Fonctionnalités principales:
 * - Carte interactive OSMDroid avec marqueurs de sentiers
 * - Liste des sentiers avec filtres et recherche
 * - Géolocalisation et centrage sur position utilisateur
 * - Filtrage par difficulté (EASY, MODERATE, HARD, EXPERT)
 * - Recherche textuelle (nom et localisation)
 * - Ajustement du rayon de recherche (1-100 km)
 * - Affichage des tracés de sentiers sur la carte
 * - Sélection d'un sentier (card popup sur la carte)
 *
 * Composants principaux:
 * 1. Header:
 *    - Titre "Sentiers"
 *    - Icônes: Calendrier, Paramètres
 *
 * 2. Barre de recherche:
 *    - TextField avec icône de recherche
 *    - Recherche en temps réel
 *    - Placeholder: "Rechercher un sentier..."
 *
 * 3. Filters:
 *    - DifficultyFilterChips (EASY, MODERATE, HARD, EXPERT)
 *    - Slider de rayon (1-100 km)
 *
 * 4. Carte OSMDroid:
 *    - Marqueurs pour chaque sentier avec couleur selon difficulté
 *    - Polylines pour tracés (si activé dans paramètres)
 *    - Zoom et pan interactifs
 *    - Centrage sur position utilisateur
 *    - Clic sur marqueur → Affiche card du sentier
 *
 * 5. Liste des sentiers:
 *    - LazyColumn de TrailCard
 *    - Filtrée selon recherche et difficulté
 *    - Clic sur carte → Anime vers le sentier sur la carte
 *    - Clic sur card → Navigate vers TrailDetailScreen
 *
 * 6. Card de sentier sélectionné (sur la carte):
 *    - Nom, distance, durée, difficulté
 *    - Dénivelé (si disponible)
 *    - Bouton fermer
 *    - Bouton "Voir détails"
 *
 * Marqueurs personnalisés:
 * - Couleur selon difficulté:
 *   - EASY: Vert
 *   - MODERATE: Orange
 *   - HARD: Orange foncé
 *   - EXPERT: Rouge
 *
 * États:
 * - isLoadingMapTrails: Indicateur de chargement sentiers OSM
 * - errorMessage: Message d'erreur si échec de chargement
 * - selectedTrail: Sentier sélectionné sur la carte
 *
 * Optimisations:
 * - Cache des sentiers par position et rayon
 * - Chargement initial sans géocodage (rapide)
 * - Limite de 20 sentiers par zone
 * - Filtrage côté client pour réactivité
 *
 * Intégration:
 * - HomeViewModel pour toute la logique
 * - SettingsManager pour préférences (unités, rayon, style carte)
 * - LocationService pour position GPS
 *
 * Utilisation:
 * - Route par défaut de MainNavGraph
 * - Premier écran après connexion
 * - Accessible via BottomNavBar (icône Home/Map)
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTrailDetail: (String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
) {
    val coroutineScope = rememberCoroutineScope()

    val filteredTrails by homeViewModel.filteredTrails.collectAsState()
    val allTrails by homeViewModel.allTrails.collectAsState()
    val selectedDifficulty by homeViewModel.selectedDifficulty.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val isLoadingMapTrails by homeViewModel.isLoadingMapTrails.collectAsState()
    val selectedTrail by homeViewModel.selectedTrail.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    val trailStats by homeViewModel.trailStats.collectAsState()

    val savedMapCenter by homeViewModel.mapCenter.collectAsState()
    val savedMapZoom by homeViewModel.mapZoom.collectAsState()
    val showTrailPath by SettingsManager.showTrailPath.collectAsState(initial = true)
    // === Settings from SettingsManager ===
    val useImperial by SettingsManager.useImperialUnits.collectAsState(initial = false)
    val mapStyle by SettingsManager.mapStyle.collectAsState(initial = "Mapnik")
    val isShowTrailPath by SettingsManager.showTrailPath.collectAsState(initial = true)
    val defaultRadiusKm by SettingsManager.defaultRadiusKm.collectAsState(initial = 5f)

    val context = LocalContext.current
    var showRadiusDialog by remember { mutableStateOf(false) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var tempRadiusKm by rememberSaveable { mutableFloatStateOf(defaultRadiusKm) }

    LaunchedEffect(Unit) {
        val savedRadiusKm = SettingsManager.defaultRadiusKm.first() // Lit la vraie valeur persistante
        tempRadiusKm = savedRadiusKm * 1000f // Convertit en mètres
        homeViewModel.loadMapTrails(savedMapCenter, tempRadiusKm)
    }

    LaunchedEffect(tempRadiusKm) {
        homeViewModel.loadMapTrails(savedMapCenter, tempRadiusKm)
    }

    LaunchedEffect(mapStyle) {
        mapViewRef?.let { map ->
            map.setTileSource(if (mapStyle == "OpenTopoMap") OpenTopoMapSource else TileSourceFactory.MAPNIK)
            map.invalidate()
        }
    }

    val mapTrails = remember(allTrails, selectedDifficulty) {
        allTrails.filter {
            it.source == "osm" &&
                    it.hasValidCoordinates() &&
                    (selectedDifficulty == null || it.difficulty == selectedDifficulty)
        }
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Image(
                painter = painterResource(id = R.drawable.trailtogether_logo),
                contentDescription = "Logo TrailTogether",
                modifier = Modifier.height(30.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToCalendar) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendrier",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { showRadiusDialog = true }) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        val radiusText = if (useImperial) {
                            "${(tempRadiusKm * 0.000621371).toInt()} mi"
                        } else {
                            "${(tempRadiusKm / 1000).toInt()} km"
                        }
                        Text(radiusText, fontSize = 10.sp)
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }

        // Message d'erreur
        errorMessage?.let { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = msg, modifier = Modifier.weight(1f), fontSize = 13.sp)
                    IconButton(onClick = { homeViewModel.clearErrorMessage() }) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { homeViewModel.setSearchQuery(it) },
            placeholder = { Text("Rechercher...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Difficulté", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tracés", fontSize = 12.sp, modifier = Modifier.padding(end = 10.dp))
                    Switch(
                        checked = showTrailPath,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                SettingsManager.setShowTrailPath(checked)
                            }
                        },
                        modifier = Modifier.size(40.dp, 24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            DifficultyFilterChips(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = { homeViewModel.setDifficultyFilter(it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        maxZoomLevel = 18.0
                        minZoomLevel = 6.0

                        controller.setZoom(savedMapZoom)
                        controller.setCenter(savedMapCenter)

                        mapViewRef = this

                        var scrollJob: kotlinx.coroutines.Job? = null
                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                scrollJob?.cancel()
                                scrollJob = coroutineScope.launch {
                                    kotlinx.coroutines.delay(2000) // 2 secondes
                                    val newCenter = mapCenter as GeoPoint
                                    val newZoom = zoomLevelDouble

                                    homeViewModel.updateMapPosition(newCenter, newZoom)

                                    if (newCenter.distanceToAsDouble(savedMapCenter) > 2000) {
                                        homeViewModel.loadMapTrails(newCenter, tempRadiusKm)
                                    }
                                }
                                return true
                            }
                            override fun onZoom(event: ZoomEvent?): Boolean {
                                val newZoom = zoomLevelDouble
                                val newCenter = mapCenter as GeoPoint
                                homeViewModel.updateMapPosition(newCenter, newZoom)
                                return true
                            }
                        })
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    mapTrails.forEach { trail ->
                        val startPoint = trail.getTrailStartPoint()
                        val pathPoints = trail.getPathAsGeoPoints()

                        if (startPoint != null) {
                            // Tracé
                            if (showTrailPath && pathPoints.isNotEmpty()) {
                                val polyline = Polyline().apply {
                                    setPoints(pathPoints)
                                    outlinePaint.color = getDifficultyColor(trail.difficulty)
                                    outlinePaint.strokeWidth = 8f
                                    outlinePaint.alpha = 180
                                }
                                mapView.overlays.add(polyline)
                            }

                            // Marker
                            val isSelected = selectedTrail?.id == trail.id
                            val marker = Marker(mapView).apply {
                                position = startPoint
                                title = trail.name
                                snippet = "${trail.difficulty.toDisplayString()} • ${trail.distance}"

                                icon = createMarkerIcon(context, trail.difficulty, isSelected)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                                setOnMarkerClickListener { _, _ ->
                                    homeViewModel.selectTrail(trail.id)
                                    mapView.controller.animateTo(startPoint, 15.0, 500L)
                                    true
                                }
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    mapView.invalidate()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            if (isLoadingMapTrails) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        selectedTrail?.let { trail ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trail.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Chip(FormatUtils.formatDistance(trail.distance, useImperial))
                                Chip(trail.duration)
                                Chip(trail.difficulty.toDisplayString())
                            }
                            if (trail.elevation.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Dénivelé: ${trail.elevation}", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { homeViewModel.clearSelectedTrail() }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onNavigateToTrailDetail(trail.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voir les détails complets")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Liste
        Text(
            "Sentiers disponibles (${filteredTrails.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredTrails) { trail ->
                TrailCard(
                    trail = trail,
                    onClick = {
                        trail.getTrailStartPoint()?.let { point ->
                            mapViewRef?.controller?.animateTo(point, 15.0, 500L)
                            homeViewModel.selectTrail(trail.id)
                        }
                        onNavigateToTrailDetail(trail.id)
                    },
                    useImperialUnits = useImperial
                )
            }
        }
    }

    // Dialog rayon
    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = { showRadiusDialog = false },
            title = { Text("Rayon de recherche") },
            text = {
                Column {
                    Text("${(tempRadiusKm / 1000).toInt()} km")
                    Slider(
                        value = tempRadiusKm,
                        onValueChange = { tempRadiusKm = it },
                        valueRange = 1000f..20000f, // Max 20km pour éviter lag
                        steps = 18
                    )
                    Text("⚠️ Plus grand = plus de temps", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    homeViewModel.refreshTrailsWithRadius(savedMapCenter, tempRadiusKm)
                    showRadiusDialog = false
                }) { Text("Rechercher") }
            },
            dismissButton = {
                TextButton(onClick = { showRadiusDialog = false }) { Text("Annuler") }
            }
        )
    }

    LaunchedEffect(Unit) {
        homeViewModel.loadMapTrails(savedMapCenter, tempRadiusKm)
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
private fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
    }
}

private fun getDifficultyColor(difficulty: Difficulty): Int {
    return when (difficulty) {
        Difficulty.EASY -> android.graphics.Color.GREEN
        Difficulty.MODERATE -> android.graphics.Color.rgb(255, 152, 0) // Orange
        Difficulty.HARD -> android.graphics.Color.RED
        Difficulty.EXPERT -> android.graphics.Color.BLACK
    }
}

private fun createMarkerIcon(
    context: android.content.Context,
    difficulty: Difficulty,
    isSelected: Boolean
): BitmapDrawable {
    val size = if (isSelected) 70 else 50 // Plus gros si sélectionné
    val height = if (isSelected) 90 else 70

    val bitmap = createBitmap(size, height)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = getDifficultyColor(difficulty)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    val centerX = size / 2f
    val radius = (size / 2f) - 5f

    canvas.drawCircle(centerX, centerX, radius, paint)
    val path = android.graphics.Path().apply {
        moveTo(centerX, radius * 2 + 5f)
        lineTo(centerX - radius, radius)
        lineTo(centerX + radius, radius)
        close()
    }
    canvas.drawPath(path, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = if (isSelected) 4f else 3f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(centerX, centerX, radius, paint)

    return bitmap.toDrawable(context.resources)
}

fun formatDistance(meters: Float, useImperial: Boolean): String {
    return if (useImperial) {
        val miles = meters * 0.000621371
        "%.1f mi".format(miles)
    } else {
        val km = meters / 1000
        "%.1f km".format(km)
    }
}