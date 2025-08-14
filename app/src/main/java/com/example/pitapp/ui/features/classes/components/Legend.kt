package com.example.pitapp.ui.features.classes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Legend(
    entries: List<PieEntry>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    highlightedIndex: Int? = null
) {
    fun Color.highlighted(): Color {
        return lerp(this, Color.White, 0.3f)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val totalValue = entries.sumOf { it.value.toDouble() }.toFloat()
        entries.forEachIndexed { index, entry ->
            val percentage = if (totalValue > 0) (entry.value / totalValue * 100) else 0f
            val isHighlighted = index == highlightedIndex

            val textColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                LocalContentColor.current
            }
            val fontWeight = if (isHighlighted) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
            val boxColor = colors.getOrElse(index) { Color.Gray }
            val finalBoxColor = if (isHighlighted) {
                boxColor.highlighted()
            } else {
                boxColor
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = finalBoxColor,
                            shape = MaterialTheme.shapes.small
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${entry.label}: ${entry.value.toInt()} (${"%.1f".format(percentage)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    fontWeight = fontWeight
                )
            }
        }
    }
}