package com.example.trailtogether_v01.data.repository

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Service de géocodage inversé pour convertir des coordonnées GPS en adresses lisibles.
 * Utilise l'API Nominatim d'OpenStreetMap (gratuite, respecte les limites d'utilisation).
 */
object GeocodingService {

    private val client = HttpClient(Android)

    /**
     * Convertit des coordonnées en adresse lisible.
     * Retourne le lieu le plus pertinent (parc, ville, région).
     */
    suspend fun getAddressFromCoordinates(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        // Vérifier le cache d'abord
        GeocodingCache.get(lat, lon)?.let {
            Log.d("GeocodingService", "Cache hit pour ($lat, $lon)")
            return@withContext it
        }

        try {
            // Nominatim API - IMPORTANT: User-Agent requis, limite 1 req/sec
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=14&addressdetails=1"

            val response: HttpResponse = client.get(url) {
                headers {
                    append("User-Agent", "TrailTogetherApp/1.0")
                }
            }

            if (response.status.value != 200) {
                Log.e("GeocodingService", "Erreur HTTP: ${response.status}")
                return@withContext "Localisation inconnue"
            }

            val responseBody = response.bodyAsText()
            val json = JSONObject(responseBody)

            val address = json.optJSONObject("address")

            // Construire l'adresse par ordre de priorité
            val location = buildString {
                // Priorité 1 : Lieux naturels (parcs, forêts, etc.)
                val tourism = address?.optString("tourism")
                val leisure = address?.optString("leisure")
                val natural = address?.optString("natural")
                val peak = address?.optString("peak")
                val park = address?.optString("park")

                when {
                    !tourism.isNullOrEmpty() && tourism != "null" -> append(tourism)
                    !leisure.isNullOrEmpty() && leisure != "null" -> append(leisure)
                    !park.isNullOrEmpty() && park != "null" -> append(park)
                    !natural.isNullOrEmpty() && natural != "null" -> append(natural)
                    !peak.isNullOrEmpty() && peak != "null" -> append(peak)
                    else -> {
                        // Priorité 2 : Ville/village
                        val village = address?.optString("village")
                        val town = address?.optString("town")
                        val city = address?.optString("city")
                        val municipality = address?.optString("municipality")

                        when {
                            !village.isNullOrEmpty() && village != "null" -> append(village)
                            !town.isNullOrEmpty() && town != "null" -> append(town)
                            !city.isNullOrEmpty() && city != "null" -> append(city)
                            !municipality.isNullOrEmpty() && municipality != "null" -> append(municipality)
                        }
                    }
                }

                // Ajouter la région/département si on a déjà un lieu
                if (isNotEmpty()) {
                    val state = address?.optString("state")
                    val county = address?.optString("county")
                    val province = address?.optString("province")

                    append(", ")
                    when {
                        !county.isNullOrEmpty() && county != "null" -> append(county)
                        !state.isNullOrEmpty() && state != "null" -> append(state)
                        !province.isNullOrEmpty() && province != "null" -> append(province)
                    }
                }
            }

            val finalLocation = if (location.isNotEmpty()) {
                location
            } else {
                // Fallback : utiliser display_name simplifié
                json.optString("display_name", "Localisation inconnue")
                    .split(",")
                    .take(2)
                    .joinToString(", ")
                    .trim()
            }

            Log.d("GeocodingService", "Adresse trouvée: $finalLocation")

            // Mettre en cache
            GeocodingCache.put(lat, lon, finalLocation)

            finalLocation

        } catch (e: Exception) {
            Log.e("GeocodingService", "Erreur géocodage: ${e.message}", e)
            "Localisation inconnue"
        }
    }
}

/**
 * Cache simple pour éviter de géocoder plusieurs fois les mêmes coordonnées.
 * Réduit drastiquement le nombre d'appels API.
 */
object GeocodingCache {
    private val cache = mutableMapOf<String, String>()

    fun get(lat: Double, lon: Double): String? {
        val key = createKey(lat, lon)
        return cache[key]
    }

    fun put(lat: Double, lon: Double, address: String) {
        val key = createKey(lat, lon)
        cache[key] = address
    }

    private fun createKey(lat: Double, lon: Double): String {
        // Arrondir à 3 décimales (~100m de précision)
        // Cela regroupe les sentiers proches sous la même adresse
        return "${String.format("%.3f", lat)}_${String.format("%.3f", lon)}"
    }

    fun clear() {
        cache.clear()
    }

    fun size() = cache.size
}
