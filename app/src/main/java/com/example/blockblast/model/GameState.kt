package com.example.blockblast.model

const val GRID_SIZE = 8

data class GameState(
    val grid: List<List<BlockColor?>> = List(GRID_SIZE) { List(GRID_SIZE) { null } },
    val availablePieces: List<BlockShape?> = BlockShape.generatePieceSet(),
    val score: Int = 0,
    val highScore: Int = 0,
    val lastEarnedPoints: Int = 0,
    val lastPointsTimestamp: Long = 0L,
    val comboCount: Int = 0,
    val streakProgress: Int = 0,
    val streakGoal: Int = 10,
    val streakMultiplier: Int = 1,
    val totalLinesCleared: Int = 0,
    val isGameOver: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val soundEffectsEnabled: Boolean = true,
    val backgroundMusicEnabled: Boolean = true,
    val particles: List<Particle> = emptyList(),
    val blastHighlight: BlastHighlight? = null,
    val isNewHighScore: Boolean = false
)
