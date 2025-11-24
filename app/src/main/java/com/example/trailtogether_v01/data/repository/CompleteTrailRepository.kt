package com.example.trailtogether_v01.data.repository

import android.util.Log
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.data.models.Trail
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import kotlin.math.abs

class CompleteTrailRepository {

    private val client = HttpClient(Android)

    // Cache pour éviter de recharger les mêmes sentiers
    private val trailCache = mutableMapOf<String, List<Trail>>()
    private var lastRequestTime = 0L
    private val MIN_REQUEST_INTERVAL = 3000L // 3 secondes minimum entre requêtes

    suspend fun getEnrichedTrails(
        center: GeoPoint,
        radiusMeters: Float = 5000f
    ): Result<List<Trail>> {

        // ANTI-LAG: Vérifier le cache
        val cacheKey = "${center.latitude}_${center.longitude}_$radiusMeters"
        trailCache[cacheKey]?.let {
            Log.d("CompleteTrailRepo", "📦 Cache hit: ${it.size} sentiers")
            return Result.success(it)
        }

        // ANTI-LAG: Limiter la fréquence des requêtes
        val now = System.currentTimeMillis()
        if (now - lastRequestTime < MIN_REQUEST_INTERVAL) {
            Log.d("CompleteTrailRepo", "⏱️ Requête ignorée (trop tôt)")
            return Result.success(emptyList())
        }
        lastRequestTime = now

        return try {
            val rawTrails = getTrailsFromOverpass(center, radiusMeters)

            // FILTRER: Enlever les routes/rues/ponts
            val filteredTrails = rawTrails.filter { isValidTrail(it) }

            Log.d("CompleteTrailRepo", "🗑️ Filtré: ${rawTrails.size} → ${filteredTrails.size} sentiers")

            // LIMITER: Max 20 sentiers pour éviter le lag
            val limitedTrails = filteredTrails.take(20)

            val enrichedTrails = limitedTrails.mapNotNull { rawTrail ->
                enrichAndConvertToTrail(rawTrail)
            }

            // Mettre en cache
            trailCache[cacheKey] = enrichedTrails

            Log.d("CompleteTrailRepo", "✅ ${enrichedTrails.size} sentiers chargés")
            Result.success(enrichedTrails)
        } catch (e: Exception) {
            Log.e("CompleteTrailRepo", "❌ Erreur", e)
            Result.failure(e)
        }
    }

    /**
     * FILTRE: Vérifie si c'est un vrai sentier (pas une rue/pont)
     */
    private fun isValidTrail(trail: RawOverpassTrail): Boolean {
        val name = trail.name.lowercase()

        // Rejeter les noms typiques de rues/routes
        val invalidKeywords = listOf(
            "rue", "avenue", "boulevard", "chemin", "route", "road", "street",
            "pont", "bridge", "autoroute", "highway", "parking", "allée"
        )

        if (invalidKeywords.any { name.contains(it) }) {
            return false
        }

        // Rejeter les chemins trop courts (< 500m)
        val lengthKm = calculateDistance(trail.pathCoordinates)
        if (lengthKm < 0.5) {
            return false
        }

        // Accepter uniquement les vrais sentiers
        return trail.highway in listOf("path", "footway", "track")
    }

    private suspend fun getTrailsFromOverpass(
        center: GeoPoint,
        radiusMeters: Float
    ): List<RawOverpassTrail> {
        // 🔥 OPTIMISATION: Requête plus stricte
        val query = """
            [out:json][timeout:15];
            (
              way(around:$radiusMeters,${center.latitude},${center.longitude})
                ["highway"~"path|footway|track"]
                ["name"]
                ["surface"!="paved"]
                ["surface"!="asphalt"];
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()

        val response: HttpResponse = client.post("https://overpass-api.de/api/interpreter") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(listOf("data" to query).formUrlEncode())
        }

        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.bodyAsText()
            return parseOverpassTrails(responseBody)
        }

        return emptyList()
    }

    private fun parseOverpassTrails(jsonString: String): List<RawOverpassTrail> {
        val trails = mutableListOf<RawOverpassTrail>()
        val root = JSONObject(jsonString)
        val elements = root.getJSONArray("elements")

        val nodesMap = mutableMapOf<Long, GeoPoint>()
        val ways = mutableListOf<JSONObject>()

        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            when (element.getString("type")) {
                "node" -> {
                    val id = element.getLong("id")
                    val lat = element.getDouble("lat")
                    val lon = element.getDouble("lon")
                    nodesMap[id] = GeoPoint(lat, lon)
                }
                "way" -> ways.add(element)
            }
        }

        for (way in ways) {
            try {
                val id = way.getLong("id").toString()
                val tags = if (way.has("tags")) way.getJSONObject("tags") else null
                val name = tags?.optString("name", "Sentier sans nom") ?: "Sentier sans nom"

                val nodes = way.getJSONArray("nodes")
                val pathCoordinates = mutableListOf<GeoPoint>()

                for (j in 0 until nodes.length()) {
                    val nodeId = nodes.getLong(j)
                    nodesMap[nodeId]?.let { pathCoordinates.add(it) }
                }

                if (pathCoordinates.size >= 2) {
                    trails.add(
                        RawOverpassTrail(
                            id = id,
                            name = name,
                            pathCoordinates = pathCoordinates,
                            surface = tags?.optString("surface") ?: "unknown",
                            sacScale = tags?.optString("sac_scale"),
                            highway = tags?.optString("highway", "path") ?: "path"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("CompleteTrailRepo", "Erreur parsing way", e)
            }
        }

        return trails
    }

    private suspend fun enrichAndConvertToTrail(rawTrail: RawOverpassTrail): Trail? {
        try {
            val lengthKm = calculateDistance(rawTrail.pathCoordinates)

            // 🔥 OPTIMISATION: Réduire les points pour l'élévation (10 au lieu de 50)
            val sampledPoints = samplePoints(rawTrail.pathCoordinates, maxPoints = 10)

            val elevations = getElevations(sampledPoints)

            val (gain, loss) = if (elevations.isNotEmpty()) {
                calculateElevationStats(elevations)
            } else {
                val estimatedGain = (lengthKm * 50).toInt()
                Pair(estimatedGain, estimatedGain)
            }

            val difficulty = calculateDifficulty(lengthKm, gain, rawTrail.sacScale)
            val duration = estimateDuration(lengthKm, gain)

            return Trail(
                id = "osm_${rawTrail.id}",
                name = rawTrail.name,
                location = "OpenStreetMap",
                distance = "${String.format("%.1f", lengthKm)} km",
                duration = duration,
                difficulty = difficulty,
                rating = 0f,
                reviewsCount = 0,
                description = "Sentier ${rawTrail.highway} • Surface: ${rawTrail.surface}",
                latitude = rawTrail.pathCoordinates.first().latitude,
                longitude = rawTrail.pathCoordinates.first().longitude,
                imageUrl = "",
                tags = listOf(rawTrail.highway, rawTrail.surface),
                elevation = if (gain > 0) "${gain}m ↑ / ${loss}m ↓" else "",
                source = "osm",
                startPoint = rawTrail.pathCoordinates.first(),
                pathCoordinates = rawTrail.pathCoordinates.map {
                    mapOf("lat" to it.latitude, "lon" to it.longitude)
                }
            )
        } catch (e: Exception) {
            Log.e("CompleteTrailRepo", "Erreur enrichissement", e)
            return createBasicTrail(rawTrail)
        }
    }

    private fun createBasicTrail(rawTrail: RawOverpassTrail): Trail {
        val lengthKm = calculateDistance(rawTrail.pathCoordinates)
        val estimatedGain = (lengthKm * 50).toInt()
        val difficulty = calculateDifficulty(lengthKm, estimatedGain, rawTrail.sacScale)
        val duration = estimateDuration(lengthKm, estimatedGain)

        return Trail(
            id = "osm_${rawTrail.id}",
            name = rawTrail.name,
            location = "OpenStreetMap",
            distance = "${String.format("%.1f", lengthKm)} km",
            duration = duration,
            difficulty = difficulty,
            rating = 0f,
            reviewsCount = 0,
            description = "Sentier ${rawTrail.highway}",
            latitude = rawTrail.pathCoordinates.first().latitude,
            longitude = rawTrail.pathCoordinates.first().longitude,
            imageUrl = "",
            tags = listOf(rawTrail.highway),
            elevation = "",
            source = "osm",
            startPoint = rawTrail.pathCoordinates.first(),
            pathCoordinates = rawTrail.pathCoordinates.map {
                mapOf("lat" to it.latitude, "lon" to it.longitude)
            }
        )
    }

    private suspend fun getElevations(points: List<GeoPoint>): List<Int> {
        return try {
            val locations = points.joinToString(",") { "${it.latitude},${it.longitude}" }
            val url = "https://api.open-elevation.com/api/v1/lookup?locations=$locations"

            val response: HttpResponse = client.get(url)

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val json = Json.parseToJsonElement(responseBody).jsonObject
                val results = json["results"]?.jsonArray ?: return emptyList()

                results.map {
                    it.jsonObject["elevation"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("CompleteTrailRepo", "Erreur élévation", e)
            emptyList()
        }
    }

    private fun samplePoints(points: List<GeoPoint>, maxPoints: Int): List<GeoPoint> {
        if (points.size <= maxPoints) return points
        val step = points.size / maxPoints
        return points.filterIndexed { index, _ -> index % step == 0 }.take(maxPoints)
    }

    private fun calculateElevationStats(elevations: List<Int>): Pair<Int, Int> {
        var gain = 0
        var loss = 0

        for (i in 1 until elevations.size) {
            val diff = elevations[i] - elevations[i - 1]
            if (diff > 0) gain += diff
            else loss += abs(diff)
        }

        return Pair(gain, loss)
    }

    private fun calculateDistance(points: List<GeoPoint>): Double {
        var totalDistance = 0.0
        for (i in 1 until points.size) {
            totalDistance += points[i - 1].distanceToAsDouble(points[i]) / 1000.0
        }
        return totalDistance
    }

    private fun calculateDifficulty(lengthKm: Double, elevationGain: Int, sacScale: String?): Difficulty {
        sacScale?.let {
            return when (it) {
                "hiking", "T1" -> Difficulty.EASY
                "mountain_hiking", "T2", "T3" -> Difficulty.MODERATE
                "demanding_mountain_hiking", "T4", "T5" -> Difficulty.HARD
                "T6" -> Difficulty.EXPERT
                else -> Difficulty.MODERATE
            }
        }

        val difficulty = (lengthKm * 10) + (elevationGain / 100.0)
        return when {
            difficulty < 30 -> Difficulty.EASY
            difficulty < 60 -> Difficulty.MODERATE
            difficulty < 90 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }

    private fun estimateDuration(lengthKm: Double, elevationGain: Int): String {
        val hours = (lengthKm / 5.0) + (elevationGain / 600.0)
        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()

        return if (h > 0) {
            "${h}h${if (m > 0) " ${m}min" else ""}"
        } else {
            "${m}min"
        }
    }

    /**
     * Nettoie le cache (appeler quand l'utilisateur change beaucoup de région)
     */
    fun clearCache() {
        trailCache.clear()
        Log.d("CompleteTrailRepo", "🗑️ Cache nettoyé")
    }
}

private data class RawOverpassTrail(
    val id: String,
    val name: String,
    val pathCoordinates: List<GeoPoint>,
    val surface: String,
    val sacScale: String?,
    val highway: String
)