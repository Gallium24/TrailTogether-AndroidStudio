package com.example.trailtogether_v01.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.util.GeoPoint
import kotlin.coroutines.resume

/**
 * LocationService.kt
 *
 * Service de gestion de la géolocalisation de l'utilisateur.
 *
 * Fonctionnalités:
 * - Récupération de la position GPS actuelle
 * - Demande de permissions de localisation
 * - Sauvegarde de la dernière position connue
 * - Fallback vers position par défaut si nécessaire
 * - Gestion du consentement utilisateur (demandé une seule fois)
 *
 * Constantes:
 * - DEFAULT_LOCATION: Saguenay, QC (48.4284°N, 71.0598°O)
 *
 * Méthodes:
 * - hasLocationPermission(context): Vérifie si permissions accordées
 * - getCurrentLocation(context): Récupère position actuelle ou défaut
 * - getLastKnownLocation(context): Dernière position connue du système
 * - saveLocationToPreferences(context, location): Sauvegarde position
 * - getSavedLocation(context): Récupère position sauvegardée
 * - wasLocationPermissionAsked(context): Vérifie si permission déjà demandée
 *
 * Permissions requises:
 * - ACCESS_FINE_LOCATION: Localisation précise (GPS)
 * - ACCESS_COARSE_LOCATION: Localisation approximative (réseau)
 *
 * Comportement:
 * - Premier lancement: Demande permission → sauvegarde résultat
 * - Lancements suivants: Utilise position sauvegardée ou défaut
 * - Si permission refusée: Utilise DEFAULT_LOCATION
 * - Si GPS désactivé: Utilise dernière position connue ou défaut
 *
 * Intégration:
 * - FusedLocationProviderClient pour récupération GPS
 * - SharedPreferences pour persistance
 * - Appelé par MainActivity au démarrage
 * - Position transmise à HomeViewModel
 *
 * Utilisation:
 * - Initialisation dans MainActivity.onCreate()
 * - Détermine position de départ de la carte dans HomeScreen
 * - Définit le centre initial pour loadMapTrails()
 */

object LocationService {

    private const val TAG = "LocationService"

    // Position par défaut : Saguenay
    private val DEFAULT_LOCATION = GeoPoint(48.4284, -71.0598)

    /**
     * Vérifie si les permissions de localisation sont accordées
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Récupère la position actuelle de l'utilisateur
     * Retourne la position par défaut si échec ou pas de permission
     */
    suspend fun getCurrentLocation(context: Context): GeoPoint {
        if (!hasLocationPermission(context)) {
            return DEFAULT_LOCATION
        }

        return try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            val location = getLastKnownLocation(fusedLocationClient)

            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                geoPoint
            } else {
                DEFAULT_LOCATION
            }
        } catch (e: Exception) {
            DEFAULT_LOCATION
        }
    }

    /**
     * Récupère la dernière position connue
     */
    private suspend fun getLastKnownLocation(
        fusedLocationClient: FusedLocationProviderClient
    ): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Erreur getCurrentLocation", exception)

                // Fallback sur lastLocation
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        continuation.resume(lastLocation)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: SecurityException) {
            continuation.resume(null)
        }
    }

    /**
     * Sauvegarde la position dans SharedPreferences
     */
    fun saveLocationToPreferences(context: Context, location: GeoPoint) {
        val prefs = context.getSharedPreferences("trail_together_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("last_lat", location.latitude.toString())
            putString("last_lon", location.longitude.toString())
            putBoolean("location_permission_asked", true)
            apply()
        }
    }

    /**
     * Vérifie si la permission a déjà été demandée
     */
    fun wasLocationPermissionAsked(context: Context): Boolean {
        val prefs = context.getSharedPreferences("trail_together_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("location_permission_asked", false)
    }

    /**
     * Récupère la dernière position sauvegardée
     */
    fun getSavedLocation(context: Context): GeoPoint? {
        val prefs = context.getSharedPreferences("trail_together_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getString("last_lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("last_lon", null)?.toDoubleOrNull()

        return if (lat != null && lon != null) {
            GeoPoint(lat, lon)
        } else {
            null
        }
    }
}