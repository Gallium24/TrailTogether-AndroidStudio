package com.example.trailtogether_v01.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.CalendarViewModel
import com.example.trailtogether_v01.ui.components.EventCard
import com.example.trailtogether_v01.ui.theme.BackgroundBeige
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * CalendarScreen.kt
 *
 * Écran du calendrier affichant les événements de randonnée.
 *
 * Fonctionnalités:
 * - Affichage du mois courant avec navigation mois précédent/suivant
 * - Liste des événements du mois sélectionné
 * - Bouton pour créer un nouvel événement
 * - Filtrage automatique des événements par mois
 *
 * Layout:
 * - Header: Mois/Année avec flèches navigation
 * - Liste: EventCard pour chaque événement du mois
 * - FloatingActionButton: Création d'événement
 *
 * Navigation:
 * - Clic sur EventCard → EventDetailScreen
 * - Clic sur FAB → HomeScreen (pour sélectionner un sentier)
 * - Flèches → Mois précédent/suivant
 *
 * Filtrage:
 * - Événements filtrés par mois et année
 * - Tri par date (plus proches en premier)
 * - Message si aucun événement dans le mois
 *
 * États:
 * - isLoading: Affiche CircularProgressIndicator
 * - Liste vide: "Aucun événement ce mois-ci"
 * - Avec événements: Liste scrollable
 *
 * Intégration:
 * - CalendarViewModel pour logique et données
 * - Flow réactif des événements
 * - Mises à jour automatiques
 *
 * Design:
 * - LazyColumn pour la liste
 * - Espacement entre cards (12.dp)
 * - FAB en bas à droite
 * - Couleurs du thème
 *
 * Utilisation:
 * - Accessible via BottomNavBar (icône Calendar)
 * - Partie de MainNavGraph
 */

@Composable
fun CalendarScreen(
    onNavigateToEventDetail: (String) -> Unit,
    viewModel: CalendarViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val eventsForDate by viewModel.eventsForSelectedDate.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    // Gestion simple du changement de mois (pour l'affichage)
    val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() }
    val year = selectedDate.year

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // --- Calendrier ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Mois
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onDateSelected(selectedDate.minusMonths(1)) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Précédent", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        text = "$monthName $year",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { viewModel.onDateSelected(selectedDate.plusMonths(1)) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Suivant", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Jours de la semaine
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa").forEach { day ->
                        Text(text = day, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grille des jours
                val daysInMonth = selectedDate.lengthOfMonth()
                val firstDayOfMonth = selectedDate.withDayOfMonth(1).dayOfWeek.value % 7 // Dimanche = 0 ou 7 selon config

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp)
                ) {
                    // Espaces vides avant le 1er du mois
                    items(firstDayOfMonth) { Spacer(modifier = Modifier.size(40.dp)) }

                    // Jours du mois
                    items(daysInMonth) { index ->
                        val day = index + 1
                        val currentDate = selectedDate.withDayOfMonth(day)
                        val isSelected = currentDate == selectedDate
                        val dateString = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val hasEvent = allEvents.any { it.date == dateString }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when {
                                        isSelected -> Color.Red
                                        hasEvent -> Color(0xFFFF6B6B).copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    },
                                    CircleShape
                                )
                                .clickable { viewModel.onDateSelected(currentDate) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (hasEvent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Liste des événements ---
        Text(
            text = "Événements du ${selectedDate.dayOfMonth} $monthName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (eventsForDate.isEmpty()) {
            Text("Aucun événement prévu ce jour-là.", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(eventsForDate) { event ->
                    EventCard(event = event, onClick = { onNavigateToEventDetail(event.id) })
                }
            }
        }
    }
}