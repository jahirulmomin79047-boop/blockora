package com.example.blockblast.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.blockblast.model.BlockShape
import com.example.blockblast.ui.components.*
import com.example.blockblast.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun BlockBlastGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()

    // Board position in root screen coordinates
    var boardTopLeft by remember { mutableStateOf(Offset.Zero) }
    var boardCellSize by remember { mutableFloatStateOf(0f) }

    // Active drag state
    var activeDragPieceIndex by remember { mutableStateOf<Int?>(null) }
    var activeDragShape by remember { mutableStateOf<BlockShape?>(null) }
    var dragTouchPosition by remember { mutableStateOf(Offset.Zero) }

    // Calculated hover grid coordinates
    val (hoverRow, hoverCol) = remember(dragTouchPosition, boardTopLeft, boardCellSize, activeDragShape) {
        val shape = activeDragShape
        if (shape != null && boardCellSize > 0f) {
            // Drag visual offset slightly above finger for natural finger visibility
            val verticalOffset = boardCellSize * 1.3f
            val adjustedY = dragTouchPosition.y - verticalOffset
            val relX = dragTouchPosition.x - boardTopLeft.x - ((shape.width * boardCellSize) / 2f)
            val relY = adjustedY - boardTopLeft.y - ((shape.height * boardCellSize) / 2f)

            val col = (relX / boardCellSize).roundToInt()
            val row = (relY / boardCellSize).roundToInt()
            row to col
        } else {
            null to null
        }
    }

    val isHoverValid = remember(activeDragShape, hoverRow, hoverCol, state.grid) {
        val shape = activeDragShape
        if (shape != null && hoverRow != null && hoverCol != null) {
            viewModel.canPlacePiece(shape, hoverRow, hoverCol)
        } else {
            false
        }
    }

    val (predictedRows, predictedCols) = remember(activeDragShape, hoverRow, hoverCol, isHoverValid) {
        val shape = activeDragShape
        if (shape != null && hoverRow != null && hoverCol != null && isHoverValid) {
            viewModel.getPredictedClears(shape, hoverRow, hoverCol)
        } else {
            emptySet<Int>() to emptySet<Int>()
        }
    }

    // Premium gaming background gradient: Deep navy, dark purple and subtle blue
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0C1026), // Deep midnight navy
            Color(0xFF131940), // Dark royal navy
            Color(0xFF1B153F), // Deep twilight indigo
            Color(0xFF221648), // Deep arcade purple
            Color(0xFF19123D), // Dark violet-navy
            Color(0xFF0F1433), // Deep midnight blue
            Color(0xFF090D22)  // Rich dark navy base
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Soft colorful ambient glow, subtle abstract light pattern & depth vignette
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Soft colorful glows around the game screen
            // Center-top cyan/blue ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x2200E5FF),
                        Color(0x0E2979FF),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.18f),
                    radius = w * 0.60f
                ),
                radius = w * 0.60f,
                center = Offset(w * 0.5f, h * 0.18f)
            )

            // Board-center rich vibrant purple/magenta/cyan glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x327C4DFF),
                        Color(0x1CFF2A85),
                        Color(0x1200E5FF),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.48f),
                    radius = w * 0.80f
                ),
                radius = w * 0.80f,
                center = Offset(w * 0.5f, h * 0.48f)
            )

            // Bottom tray soft electric blue glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x252979FF),
                        Color(0x107C4DFF),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.88f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.5f, h * 0.88f)
            )

            // 2. Subtle abstract geometric light pattern (clean & minimal diamond lattice)
            val gridSpacing = 48.dp.toPx()
            val strokeColor = Color(0xFF818CF8).copy(alpha = 0.045f)
            val nodeDotColor = Color(0xFFA5B4FC).copy(alpha = 0.08f)

            // Diagonal lattice lines (\)
            var offset = -h
            while (offset < w + h) {
                drawLine(
                    color = strokeColor,
                    start = Offset(offset, 0f),
                    end = Offset(offset + h, h),
                    strokeWidth = 1.0f
                )
                offset += gridSpacing
            }

            // Diagonal lattice lines (/)
            offset = -h
            while (offset < w + h) {
                drawLine(
                    color = strokeColor,
                    start = Offset(offset + h, 0f),
                    end = Offset(offset, h),
                    strokeWidth = 1.0f
                )
                offset += gridSpacing
            }

            // Minimal luminous intersection nodes in the outer margins
            var xPos = gridSpacing
            while (xPos < w) {
                var yPos = gridSpacing
                while (yPos < h) {
                    drawCircle(
                        color = nodeDotColor,
                        radius = 1.5.dp.toPx(),
                        center = Offset(xPos, yPos)
                    )
                    yPos += gridSpacing * 2
                }
                xPos += gridSpacing * 2
            }

            // 3. Subtle soft diagonal light streak across background
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1200E5FF),
                        Color(0x0C7C4DFF),
                        Color.Transparent
                    ),
                    start = Offset(0f, h * 0.2f),
                    end = Offset(w, h * 0.7f)
                ),
                start = Offset(0f, h * 0.2f),
                end = Offset(w, h * 0.7f),
                strokeWidth = 80.dp.toPx()
            )

            // 4. Outer edge vignette for depth (brighter center, darker edges, no pure black)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1507091B),
                        Color(0x45050718)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = maxOf(w, h) * 0.75f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header (Scores, streak progress & quick control icon buttons)
            HeaderView(
                state = state,
                onToggleSfx = { viewModel.toggleSoundEffects() },
                onToggleBgm = { viewModel.toggleBackgroundMusic() },
                onOpenSettings = { viewModel.openSettings() },
                onRestart = { viewModel.restartGame() }
            )

            // 2. 8x8 Board View
            BoardView(
                state = state,
                hoveredShape = activeDragShape,
                hoverRow = hoverRow,
                hoverCol = hoverCol,
                isHoverValid = isHoverValid,
                predictedClearRows = predictedRows,
                predictedClearCols = predictedCols,
                onBoardPositioned = { topLeft, cellSize ->
                    boardTopLeft = topLeft
                    boardCellSize = cellSize
                },
                modifier = Modifier.weight(1f, fill = false)
            )

            // 3. Piece Tray
            PieceTray(
                pieces = state.availablePieces,
                activeDragIndex = activeDragPieceIndex,
                onDragStart = { index, initialCenter, shape ->
                    activeDragPieceIndex = index
                    activeDragShape = shape
                    dragTouchPosition = initialCenter
                    viewModel.onPieceDragStart()
                },
                onDrag = { currentPointerPos ->
                    dragTouchPosition = currentPointerPos
                },
                onDragEnd = { finalPointerPos ->
                    val pieceIndex = activeDragPieceIndex
                    val shape = activeDragShape
                    if (pieceIndex != null && shape != null && boardCellSize > 0f) {
                        val verticalOffset = boardCellSize * 1.3f
                        val adjustedY = finalPointerPos.y - verticalOffset
                        val relX = finalPointerPos.x - boardTopLeft.x - ((shape.width * boardCellSize) / 2f)
                        val relY = adjustedY - boardTopLeft.y - ((shape.height * boardCellSize) / 2f)

                        val targetCol = (relX / boardCellSize).roundToInt()
                        val targetRow = (relY / boardCellSize).roundToInt()

                        if (viewModel.canPlacePiece(shape, targetRow, targetCol)) {
                            viewModel.tryPlacePiece(
                                pieceIndex = pieceIndex,
                                targetRow = targetRow,
                                targetCol = targetCol,
                                cellCenterInScreen = finalPointerPos
                            )
                        }
                    }

                    activeDragPieceIndex = null
                    activeDragShape = null
                },
                onDragCancel = {
                    val pieceIndex = activeDragPieceIndex
                    val shape = activeDragShape
                    if (pieceIndex != null && shape != null && boardCellSize > 0f) {
                        val verticalOffset = boardCellSize * 1.3f
                        val adjustedY = dragTouchPosition.y - verticalOffset
                        val relX = dragTouchPosition.x - boardTopLeft.x - ((shape.width * boardCellSize) / 2f)
                        val relY = adjustedY - boardTopLeft.y - ((shape.height * boardCellSize) / 2f)

                        val targetCol = (relX / boardCellSize).roundToInt()
                        val targetRow = (relY / boardCellSize).roundToInt()

                        if (viewModel.canPlacePiece(shape, targetRow, targetCol)) {
                            viewModel.tryPlacePiece(
                                pieceIndex = pieceIndex,
                                targetRow = targetRow,
                                targetCol = targetCol,
                                cellCenterInScreen = dragTouchPosition
                            )
                        }
                    }

                    activeDragPieceIndex = null
                    activeDragShape = null
                }
            )
        }

        // 4. Floating Drag Piece Overlay
        if (activeDragShape != null && activeDragPieceIndex != null) {
            val shape = activeDragShape!!
            val cellSize = if (boardCellSize > 0f) boardCellSize else 44.dp.value
            val verticalOffset = cellSize * 1.3f
            val adjustedPos = dragTouchPosition - Offset(0f, verticalOffset)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val blockPadding = cellSize * 0.065f
                val blockSize = cellSize - (blockPadding * 2)
                val cornerRadius = CornerRadius(blockSize * 0.2f, blockSize * 0.2f)

                val shapeWidth = shape.width * cellSize
                val shapeHeight = shape.height * cellSize
                val startX = adjustedPos.x - (shapeWidth / 2f)
                val startY = adjustedPos.y - (shapeHeight / 2f)

                for (r in 0 until shape.height) {
                    for (c in 0 until shape.width) {
                        if (shape.matrix[r][c]) {
                            val x = startX + c * cellSize + blockPadding
                            val y = startY + r * cellSize + blockPadding

                            // Lifted drop shadow
                            drawRoundRect(
                                color = Color.Black.copy(alpha = 0.45f),
                                topLeft = Offset(x + 4f, y + 8f),
                                size = Size(blockSize, blockSize),
                                cornerRadius = cornerRadius
                            )

                            // Dragged block
                            drawBlock(
                                color = shape.color,
                                topLeft = Offset(x, y),
                                size = Size(blockSize, blockSize),
                                cornerRadius = cornerRadius,
                                alpha = 0.98f
                            )
                        }
                    }
                }
            }
        }

        // 5. Settings Modal Dialog
        if (state.showSettingsDialog) {
            SettingsDialog(
                soundEffectsEnabled = state.soundEffectsEnabled,
                backgroundMusicEnabled = state.backgroundMusicEnabled,
                onToggleSoundEffects = { viewModel.toggleSoundEffects() },
                onToggleBackgroundMusic = { viewModel.toggleBackgroundMusic() },
                onResetHighScore = { viewModel.resetHighScore() },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        // 6. Modern Animated Game Over Modal Screen Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = state.showGameOverDialog,
            enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)),
            exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
        ) {
            GameOverDialog(
                score = state.score,
                highScore = state.highScore,
                isNewHighScore = state.isNewHighScore,
                totalLinesCleared = state.totalLinesCleared,
                onRestart = { viewModel.restartGame() }
            )
        }
    }
}
