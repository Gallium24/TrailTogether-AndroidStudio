package com.example.trailtogether_v01.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.FeedViewModel

/**
 * CreatePostScreen est la composante de l'écran de création de publication.
 * @param onNavigateBack Une fonction lambda appelée lorsque l'utilisateur clique sur le bouton "Retour".
 * @param feedViewModel Le ViewModel de la publication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    feedViewModel: FeedViewModel = viewModel()
) {
    var content by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }

    // États pour le Dropdown
    val userTrails by feedViewModel.userHistoryTrails.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var selectedTrailId by remember { mutableStateOf("") }
    var selectedTrailName by remember { mutableStateOf("") }

    // Charger l'historique au lancement
    LaunchedEffect(Unit) {
        feedViewModel.loadUserTrailsForPost()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer une publication") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (isPublishing || content.isBlank() || selectedTrailId.isBlank()) return@Button

                            isPublishing = true
                            feedViewModel.createPost(
                                trailId = selectedTrailId,
                                trailName = selectedTrailName,
                                content = content,
                                onSuccess = { onNavigateBack() },
                                onFailure = { isPublishing = false }
                            )
                        },
                        // On désactive le bouton si aucun sentier n'est sélectionné
                        enabled = !isPublishing && content.isNotBlank() && selectedTrailId.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Publier")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- SÉLECTEUR DE SENTIER ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = if (selectedTrailName.isEmpty()) "Choisir une randonnée de votre historique" else selectedTrailName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lieu de la randonnée") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (userTrails.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Aucun historique disponible") },
                            onClick = { expanded = false }
                        )
                    } else {
                        userTrails.forEach { trail ->
                            DropdownMenuItem(
                                text = { Text(trail.name) },
                                onClick = {
                                    selectedTrailId = trail.id
                                    selectedTrailName = trail.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Partagez votre expérience...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}