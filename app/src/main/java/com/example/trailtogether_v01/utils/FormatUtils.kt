package com.example.trailtogether_v01.utils

/**
 * Utilitaires de formatage pour l'affichage des données
 */
object FormatUtils {

    /**
     * Formate une distance en fonction des préférences utilisateur
     * @param distanceKm Distance en kilomètres
     * @param useImperial Si true, convertit en miles, sinon utilise km
     * @return Distance formatée avec l'unité appropriée (ex: "5.2 km" ou "3.2 mi")
     */
    fun formatDistance(distanceKm: Double?, useImperial: Boolean): String {
        if (distanceKm == null) return "N/A"

        return if (useImperial) {
            val miles = distanceKm * 0.621371
            String.format("%.2f mi", miles)
        } else {
            String.format("%.2f km", distanceKm)
        }
    }
}