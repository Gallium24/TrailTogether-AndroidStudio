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