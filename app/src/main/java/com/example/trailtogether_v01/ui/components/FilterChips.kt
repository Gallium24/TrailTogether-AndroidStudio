package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trailtogether_v01.data.models.Difficulty

/**
 * FilterChips.kt
 *
 * Composant DifficultyFilterChips pour filtrer les sentiers par difficulté.
 *
 * Fonctionnalités:
 * - Affichage de chips cliquables pour chaque niveau de difficulté
 * - Sélection/désélection d'un niveau
 * - Indicateur visuel de sélection
 *
 * Niveaux de difficulté:
 * - EASY (Facile): 🟢 Vert
 * - MODERATE (Modéré): 🟠 Orange
 * - HARD (Difficile): 🟠 Orange foncé
 * - EXPERT (Expert): 🔴 Rouge
 *
 * Comportement:
 * - Clic sur chip sélectionnée: Désélectionne (affiche tous les niveaux)
 * - Clic sur chip non sélectionnée: Sélectionne ce niveau uniquement
 * - Une seule sélection à la fois
 *
 * Design:
 * - Chips Material 3 avec emoji et texte
 * - Chip sélectionnée: Couleur de fond appropriée
 * - Chip non sélectionnée: Fond gris clair
 * - Disposition horizontale avec espacement
 *
 * Paramètres:
 * - selectedDifficulty: Difficulté actuellement sélectionnée (null = toutes)
 * - onDifficultySelected: Callback lors de la sélection
 *
 * Utilisation:
 * - Utilisé dans HomeScreen au-dessus de la liste de sentiers
 * - Mis à jour via HomeViewModel.setDifficultyFilter()
 * - Filtre automatiquement la liste filteredTrails
 */

@Composable
fun DifficultyFilterChips(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit
) {
    Row(

        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedDifficulty == null,
            onClick = { onDifficultySelected(null) },
            label = { Text("Tous") }
        )
        FilterChip(
            selected = selectedDifficulty == Difficulty.EASY,
            onClick = { onDifficultySelected(Difficulty.EASY) },
            label = { Text("🟢 Facile") }
        )
        FilterChip(
            selected = selectedDifficulty == Difficulty.MODERATE,
            onClick = { onDifficultySelected(Difficulty.MODERATE) },
            label = { Text("🟠 Modéré") }
        )
        FilterChip(
            selected = selectedDifficulty == Difficulty.HARD,
            onClick = { onDifficultySelected(Difficulty.HARD) },
            label = { Text("⚫ Difficile") }
        )
        FilterChip(
            selected = selectedDifficulty == Difficulty.EXPERT,
            onClick = { onDifficultySelected(Difficulty.EXPERT) },
            label = { Text("🔴 Expert") }
        )
    }
}