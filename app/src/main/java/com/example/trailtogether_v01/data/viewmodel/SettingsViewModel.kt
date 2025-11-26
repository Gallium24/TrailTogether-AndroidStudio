package com.example.trailtogether_v01.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.utils.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.modules.SqlTileWriter
import android.widget.Toast

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