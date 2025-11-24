package com.example.trailtogether_v01.data.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.osmdroid.tileprovider.modules.SqlTileWriter

// 1. Define a Data Class to hold the state
data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val useImperialUnits: Boolean = false,
    val defaultRadius: Float = 5f, // km
    val mapStyle: String = "Mapnik"
)

// 2. Create the ViewModel
class SettingsViewModel : ViewModel() {

    // Internal mutable state
    private val _uiState = MutableStateFlow(SettingsUiState())

    // Public read-only state
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // --- Actions ---

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        // In a real app: Save to DataStore/SharedPreferences here
    }

    fun toggleImperialUnits(enabled: Boolean) {
        _uiState.update { it.copy(useImperialUnits = enabled) }
    }

    fun updateDefaultRadius(radius: Float) {
        _uiState.update { it.copy(defaultRadius = radius) }
    }

    fun toggleMapStyle() {
        _uiState.update {
            val newStyle = if (it.mapStyle == "Mapnik") "OpenTopoMap" else "Mapnik"
            it.copy(mapStyle = newStyle)
        }
    }

    fun clearMapCache(context: Context) {
        try {
            // 1. Clear OSMDroid specific tile cache (database)
            val sqlTileWriter = SqlTileWriter()
            val success = sqlTileWriter.purgeCache()
            sqlTileWriter.onDetach() // close database connection

            // 2. Clear generic app cache files (optional but good for "deep clean")
            context.cacheDir.deleteRecursively()

            if (success) {
                Toast.makeText(context, "Cache de la carte vidé", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Cache vidé (fichiers temporaires)", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur lors du nettoyage du cache", Toast.LENGTH_SHORT).show()
        }
    }
}