package com.example.blockblast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.blockblast.model.BlockColor
import com.example.blockblast.model.BlockShape
import com.example.blockblast.model.GRID_SIZE
import com.example.blockblast.model.GameState
import com.example.blockblast.ui.theme.*

@Composable
fun BoardView(
    state: GameState,
    hoveredShape: BlockShape?,
    hoverRow: Int?,
    hoverCol: Int?,
    isHoverValid: Boolean,
    predictedClearRows: Set<Int>,
    predictedClearCols: Set<Int>,
    onBoardPositioned: (Offset, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sharp neon border gradient for the board
    val boardBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFF7C4DFF),
            Color(0xFFFF2A85),
            Color(0xFFFF9E00),
            Color(0xFF00E5FF)
        )
    )

    // Deep illuminated arcade purple base for the board
    val boardBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E133E),
            Color(0xFF140C2E),
            Color(0xFF0C071D)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .aspectRatio(1f)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF7C4DFF).copy(alpha = 0.55f),
                spotColor = Color(0xFF00E5FF).copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(boardBackgroundBrush)
            .border(2.dp, boardBorderBrush, RoundedCornerShape(20.dp))
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInRoot()
                val boardSize = bounds.width
                val cellSize = boardSize / GRID_SIZE
                onBoardPositioned(bounds.topLeft, cellSize)
            }
            .testTag("game_board")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardWidth = size.width
            val cellSize = boardWidth / GRID_SIZE
            val blockPadding = cellSize * 0.065f
            val blockSize = cellSize - (blockPadding * 2)
            val cornerRadius = CornerRadius(blockSize * 0.2f, blockSize * 0.2f)

            // 1. Draw Crisp Grid Cells
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    val x = c * cellSize + blockPadding
                    val y = r * cellSize + blockPadding

                    val isClearingHover = predictedClearRows.contains(r) || predictedClearCols.contains(c)

                    if (isClearingHover) {
                        // Sharp glowing clear preview highlight
                        drawRoundRect(
                            color = Color(0xAAFFD700),
                            topLeft = Offset(x, y),
                            size = Size(blockSize, blockSize),
                            cornerRadius = cornerRadius
                        )
                        drawRoundRect(
                            color = Color(0xFFFFF07A),
                            topLeft = Offset(x, y),
                            size = Size(blockSize, blockSize),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 2f)
                        )
                    } else {
                        // High-contrast clean empty cell
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(GridCellEmpty, GridCellEmptyInner),
                                startY = y,
                                endY = y + blockSize
                            ),
                            topLeft = Offset(x, y),
                            size = Size(blockSize, blockSize),
                            cornerRadius = cornerRadius
                        )

                        // Clean top rim highlight
                        drawRoundRect(
                            color = Color(0x22FFFFFF),
                            topLeft = Offset(x + 1f, y + 1f),
                            size = Size(blockSize - 2f, blockSize * 0.4f),
                            cornerRadius = CornerRadius(blockSize * 0.16f, blockSize * 0.16f)
                        )

                        // Crisp cell border
                        drawRoundRect(
                            color = GridCellBorder,
                            topLeft = Offset(x, y),
                            size = Size(blockSize, blockSize),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 1.2f)
                        )
                    }
                }
            }

            // 2. Draw Hover Preview (Ghost) if dragging
            if (hoveredShape != null && hoverRow != null && hoverCol != null) {
                for (r in 0 until hoveredShape.height) {
                    for (c in 0 until hoveredShape.width) {
                        if (hoveredShape.matrix[r][c]) {
                            val targetR = hoverRow + r
                            val targetC = hoverCol + c
                            if (targetR in 0 until GRID_SIZE && targetC in 0 until GRID_SIZE) {
                                val x = targetC * cellSize + blockPadding
                                val y = targetR * cellSize + blockPadding

                                if (isHoverValid) {
                                    // Valid placement ghost: crisp semi-transparent block preview
                                    drawBlock(
                                        color = hoveredShape.color,
                                        topLeft = Offset(x, y),
                                        size = Size(blockSize, blockSize),
                                        cornerRadius = cornerRadius,
                                        alpha = 0.72f
                                    )
                                } else {
                                    // Invalid placement ghost: Crisp warning red
                                    drawRoundRect(
                                        color = Color(0x66FF1744),
                                        topLeft = Offset(x, y),
                                        size = Size(blockSize, blockSize),
                                        cornerRadius = cornerRadius
                                    )
                                    drawRoundRect(
                                        color = Color(0xFFFF1744),
                                        topLeft = Offset(x, y),
                                        size = Size(blockSize, blockSize),
                                        cornerRadius = cornerRadius,
                                        style = Stroke(width = 2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Draw Placed Blocks on Board (Sharp & 3D Glossy)
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    val blockColor = state.grid[r][c]
                    if (blockColor != null) {
                        val x = c * cellSize + blockPadding
                        val y = r * cellSize + blockPadding

                        drawBlock(
                            color = blockColor,
                            topLeft = Offset(x, y),
                            size = Size(blockSize, blockSize),
                            cornerRadius = cornerRadius,
                            alpha = 1.0f
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3D Glossy Jewel Block Drawing Function
 * Ultra-crisp, bright, glossy crystal jewel with sharp bevels and clean specular reflections.
 */
fun DrawScope.drawBlock(
    color: BlockColor,
    topLeft: Offset,
    size: Size,
    cornerRadius: CornerRadius,
    alpha: Float = 1.0f
) {
    val w = size.width
    val h = size.height

    // 1. Crisp drop shadow for 3D elevation
    drawRoundRect(
        color = Color.Black.copy(alpha = alpha * 0.45f),
        topLeft = Offset(topLeft.x + w * 0.04f, topLeft.y + h * 0.06f),
        size = size,
        cornerRadius = cornerRadius
    )

    // 2. High-saturation, punchy block body gradient
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                color.highlight,
                color.primary,
                color.shadow
            ),
            startY = topLeft.y,
            endY = topLeft.y + h
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = cornerRadius,
        alpha = alpha
    )

    // 3. Top-half glossy crystal reflection facet
    val glossFacetPath = Path().apply {
        moveTo(topLeft.x + w * 0.12f, topLeft.y + h * 0.10f)
        lineTo(topLeft.x + w * 0.88f, topLeft.y + h * 0.10f)
        lineTo(topLeft.x + w * 0.74f, topLeft.y + h * 0.42f)
        lineTo(topLeft.x + w * 0.26f, topLeft.y + h * 0.42f)
        close()
    }
    drawPath(
        path = glossFacetPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha * 0.80f),
                Color.White.copy(alpha = alpha * 0.20f)
            ),
            startY = topLeft.y + h * 0.10f,
            endY = topLeft.y + h * 0.42f
        )
    )

    // 4. Crisp diagonal specular glass reflection streak
    val streakPath = Path().apply {
        moveTo(topLeft.x + w * 0.18f, topLeft.y + h * 0.84f)
        lineTo(topLeft.x + w * 0.36f, topLeft.y + h * 0.84f)
        lineTo(topLeft.x + w * 0.82f, topLeft.y + h * 0.22f)
        lineTo(topLeft.x + w * 0.64f, topLeft.y + h * 0.22f)
        close()
    }
    drawPath(
        path = streakPath,
        color = Color.White.copy(alpha = alpha * 0.30f)
    )

    // 5. Crisp Corner Sparkle glint
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.95f),
        radius = w * 0.08f,
        center = Offset(topLeft.x + w * 0.27f, topLeft.y + h * 0.25f)
    )

    // 6. Deep bottom bevel for crisp 3D depth
    val bottomShadowPath = Path().apply {
        moveTo(topLeft.x + w * 0.14f, topLeft.y + h * 0.90f)
        lineTo(topLeft.x + w * 0.86f, topLeft.y + h * 0.90f)
        lineTo(topLeft.x + w * 0.74f, topLeft.y + h * 0.74f)
        lineTo(topLeft.x + w * 0.26f, topLeft.y + h * 0.74f)
        close()
    }
    drawPath(
        path = bottomShadowPath,
        color = color.shadow.copy(alpha = alpha * 0.6f)
    )

    // 7. Sharp, crisp outer rim border
    drawRoundRect(
        color = Color.White.copy(alpha = alpha * 0.45f),
        topLeft = topLeft,
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = w * 0.04f)
    )
}
