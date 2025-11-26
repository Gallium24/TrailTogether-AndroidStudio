package com.example.trailtogether_v01.ui.screens.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.trailtogether_v01.data.models.Event
import com.example.trailtogether_v01.data.models.Trail
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import com.example.trailtogether_v01.ui.theme.TrailGreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    trailId: String,
    trailName: String,
    trail: Trail?,
    onNavigateBack: () -> Unit
) {
    val repository = FirestoreRepository()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = Firebase.auth

    // États du formulaire
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()

    // ... (DatePicker et TimePicker inchangés) ...
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planifier une sortie") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... (Champs Date, Time, Description inchangés) ...
            Text("Quand souhaitez-vous partir ?", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.CalendarToday, "Date") } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                label = { Text("Heure") },
                readOnly = true,
                trailingIcon = { IconButton(onClick = { timePickerDialog.show() }) { Icon(Icons.Default.AccessTime, "Heure") } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Message aux participants") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedDate.isNotBlank() && selectedTime.isNotBlank()) {
                        isSubmitting = true
                        scope.launch {
                            val user = auth.currentUser

                            // 1. Si on a le trail (OSM), on le sauvegarde dans Firestore pour que tout le monde puisse voir la carte
                            if (trail != null) {
                                repository.saveTrail(trail)
                            }

                            // 2. On crée l'événement avec les infos complètes (distance, durée)
                            val newEvent = Event(
                                trailId = trailId,
                                trailName = trailName,
                                organizerId = user?.uid ?: "",
                                organizerName = user?.displayName ?: "Organisateur",
                                date = selectedDate,
                                time = selectedTime,
                                description = description,
                                distance = (trail?.distance ?: "").toString(),
                                duration = trail?.duration ?: "",
                                difficulty = trail?.difficulty ?: com.example.trailtogether_v01.data.models.Difficulty.EASY,
                                participantsCount = 1,
                                maxParticipants = 10
                            )
                            repository.createEvent(newEvent)
                            isSubmitting = false
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isSubmitting && selectedDate.isNotBlank() && selectedTime.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TrailGreen)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Text("Confirmer la sortie")
                }
            }
        }
    }
}