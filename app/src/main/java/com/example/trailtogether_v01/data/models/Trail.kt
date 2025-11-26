package com.example.trailtogether_v01.data.models

import com.google.firebase.firestore.IgnoreExtraProperties
import org.osmdroid.util.GeoPoint


/**
 * Trail.kt
 *
 * Modèle unifié de sentier compatible avec Firestore ET données OpenStreetMap.
 *
 * Structure:
 * - id: Identifiant unique (préfixé "osm_" pour sentiers OSM)
 * - name: Nom du sentier
 * - location: Localisation (adresse ou coordonnées formatées)
 * - distance: Distance en kilomètres (Double)
 * - duration: Durée estimée (format "Xh" ou "XXmin")
 * - difficulty: Niveau de difficulté (enum: EASY, MODERATE, HARD, EXPERT)
 * - rating: Note moyenne (pour sentiers Firestore)
 * - reviewsCount: Nombre d'avis
 * - description: Description du sentier
 * - latitude/longitude: Coordonnées GPS du point de départ
 * - imageUrl: URL de l'image (pour sentiers Firestore)
 * - tags: Liste de tags (type de surface, type de chemin)
 * - elevation: Dénivelé (format "XXXm ↑ / XXXm ↓")
 * - source: Source des données ("firestore" ou "osm")
 * - startPoint: Point de départ (GeoPoint, exclu de Firestore)
 * - pathCoordinates: Tracé du sentier (liste de coordonnées, exclu de Firestore)
 *
 * Fonctions utilitaires:
 * - getTrailStartPoint(): Retourne le GeoPoint de départ
 * - getPathAsGeoPoints(): Convertit pathCoordinates en List<GeoPoint>
 * - hasValidCoordinates(): Vérifie si les coordonnées GPS sont valides
 *
 * Utilisation:
 * - Chargé depuis Firestore (sentiers custom) ou API Overpass (sentiers OSM)
 * - Enrichi par CompleteTrailRepository avec calculs de distance, dénivelé, difficulté
 * - Affiché dans HomeScreen, TrailDetailScreen, EventCard
 */
enum class Difficulty {
    EASY, MODERATE, HARD, EXPERT;

    fun toDisplayString(): String = when(this) {
        EASY -> "Facile"
        MODERATE -> "Modéré"
        HARD -> "Difficile"
        EXPERT -> "Expert"
    }
}

/**
 * Modèle unifié de Trail compatible avec Firestore ET OSM
 */
@IgnoreExtraProperties
data class Trail(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val distance: Double? = null,
    val duration: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val tags: List<String> = emptyList(),
    val elevation: String = "",
    val source: String = "firestore", // "firestore" ou "osm"

    // Champs pour compatibilité OSM (ignorés par Firestore si absents)
    @get:com.google.firebase.firestore.Exclude
    val startPoint: GeoPoint? = null,

    @get:com.google.firebase.firestore.Exclude
    val pathCoordinates: List<Map<String, Double>> = emptyList()
) {
    /**
     * Retourne le GeoPoint de départ (utilise startPoint ou crée depuis lat/lon)
     * RENOMMÉ pour éviter le conflit avec le getter automatique
     */
    fun getTrailStartPoint(): GeoPoint? {
        return startPoint ?: if (latitude != 0.0 && longitude != 0.0) {
            GeoPoint(latitude, longitude)
        } else null
    }

    /**
     * Retourne les coordonnées du chemin en format GeoPoint
     */
    fun getPathAsGeoPoints(): List<GeoPoint> {
        return pathCoordinates.mapNotNull { coord ->
            val lat = coord["lat"]
            val lon = coord["lon"]
            if (lat != null && lon != null) {
                GeoPoint(lat, lon)
            } else null
        }
    }

    /**
     * Vérifie si le trail a des coordonnées GPS valides
     */
    fun hasValidCoordinates(): Boolean {
        return getTrailStartPoint() != null
    }
}