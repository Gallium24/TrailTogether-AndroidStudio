package com.example.trailtogether_v01.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailtogether_v01.utils.SettingsManager
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.modules.SqlTileWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDarkMode by SettingsManager.isDarkMode.collectAsState(initial = false)
    val useImperial by SettingsManager.useImperialUnits.collectAsState(initial = false)
    val defaultRadiusKm by SettingsManager.defaultRadiusKm.collectAsState(initial = 5f)
    val mapStyle by SettingsManager.mapStyle.collectAsState(initial = "Mapnik")
    val showTrailPath by SettingsManager.showTrailPath.collectAsState(initial = true)

    val radiusDisplayText = if (useImperial) {
        val miles = defaultRadiusKm * 0.621371f
        String.format("%.1f mi", miles)
    } else {
        "${defaultRadiusKm.toInt()} km"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.systemBars) // Supprime le bandeau noir en bas
    ) {
        // TopBar manuelle
        CenterAlignedTopAppBar(
            title = { Text("Paramètres") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // === Général ===
        SettingsSectionTitle("Général")

        SwitchSettingItem(
            title = "Mode sombre",
            subtitle = "Activer le thème sombre",
            icon = Icons.Default.DarkMode,
            checked = isDarkMode,
            onCheckedChange = { coroutineScope.launch { SettingsManager.setDarkMode(it) } }
        )

        SwitchSettingItem(
            title = "Unités impériales",
            subtitle = "Afficher les distances en miles et pieds",
            icon = Icons.Default.Straighten,
            checked = useImperial,
            onCheckedChange = { coroutineScope.launch { SettingsManager.setImperialUnits(it) } }
        )

        SwitchSettingItem(
            title = "Afficher les tracés sur la carte",
            subtitle = "Voir le chemin complet des sentiers",
            icon = Icons.Default.Terrain,
            checked = showTrailPath,
            onCheckedChange = { coroutineScope.launch { SettingsManager.setShowTrailPath(it) } }
        )

        // === Carte & Navigation ===
        SettingsSectionTitle("Carte & Navigation")

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Rayon de recherche par défaut", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = radiusDisplayText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = defaultRadiusKm,
                onValueChange = { coroutineScope.launch { SettingsManager.setDefaultRadius(it) } },
                valueRange = 1f..50f,
                steps = 49,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        HorizontalDivider()

        SettingClickableItem(
            title = "Style de carte",
            subtitle = "Actuel : ${if (mapStyle == "OpenTopoMap") "OpenTopoMap (topographique)" else "Mapnik (classique)"}",
            icon = Icons.Default.Map,
            onClick = {
                coroutineScope.launch {
                    val newStyle = if (mapStyle == "Mapnik") "OpenTopoMap" else "Mapnik"
                    SettingsManager.setMapStyle(newStyle)

                    // Vide le cache des tuiles à chaque changement
                    try {
                        val sqlTileWriter = SqlTileWriter()
                        sqlTileWriter.purgeCache()
                        sqlTileWriter.onDetach()
                        context.cacheDir.deleteRecursively()
                        Toast.makeText(context, "Style changé et cache vidé", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erreur lors du nettoyage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        SettingClickableItem(
            title = "Vider le cache de la carte",
            subtitle = "Supprime les tuiles téléchargées (~50-500 Mo)",
            icon = Icons.Default.DeleteSweep,
            onClick = {
                coroutineScope.launch {
                    try {
                        val sqlTileWriter = SqlTileWriter()
                        sqlTileWriter.purgeCache()
                        sqlTileWriter.onDetach()
                        context.cacheDir.deleteRecursively()
                        Toast.makeText(context, "Cache vidé avec succès", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erreur lors du nettoyage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        // === Compte ===
        SettingsSectionTitle("Compte")

        SettingClickableItem(
            title = "Se déconnecter",
            subtitle = "Vous devrez vous reconnecter",
            icon = Icons.Default.Logout,
            textColor = MaterialTheme.colorScheme.error,
            onClick = onSignOut
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "TrailTogether v1.0.0",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// === Composables réutilisables ===
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
            Text(title, fontSize = 16.sp)
            subtitle?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
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
            Text(title, fontSize = 16.sp, color = textColor)
            subtitle?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}