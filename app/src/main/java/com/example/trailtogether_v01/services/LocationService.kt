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
            Log.w(TAG, "⚠️ Permissions de localisation non accordées")
            return DEFAULT_LOCATION
        }

        return try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            val location = getLastKnownLocation(fusedLocationClient)

            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                Log.d(TAG, "✅ Position trouvée: ${geoPoint.latitude}, ${geoPoint.longitude}")
                geoPoint
            } else {
                Log.w(TAG, "⚠️ Aucune position connue, utilisation position par défaut")
                DEFAULT_LOCATION
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur récupération position", e)
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
            Log.e(TAG, "❌ SecurityException", e)
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