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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.trailtogether_v01.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.HomeViewModel
import com.example.trailtogether_v01.ui.components.DifficultyFilterChips
import com.example.trailtogether_v01.ui.components.TrailCard
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import com.example.trailtogether_v01.ui.theme.TrailGreen



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTrailDetail: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val filteredTrails = homeViewModel.getFilteredTrails()
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .background(TrailGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🗺️ CARTE INTERACTIVE\nSentiers et parcours",
                color = Color.DarkGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
