package com.example.trailtogether_v01.ui.screens.profile

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by profileViewModel.user.collectAsState()
    var name by remember { mutableStateOf(user?.name ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var emergencyContact by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.let {
            name = it.name ?: ""
            bio = it.bio ?: ""
            emergencyContact = it.emergencyContact ?: ""
            emergencyPhone = it.emergencyPhone ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // On sauvegarde les nouvelles valeurs
                            profileViewModel.updateUserProfile(name, bio, emergencyContact, emergencyPhone)
                            // Et on retourne à l'écran précédent
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Enregistrer")
                    }
                }
            )
        }
    ) { paddingValues ->
        // On affiche un indicateur de chargement tant que l'utilisateur n'est pas chargé
        if (profileViewModel.isLoading.collectAsState().value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Profil ---
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Aligne l'icône et le texte sur la même ligne
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Ajoute un espace de 8.dp entre eux
                ) {
                    Text(
                        text = "Votre profil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Information contact d'urgence ---
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Aligne l'icône et le texte sur la même ligne
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Ajoute un espace de 8.dp entre eux
                ) {
                    Text(
                        text = "Contact d'urgence",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error // Couleur rouge
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Ce contact sera alerté si vous ne signalez pas la fin de votre randonnée dans les 24h suivant l'heure prévue.",
                            fontSize = 14.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Nom du contact") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = { emergencyPhone = it },
                    label = { Text("Numéro de téléphone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}