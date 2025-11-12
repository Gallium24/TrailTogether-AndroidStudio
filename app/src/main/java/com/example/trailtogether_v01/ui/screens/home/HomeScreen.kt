package com.example.trailtogether_v01.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.core.content.ContextCompat
import org.osmdroid.tileprovider.tilesource.TileSourceFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTrailDetail: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val filteredTrails by homeViewModel.filteredTrails.collectAsState()
    val selectedDifficulty by homeViewModel.selectedDifficulty.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.trailtogether_logo),
                contentDescription = "Logo TrailTogether",
                modifier = Modifier.height(30.dp) // Ajustez la hauteur selon vos préférences
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { homeViewModel.setSearchQuery(it) },
            placeholder = { Text("Rechercher un sentier...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filters
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Difficulté",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            DifficultyFilterChips(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = { homeViewModel.setDifficultyFilter(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map placeholder
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//                .padding(horizontal = 16.dp)
//                .background(TrailGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "🗺️ CARTE INTERACTIVE\nSentiers et parcours",
//                color = Color.DarkGray,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Bold
//            )
//        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK) // OpenStreetMap tiles
                        setMultiTouchControls(true)
                        maxZoomLevel = 20.0
                        minZoomLevel = 5.0

                        // Center on Chamonix, France (trail hub)
                        controller.setCenter(GeoPoint(45.924, 6.868))
                        controller.setZoom(10.0)
                    }
                },
                update = { mapView ->
                    // Clear old markers
                    mapView.overlays.removeAll { it is Marker }

                    // Add trail markers
                    filteredTrails.forEach { trail ->
                        trail.latitude.let { lat ->
                            trail.longitude.let { lng ->
                                val geoPoint = GeoPoint(lat, lng)
                                val marker = Marker(mapView).apply {
                                    position = geoPoint
                                    title = trail.name
                                    snippet = trail.difficulty.toString()
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                                    //Optional: custom icon
                                    icon = ContextCompat.getDrawable(mapView.context, R.drawable.rc_trail_marker)

                                    setOnMarkerClickListener { _, _ ->
                                        onNavigateToTrailDetail(trail.id)
                                        true
                                    }
                                }
                                mapView.overlays.add(marker)
                            }
                        }
                    }

                    // Refresh map
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trails list
        Text(
            text = "Sentiers disponibles (${filteredTrails.size})",
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
                    onClick = { onNavigateToTrailDetail(trail.id) }
                )
            }
        }
    }
}