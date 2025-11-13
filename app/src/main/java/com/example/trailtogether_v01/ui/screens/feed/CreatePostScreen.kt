package com.example.trailtogether_v01.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    feedViewModel: FeedViewModel = viewModel()
) {
    var content by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    val selectedTrailId = "trail_1" // TODO: Remplacer par une vraie sélection

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
                            // On ne fait rien si la publication est déjà en cours ou si le contenu est vide
                            if (isPublishing || content.isBlank()) return@Button

                            // On passe l'état à "en cours de publication" pour désactiver le bouton
                            isPublishing = true

                            // On appelle la fonction du FeedViewModel
                            feedViewModel.createPost(
                                trailId = selectedTrailId,
                                content = content,
                                onSuccess = {
                                    // En cas de succès, on ferme l'écran
                                    onNavigateBack()
                                },
                                onFailure = {
                                    // En cas d'échec, on réactive le bouton pour que l'utilisateur puisse réessayer
                                    isPublishing = false
                                    // Idéalement, afficher un message d'erreur ici (Snackbar, etc.)
                                }
                            )
                        },
                        // Le bouton est activé uniquement si 'isPublishing' est false ET le contenu n'est pas vide.
                        enabled = !isPublishing && content.isNotBlank(),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Affiche une roue de chargement pendant la publication
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
                .padding(16.dp)
        ) {
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