package com.example.blockblast.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Particle(
    val id: Long,
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val size: Float,
    val alpha: Float = 1f,
    val rotation: Float = 0f,
    val rotationSpeed: Float = 0f
)

data class FloatingScore(
    val id: Long,
    val text: String,
    val position: Offset,
    val color: Color,
    val alpha: Float = 1f,
    val scale: Float = 1f
)

data class BlastHighlight(
    val rows: Set<Int> = emptySet(),
    val cols: Set<Int> = emptySet(),
    val timestamp: Long = System.currentTimeMillis()
)
