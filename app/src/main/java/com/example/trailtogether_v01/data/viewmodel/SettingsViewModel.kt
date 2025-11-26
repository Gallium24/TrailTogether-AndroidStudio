package com.example.trailtogether_v01.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.utils.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.modules.SqlTileWriter
import android.widget.Toast

/**
 * SettingsViewModel.kt
 *
 * Gère les paramètres de l'application via SettingsManager.
 *
 * Fonctionnalités:
 * - Mode sombre/clair
 * - Unités de mesure (métriques/impériales)
 * - Rayon de recherche des sentiers (1-100 km)
 * - Style de carte (Mapnik, OpenTopoMap, etc.)
 * - Affichage des tracés de sentiers
 * - Contact d'urgence
 *
 * StateFlows exposés:
 * - isDarkMode: État du mode sombre
 * - useImperialUnits: Unités impériales activées
 * - defaultRadiusKm: Rayon de recherche par défaut
 * - mapStyle: Style de carte sélectionné
 * - showTrailPath: Afficher tracés des sentiers
 *
 * Méthodes:
 * - toggleDarkMode(): Bascule le mode sombre
 * - setImperialUnits(enabled): Active/désactive unités impériales
 * - setDefaultRadius(radius): Définit le rayon de recherche
 * - setMapStyle(style): Change le style de carte
 * - toggleTrailPath(): Bascule affichage des tracés
 *
 * Persistence:
 * - Utilise SettingsManager (DataStore Preferences)
 * - Paramètres sauvegardés localement
 * - Chargés automatiquement au démarrage
 * - Survivent à la fermeture de l'app
 *
 * Impact des paramètres:
 * - Mode sombre: Appliqué via Theme.kt
 * - Unités impériales: Conversion km → miles dans FormatUtils
 * - Rayon recherche: Utilisé par HomeViewModel pour loadMapTrails()
 * - Style carte: Appliqué dans HomeScreen (TileSource)
 * - Tracés: Affichage des Polylines sur la carte
 *
 * Utilisation:
 * - Utilisé par SettingsScreen
 * - États observés par divers composants (Theme, HomeScreen, TrailCard)
 */

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val useImperialUnits: Boolean = false,
    val defaultRadiusKm: Float = 5f,
    val mapStyle: String = "Mapnik",
    val showTrailPath: Boolean = true,
    val isLoading: Boolean = true
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                SettingsManager.isDarkMode,
                SettingsManager.useImperialUnits,
                SettingsManager.defaultRadiusKm,
                SettingsManager.mapStyle,
                SettingsManager.showTrailPath
            ) { dark, imperial, radius, style, showPath ->
                SettingsUiState(
                    isDarkMode = dark,
                    useImperialUnits = imperial,
                    defaultRadiusKm = radius,
                    mapStyle = style,
                    showTrailPath = showPath,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            SettingsManager.setDarkMode(!_uiState.value.isDarkMode)
        }
    }

    fun toggleImperialUnits() {
        viewModelScope.launch {
            SettingsManager.setImperialUnits(!_uiState.value.useImperialUnits)
        }
    }

    fun updateDefaultRadius(radius: Float) {
        viewModelScope.launch {
            SettingsManager.setDefaultRadius(radius)
        }
    }

    fun toggleMapStyle() {
        viewModelScope.launch {
            val newStyle = if (_uiState.value.mapStyle == "Mapnik") "OpenTopoMap" else "Mapnik"
            SettingsManager.setMapStyle(newStyle)
        }
    }

    fun toggleShowTrailPath() {
        viewModelScope.launch {
            SettingsManager.setShowTrailPath(!_uiState.value.showTrailPath)
        }
    }

    fun clearMapCache(context: Context) {
        viewModelScope.launch {
            try {
                val sqlTileWriter = SqlTileWriter()
                val success = sqlTileWriter.purgeCache()
                sqlTileWriter.onDetach()
                context.cacheDir.deleteRecursively()

                Toast.makeText(
                    context,
                    if (success) "Cache de la carte vidé avec succès" else "Cache partiellement vidé",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur lors du nettoyage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}