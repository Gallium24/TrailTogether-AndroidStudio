// SettingsManager.kt
package com.example.trailtogether_v01.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object SettingsManager {

    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    // On ne crée les flows QUE APRÈS init → plus jamais d'accès prématuré
    private fun requireInitialized() = check(::dataStore.isInitialized) {
        "SettingsManager.init(context) doit être appelé dans MainActivity.onCreate() avant setContent"
    }

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val USE_IMPERIAL_UNITS = booleanPreferencesKey("use_imperial_units")
        val DEFAULT_RADIUS_KM = floatPreferencesKey("default_radius_km")
        val MAP_STYLE = stringPreferencesKey("map_style")
        val SHOW_TRAIL_PATH = booleanPreferencesKey("show_trail_path")
    }

    // Ces propriétés sont calculées à la volée → aucun init statique
    val isDarkMode: Flow<Boolean>
        get() = run {
            requireInitialized()
            dataStore.data.map { it[Keys.IS_DARK_MODE] ?: false }
        }

    val useImperialUnits: Flow<Boolean>
        get() = run {
            requireInitialized()
            dataStore.data.map { it[Keys.USE_IMPERIAL_UNITS] ?: false }
        }

    val defaultRadiusKm: Flow<Float>
        get() = run {
            requireInitialized()
            dataStore.data.map { it[Keys.DEFAULT_RADIUS_KM] ?: 5f }
        }

    val mapStyle: Flow<String>
        get() = run {
            requireInitialized()
            dataStore.data.map { it[Keys.MAP_STYLE] ?: "Mapnik" }
        }

    val showTrailPath: Flow<Boolean>
        get() = run {
            requireInitialized()
            dataStore.data.map { it[Keys.SHOW_TRAIL_PATH] ?: true }
        }

    // Fonctions de mise à jour
    suspend fun setDarkMode(enabled: Boolean) = dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    suspend fun setImperialUnits(enabled: Boolean) = dataStore.edit { it[Keys.USE_IMPERIAL_UNITS] = enabled }
    suspend fun setDefaultRadius(radiusKm: Float) = dataStore.edit { it[Keys.DEFAULT_RADIUS_KM] = radiusKm.coerceIn(1f, 100f) }
    suspend fun setMapStyle(style: String) = dataStore.edit { it[Keys.MAP_STYLE] = style }
    suspend fun setShowTrailPath(show: Boolean) = dataStore.edit { it[Keys.SHOW_TRAIL_PATH] = show }

    // Pour un accès synchrone (rarement nécessaire)
    fun getDarkModeSync(): Boolean = runBlocking { isDarkMode.first() }
}