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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailtogether_v01.data.repository.MockRepository
import com.example.trailtogether_v01.ui.components.EventCard
import com.example.trailtogether_v01.ui.theme.BackgroundBeige

@Composable
fun CalendarScreen(
    onNavigateToEventDetail: (String) -> Unit
) {
    val repository = MockRepository()
    var selectedDate by remember { mutableStateOf(15) }
    var selectedMonth by remember { mutableStateOf("Janvier") }

    // Mock events
    val events = remember {
        listOf(
            com.example.trailtogether_v01.data.models.Event(
                id = "event_1",
                trailId = "trail_1",
                trailName = "Randonnée des Sapins",
                organizerId = "user_2",
                organizerName = "Marie Martin",
                date = "2025-01-15",
                time = "13:00",
                duration = "4h 30min",
                distance = "12.5 km",
                difficulty = com.example.trailtogether_v01.data.models.Difficulty.MODERATE,
                participantsCount = 8,
                maxParticipants = 15
            ),
            com.example.trailtogether_v01.data.models.Event(
                id = "event_2",
                trailId = "trail_2",
                trailName = "Randonnée des Pins",
                organizerId = "user_3",
                organizerName = "Pierre Dubois",
                date = "2025-01-15",
                time = "09:00",
                duration = "8h 15min",
                distance = "30.2 km",
                difficulty = com.example.trailtogether_v01.data.models.Difficulty.HARD,
                participantsCount = 5,
                maxParticipants = 10
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBeige)
            .padding(16.dp)
    ) {
        // Calendar card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Mois précédent",
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Red, CircleShape)
                                .padding(4.dp)
                        )
                    }
                    Text(
                        text = selectedMonth,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Mois suivant",
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Red, CircleShape)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of week
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(250.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(31) { index ->
                        val day = index + 1
                        val isSelected = day == selectedDate
                        val hasEvent = day == 15 || day == 20 || day == 28

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
                                .clickable { selectedDate = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = if (hasEvent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Events list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Évènements du $selectedDate $selectedMonth",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter événement",
                    tint = com.example.trailtogether_v01.ui.theme.TrailGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                EventCard(
                    event = event,
                    onClick = { onNavigateToEventDetail(event.id) }
                )
            }
        }
    }
}
