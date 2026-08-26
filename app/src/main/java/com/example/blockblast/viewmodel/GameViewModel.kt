package com.example.blockblast.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blockblast.audio.SoundManager
import com.example.blockblast.data.GamePreferences
import com.example.blockblast.model.BlastHighlight
import com.example.blockblast.model.BlockColor
import com.example.blockblast.model.BlockShape
import com.example.blockblast.model.FloatingScore
import com.example.blockblast.model.GRID_SIZE
import com.example.blockblast.model.GameState
import com.example.blockblast.model.Particle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GamePreferences(application)
    val soundManager = SoundManager(application)

    private val _gameState = MutableStateFlow(
        GameState(
            highScore = preferences.highScore,
            soundEffectsEnabled = preferences.soundEffectsEnabled,
            backgroundMusicEnabled = preferences.backgroundMusicEnabled
        )
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var animationJob: Job? = null

    init {
        soundManager.isSfxEnabled = preferences.soundEffectsEnabled
        soundManager.isBgmEnabled = preferences.backgroundMusicEnabled
        if (preferences.backgroundMusicEnabled) {
            soundManager.startMusic()
        }
    }

    private fun startAnimationLoop() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            while (true) {
                delay(16) // ~60 FPS
                updateAnimations()
            }
        }
    }

    private fun updateAnimations() {
        val currentParticles = _gameState.value.particles
        if (currentParticles.isEmpty()) return

        val updatedParticles = currentParticles.mapNotNull { p ->
            val newAlpha = p.alpha - 0.035f
            if (newAlpha <= 0f) null
            else p.copy(
                position = p.position + p.velocity,
                velocity = p.velocity + Offset(0f, 0.3f), // subtle gravity
                rotation = p.rotation + p.rotationSpeed,
                alpha = newAlpha
            )
        }

        _gameState.update { state ->
            state.copy(particles = updatedParticles)
        }
    }

    // --- Sound and Settings Toggles ---

    fun toggleSoundEffects() {
        val newState = !_gameState.value.soundEffectsEnabled
        preferences.soundEffectsEnabled = newState
        soundManager.isSfxEnabled = newState
        soundManager.playSound(SoundManager.SoundType.CLICK)
        _gameState.update { it.copy(soundEffectsEnabled = newState) }
    }

    fun toggleBackgroundMusic() {
        val newState = !_gameState.value.backgroundMusicEnabled
        preferences.backgroundMusicEnabled = newState
        soundManager.isBgmEnabled = newState
        soundManager.playSound(SoundManager.SoundType.CLICK)
        _gameState.update { it.copy(backgroundMusicEnabled = newState) }
    }

    fun openSettings() {
        soundManager.playSound(SoundManager.SoundType.CLICK)
        _gameState.update { it.copy(showSettingsDialog = true) }
    }

    fun closeSettings() {
        soundManager.playSound(SoundManager.SoundType.CLICK)
        _gameState.update { it.copy(showSettingsDialog = false) }
    }

    fun resetHighScore() {
        preferences.resetHighScore()
        soundManager.playSound(SoundManager.SoundType.CLICK)
        _gameState.update { it.copy(highScore = 0) }
    }

    fun onResumeApp() {
        if (_gameState.value.backgroundMusicEnabled) {
            soundManager.startMusic()
        }
    }

    fun onPauseApp() {
        soundManager.stopMusic()
    }

    // --- Game Logic ---

    fun canPlacePiece(shape: BlockShape, startRow: Int, startCol: Int): Boolean {
        return canPlaceShape(shape, startRow, startCol, _gameState.value.grid)
    }

    private fun canPlaceShape(
        shape: BlockShape,
        startRow: Int,
        startCol: Int,
        grid: List<List<BlockColor?>>
    ): Boolean {
        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.matrix[r][c]) {
                    val gridR = startRow + r
                    val gridC = startCol + c
                    if (gridR !in 0 until GRID_SIZE || gridC !in 0 until GRID_SIZE) {
                        return false
                    }
                    if (grid[gridR][gridC] != null) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun getPredictedClears(shape: BlockShape, startRow: Int, startCol: Int): Pair<Set<Int>, Set<Int>> {
        if (!canPlacePiece(shape, startRow, startCol)) return emptySet<Int>() to emptySet<Int>()

        // Simulate placing the piece
        val tempGrid = _gameState.value.grid.map { it.toMutableList() }
        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.matrix[r][c]) {
                    tempGrid[startRow + r][startCol + c] = shape.color
                }
            }
        }

        val fullRows = mutableSetOf<Int>()
        val fullCols = mutableSetOf<Int>()

        for (r in 0 until GRID_SIZE) {
            if (tempGrid[r].all { it != null }) {
                fullRows.add(r)
            }
        }

        for (c in 0 until GRID_SIZE) {
            if ((0 until GRID_SIZE).all { r -> tempGrid[r][c] != null }) {
                fullCols.add(c)
            }
        }

        return fullRows to fullCols
    }

    fun tryPlacePiece(
        pieceIndex: Int,
        targetRow: Int,
        targetCol: Int,
        cellCenterInScreen: Offset? = null
    ): Boolean {
        val state = _gameState.value
        if (state.isGameOver) return false
        val piece = state.availablePieces.getOrNull(pieceIndex) ?: return false

        if (!canPlaceShape(piece, targetRow, targetCol, state.grid)) {
            return false
        }

        soundManager.playSound(SoundManager.SoundType.PLACE)

        // 1. Place piece into grid
        val newGrid = state.grid.map { it.toMutableList() }
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.matrix[r][c]) {
                    newGrid[targetRow + r][targetCol + c] = piece.color
                }
            }
        }

        // 2. Consume piece from available slots
        val newPieces = state.availablePieces.toMutableList()
        newPieces[pieceIndex] = null

        // 3. Check for full rows and columns
        val fullRows = mutableListOf<Int>()
        val fullCols = mutableListOf<Int>()

        for (r in 0 until GRID_SIZE) {
            if (newGrid[r].all { it != null }) {
                fullRows.add(r)
            }
        }

        for (c in 0 until GRID_SIZE) {
            if ((0 until GRID_SIZE).all { r -> newGrid[r][c] != null }) {
                fullCols.add(c)
            }
        }

        val linesCleared = fullRows.size + fullCols.size
        var pointsEarned = piece.blockCount * 10
        var newCombo = state.comboCount

        if (linesCleared > 0) {
            newCombo += 1

            // Multiplier for combos & multi-lines
            val comboMultiplier = if (newCombo > 1) newCombo else 1
            val clearPoints = (linesCleared * 100 + (linesCleared - 1) * 50) * comboMultiplier
            pointsEarned += clearPoints

            soundManager.playClearSoundWithCombo(newCombo)

            // Clear the cells
            for (r in fullRows) {
                for (c in 0 until GRID_SIZE) {
                    newGrid[r][c] = null
                }
            }
            for (c in fullCols) {
                for (r in 0 until GRID_SIZE) {
                    newGrid[r][c] = null
                }
            }
        } else {
            newCombo = 0
        }

        val newScore = state.score + pointsEarned

        // Track and persist all-time high score in SharedPreferences
        val isNewAllTimeRecord = newScore > state.highScore
        val newHighScore = if (isNewAllTimeRecord) newScore else state.highScore
        if (isNewAllTimeRecord) {
            preferences.highScore = newHighScore
        }

        // Streak Progress calculation (0 to 10)
        val newTotalCleared = state.totalLinesCleared + linesCleared
        val currentStreakProgress = if (linesCleared > 0) (state.streakProgress + linesCleared) % 10 else state.streakProgress
        val streakMult = 1 + (newTotalCleared / 10).coerceAtMost(5)

        // Check if all 3 pieces are used -> spawn new set
        val finalPieces = if (newPieces.all { it == null }) {
            BlockShape.generatePieceSet()
        } else {
            newPieces
        }

        // Check for Game Over: can any available piece fit anywhere?
        val immutableGrid = newGrid.map { it.toList() }
        val canFitAny = canAnyPieceFit(finalPieces, immutableGrid)
        val gameOver = !canFitAny

        if (gameOver) {
            soundManager.playSound(SoundManager.SoundType.GAME_OVER)
        }

        _gameState.update {
            it.copy(
                grid = immutableGrid,
                availablePieces = finalPieces,
                score = newScore,
                highScore = newHighScore,
                lastEarnedPoints = pointsEarned,
                lastPointsTimestamp = System.currentTimeMillis(),
                comboCount = newCombo,
                streakProgress = currentStreakProgress,
                streakMultiplier = streakMult,
                totalLinesCleared = newTotalCleared,
                blastHighlight = if (linesCleared > 0) BlastHighlight(fullRows.toSet(), fullCols.toSet()) else null,
                isGameOver = gameOver,
                showGameOverDialog = gameOver,
                isNewHighScore = isNewAllTimeRecord && newScore > 0
            )
        }

        return true
    }

    private fun canAnyPieceFit(pieces: List<BlockShape?>, grid: List<List<BlockColor?>>): Boolean {
        val nonNullPieces = pieces.filterNotNull()
        if (nonNullPieces.isEmpty()) return true

        for (piece in nonNullPieces) {
            for (r in 0..(GRID_SIZE - piece.height)) {
                for (c in 0..(GRID_SIZE - piece.width)) {
                    if (canPlaceShape(piece, r, c, grid)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun restartGame() {
        soundManager.playSound(SoundManager.SoundType.CLICK)
        _gameState.update {
            GameState(
                score = 0,
                highScore = preferences.highScore,
                soundEffectsEnabled = preferences.soundEffectsEnabled,
                backgroundMusicEnabled = preferences.backgroundMusicEnabled,
                availablePieces = BlockShape.generatePieceSet(),
                isGameOver = false,
                showGameOverDialog = false,
                isNewHighScore = false
            )
        }
    }

    fun onPieceDragStart() {
        soundManager.playSound(SoundManager.SoundType.PICK_UP, 0.6f)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
