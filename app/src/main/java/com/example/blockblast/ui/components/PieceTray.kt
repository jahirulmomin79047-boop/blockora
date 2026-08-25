package com.example.blockblast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.blockblast.model.BlockShape

@Composable
fun PieceTray(
    pieces: List<BlockShape?>,
    activeDragIndex: Int?,
    onDragStart: (Int, Offset, BlockShape) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pieces.forEachIndexed { index, shape ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("piece_slot_$index"),
                contentAlignment = Alignment.Center
            ) {
                if (shape != null) {
                    var layoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                    val isBeingDragged = activeDragIndex == index

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .onGloballyPositioned { coordinates ->
                                layoutCoords = coordinates
                            }
                            .pointerInput(shape.id) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val coords = layoutCoords ?: return@awaitEachGesture
                                    val startPosInRoot = coords.localToRoot(down.position)

                                    onDragStart(index, startPosInRoot, shape)
                                    down.consume()

                                    var currentRootPos = startPosInRoot

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()

                                        if (change == null || !change.pressed) {
                                            // Pointer released / lifted
                                            change?.consume()
                                            val finalPos = layoutCoords?.let {
                                                if (change != null) it.localToRoot(change.position) else currentRootPos
                                            } ?: currentRootPos
                                            onDragEnd(finalPos)
                                            break
                                        } else {
                                            currentRootPos = layoutCoords?.localToRoot(change.position) ?: currentRootPos
                                            change.consume()
                                            onDrag(currentRootPos)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isBeingDragged) {
                            // 1. Soft matching glow aura behind the piece
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension * 0.44f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            shape.color.primary.copy(alpha = 0.22f),
                                            shape.color.highlight.copy(alpha = 0.08f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = radius
                                    ),
                                    radius = radius,
                                    center = center
                                )
                            }

                            // 2. Crisp 3D Glossy Piece
                            PieceCanvas(
                                shape = shape,
                                maxBoxSize = 90f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieceCanvas(
    shape: BlockShape,
    maxBoxSize: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size((maxBoxSize * 1.05f).dp)
    ) {
        val maxDim = maxOf(shape.width, shape.height, 1)
        val availableSize = size.minDimension
        val cellSize = (availableSize / maxDim).coerceAtMost(36.dp.toPx())
        val blockPadding = cellSize * 0.065f
        val blockSize = cellSize - (blockPadding * 2)
        val cornerRadius = CornerRadius(blockSize * 0.2f, blockSize * 0.2f)

        val totalShapeWidth = shape.width * cellSize
        val totalShapeHeight = shape.height * cellSize
        val startX = (size.width - totalShapeWidth) / 2f
        val startY = (size.height - totalShapeHeight) / 2f

        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.matrix[r][c]) {
                    val x = startX + c * cellSize + blockPadding
                    val y = startY + r * cellSize + blockPadding

                    drawBlock(
                        color = shape.color,
                        topLeft = Offset(x, y),
                        size = Size(blockSize, blockSize),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}
