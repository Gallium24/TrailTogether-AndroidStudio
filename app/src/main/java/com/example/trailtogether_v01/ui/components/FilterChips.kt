package com.example.trailtogether_v01.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyFilterChips(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedDifficulty == null,
            onClick = { onDifficultySelected(null) },
            label = { Text("Tous") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TrailGreen,
                selectedLabelColor = Color.White
            )
        )
        Difficulty.values().forEach { difficulty ->
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