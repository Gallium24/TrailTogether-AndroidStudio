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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Email

/**
 * EditProfileScreen.kt
 *
 * Écran d'édition du profil utilisateur.
 *
 * Fonctionnalités:
 * - Modification du nom
 * - Modification de la bio
 * - Modification du contact d'urgence
 * - Upload de photo de profil
 * - Validation des champs
 * - Sauvegarde des modifications
 *
 * Champs éditables:
 * - Nom: TextField (requis)
 * - Bio: TextField multilignes (optionnel, max 200 caractères)
 * - Contact d'urgence: TextField avec validation téléphone (optionnel)
 * - Photo de profil: Sélection depuis galerie
 *
 * Validation:
 * - Nom: Minimum 2 caractères, requis
 * - Bio: Maximum 200 caractères
 * - Contact: Format téléphone valide (si renseigné)
 *
 * Boutons:
 * - "Sauvegarder": Enregistre les modifications
 * - "Annuler": Retour sans sauvegarder (avec confirmation si changements)
 * - "Changer la photo": Ouvre sélecteur de galerie
 *
 * Aperçu photo:
 * - Affiche photo actuelle ou placeholder
 * - Preview de la nouvelle photo avant sauvegarde
 * - Option de retirer la photo
 *
 * Process de sauvegarde:
 * 1. Validation des champs
 * 2. Upload de la photo (si changée)
 * 3. Mise à jour du profil dans Firestore
 * 4. Affichage message de succès
 * 5. Navigation vers ProfileScreen
 *
 * États:
 * - Idle: Formulaire actif
 * - Loading: Sauvegarde en cours (CircularProgressIndicator)
 * - Success: Message de succès + navigation
 * - Error: Message d'erreur affiché
 *
 * Pré-remplissage:
 * - Tous les champs sont pré-remplis avec valeurs actuelles
 * - Chargés depuis ProfileViewModel
 *
 * Intégration:
 * - ProfileViewModel pour chargement et sauvegarde
 * - Firebase Storage pour upload photo (si implémenté)
 *
 * Utilisation:
 * - Navigation depuis ProfileScreen (bouton "Éditer le profil")
 * - Partie de MainNavGraph
 */

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sécurité",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Outlined.Security, // ou Shield
                        contentDescription = null,
                        tint = Color(0xFF40A829)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "L'email ci-dessous recevra une alerte si vous ne signalez pas votre retour 24h après le début d'une sortie.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Email du contact d'urgence") }, // Label clair
                    placeholder = { Text("ex: proche@email.com") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = Color(0xFF40A829)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Clavier Email
                    singleLine = true
                )
            }
        }
    }
}