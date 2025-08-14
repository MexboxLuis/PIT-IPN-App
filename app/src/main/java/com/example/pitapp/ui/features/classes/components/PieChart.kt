package com.example.pitapp.ui.features.classes.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PieChart(entries: List<PieEntry>, colors: List<Color>, highlightedIndex: Int? = null) {
    if (entries.isEmpty() || colors.isEmpty()) return
    val total = entries.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(key1 = entries) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
            )
        )
    }

    val bounceDistancePx = with(LocalDensity.current) { 4.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        var startAngle = -90f

        entries.forEachIndexed { index, entry ->
            val targetSweepAngle = (entry.value / total) * 360f

            val animatedSweepAngle = targetSweepAngle * animationProgress.value

            if (animatedSweepAngle > 0f) {
                val baseColor = colors.getOrElse(index) { Color.LightGray }
                val currentColor = baseColor

                val currentTopLeft: Offset
                if (index == highlightedIndex) {
                    val middleAngleDegrees = startAngle + animatedSweepAngle / 2f
                    val middleAngleRadians = Math.toRadians(middleAngleDegrees.toDouble()).toFloat()

                    val offsetX = cos(middleAngleRadians) * bounceDistancePx
                    val offsetY = sin(middleAngleRadians) * bounceDistancePx

                    currentTopLeft = Offset(offsetX, offsetY)
                } else {
                    currentTopLeft = Offset.Zero
                }

                drawArc(
                    color = currentColor,
                    startAngle = startAngle,
                    sweepAngle = animatedSweepAngle,
                    useCenter = true,
                    topLeft = currentTopLeft,
                    size = size
                )
            }
            startAngle += animatedSweepAngle
        }
    }
}