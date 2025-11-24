package com.example.trailtogether_v01.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel() // Inject ViewModel
) {
    // Collect the state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // --- Section: Affichage & Unités ---
            SettingsSectionTitle("Général")

            SwitchSettingItem(
                title = "Unités Impériales (Miles)",
                subtitle = "Afficher les distances en miles",
                icon = Icons.Default.Straighten,
                checked = uiState.useImperialUnits,
                onCheckedChange = { viewModel.toggleImperialUnits(it) }
            )

            // --- Section: Carte ---
            SettingsSectionTitle("Carte & Navigation")

            Text(
                text = "Rayon de recherche par défaut: ${uiState.defaultRadius.toInt()} km",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = uiState.defaultRadius,
                onValueChange = { viewModel.updateDefaultRadius(it) },
                valueRange = 1f..50f,
                steps = 49,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Selection for Map Style
            SettingClickableItem(
                title = "Style de carte",
                subtitle = "Actuel: ${uiState.mapStyle}",
                icon = Icons.Default.Map,
                onClick = { viewModel.toggleMapStyle() }
            )

            SettingClickableItem(
                title = "Vider le cache de la carte",
                subtitle = "Libérer de l'espace de stockage",
                icon = Icons.Default.Delete,
                onClick = { viewModel.clearMapCache(context) }
            )

            // --- Section: Compte ---
            SettingsSectionTitle("Compte")

            SettingClickableItem(
                title = "Se déconnecter",
                subtitle = "Fermer la session actuelle",
                icon = Icons.Default.AccountCircle,
                onClick = onSignOut,
                textColor = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Version 1.0.0",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Helper Composables (Keep these as they were) ---

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingClickableItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = textColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, color = textColor)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}