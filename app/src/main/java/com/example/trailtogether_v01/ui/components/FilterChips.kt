package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trailtogether_v01.data.models.Difficulty
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * DifficultyFilterChips est une composante qui représente les filtres de difficulté.
 * @param selectedDifficulty La difficulté actuellement sélectionnée.
 * @param onDifficultySelected Une fonction lambda appelée lorsque l'utilisateur sélectionne une nouvelle difficulté.
 * L'argument est la nouvelle difficulté sélectionnée.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyFilterChips(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {

        item {
            FilterChip(
                selected = selectedDifficulty == null,
                onClick = { onDifficultySelected(null) },
                label = { Text("Tous") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TrailGreen,
                    selectedLabelColor = Color.White
                )
            )
        }

        items(Difficulty.entries) { difficulty ->
            FilterChip(
                selected = selectedDifficulty == difficulty,
                onClick = { onDifficultySelected(difficulty) },
                label = { Text(getDifficultyText(difficulty)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TrailGreen,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

private fun getDifficultyText(difficulty: Difficulty): String {
    return when (difficulty) {
        Difficulty.EASY -> "Facile"
        Difficulty.MODERATE -> "Modéré"
        Difficulty.HARD -> "Difficile"
        Difficulty.EXPERT -> "Expert"
    }
}