package com.example.blockblast.model

import java.util.UUID

data class BlockShape(
    val id: String = UUID.randomUUID().toString(),
    val matrix: List<List<Boolean>>,
    val color: BlockColor
) {
    val height: Int = matrix.size
    val width: Int = if (matrix.isNotEmpty()) matrix[0].size else 0
    val blockCount: Int = matrix.sumOf { row -> row.count { it } }

    companion object {
        // Preset shape templates: (rows of 0s and 1s)
        private val TEMPLATES: List<Pair<List<List<Int>>, List<BlockColor>>> = listOf(
            // 1x1 Dot
            listOf(listOf(1)) to listOf(BlockColor.YELLOW, BlockColor.CYAN, BlockColor.PINK),

            // 2x1 & 1x2 Dominoes
            listOf(listOf(1, 1)) to listOf(BlockColor.CYAN, BlockColor.GREEN),
            listOf(listOf(1), listOf(1)) to listOf(BlockColor.CYAN, BlockColor.GREEN),

            // 3x1 & 1x3 Lines
            listOf(listOf(1, 1, 1)) to listOf(BlockColor.BLUE, BlockColor.ORANGE),
            listOf(listOf(1), listOf(1), listOf(1)) to listOf(BlockColor.BLUE, BlockColor.ORANGE),

            // 4x1 & 1x4 Lines
            listOf(listOf(1, 1, 1, 1)) to listOf(BlockColor.PURPLE, BlockColor.CYAN),
            listOf(listOf(1), listOf(1), listOf(1), listOf(1)) to listOf(BlockColor.PURPLE, BlockColor.CYAN),

            // 5x1 & 1x5 Lines
            listOf(listOf(1, 1, 1, 1, 1)) to listOf(BlockColor.RED, BlockColor.BLUE),
            listOf(listOf(1), listOf(1), listOf(1), listOf(1), listOf(1)) to listOf(BlockColor.RED, BlockColor.BLUE),

            // 2x2 Square
            listOf(
                listOf(1, 1),
                listOf(1, 1)
            ) to listOf(BlockColor.YELLOW, BlockColor.ORANGE),

            // 3x3 Square
            listOf(
                listOf(1, 1, 1),
                listOf(1, 1, 1),
                listOf(1, 1, 1)
            ) to listOf(BlockColor.RED, BlockColor.PURPLE),

            // Small 2x2 Corner L (4 orientations)
            listOf(
                listOf(1, 0),
                listOf(1, 1)
            ) to listOf(BlockColor.GREEN, BlockColor.PINK),
            listOf(
                listOf(0, 1),
                listOf(1, 1)
            ) to listOf(BlockColor.GREEN, BlockColor.PINK),
            listOf(
                listOf(1, 1),
                listOf(1, 0)
            ) to listOf(BlockColor.GREEN, BlockColor.PINK),
            listOf(
                listOf(1, 1),
                listOf(0, 1)
            ) to listOf(BlockColor.GREEN, BlockColor.PINK),

            // Big 3x3 Corner L (4 orientations)
            listOf(
                listOf(1, 0, 0),
                listOf(1, 0, 0),
                listOf(1, 1, 1)
            ) to listOf(BlockColor.ORANGE, BlockColor.CYAN),
            listOf(
                listOf(0, 0, 1),
                listOf(0, 0, 1),
                listOf(1, 1, 1)
            ) to listOf(BlockColor.ORANGE, BlockColor.CYAN),
            listOf(
                listOf(1, 1, 1),
                listOf(1, 0, 0),
                listOf(1, 0, 0)
            ) to listOf(BlockColor.ORANGE, BlockColor.CYAN),
            listOf(
                listOf(1, 1, 1),
                listOf(0, 0, 1),
                listOf(0, 0, 1)
            ) to listOf(BlockColor.ORANGE, BlockColor.CYAN),

            // Classic 3x2 L shape (4 orientations)
            listOf(
                listOf(1, 0),
                listOf(1, 0),
                listOf(1, 1)
            ) to listOf(BlockColor.BLUE, BlockColor.PURPLE),
            listOf(
                listOf(0, 1),
                listOf(0, 1),
                listOf(1, 1)
            ) to listOf(BlockColor.BLUE, BlockColor.PURPLE),
            listOf(
                listOf(1, 1, 1),
                listOf(1, 0, 0)
            ) to listOf(BlockColor.BLUE, BlockColor.PURPLE),
            listOf(
                listOf(1, 1, 1),
                listOf(0, 0, 1)
            ) to listOf(BlockColor.BLUE, BlockColor.PURPLE),

            // T Shapes (4 orientations)
            listOf(
                listOf(1, 1, 1),
                listOf(0, 1, 0)
            ) to listOf(BlockColor.PURPLE, BlockColor.PINK),
            listOf(
                listOf(0, 1, 0),
                listOf(1, 1, 1)
            ) to listOf(BlockColor.PURPLE, BlockColor.PINK),
            listOf(
                listOf(1, 0),
                listOf(1, 1),
                listOf(1, 0)
            ) to listOf(BlockColor.PURPLE, BlockColor.PINK),
            listOf(
                listOf(0, 1),
                listOf(1, 1),
                listOf(0, 1)
            ) to listOf(BlockColor.PURPLE, BlockColor.PINK),

            // Z & S shapes
            listOf(
                listOf(1, 1, 0),
                listOf(0, 1, 1)
            ) to listOf(BlockColor.RED, BlockColor.GREEN),
            listOf(
                listOf(0, 1, 1),
                listOf(1, 1, 0)
            ) to listOf(BlockColor.GREEN, BlockColor.YELLOW),
            listOf(
                listOf(1, 0),
                listOf(1, 1),
                listOf(0, 1)
            ) to listOf(BlockColor.RED, BlockColor.GREEN),
            listOf(
                listOf(0, 1),
                listOf(1, 1),
                listOf(1, 0)
            ) to listOf(BlockColor.GREEN, BlockColor.YELLOW)
        )

        fun createRandom(): BlockShape {
            val (template, colors) = TEMPLATES.random()
            val matrix = template.map { row -> row.map { it == 1 } }
            val color = colors.randomOrNull() ?: BlockColor.random()
            return BlockShape(matrix = matrix, color = color)
        }

        fun generatePieceSet(): List<BlockShape> {
            return listOf(createRandom(), createRandom(), createRandom())
        }
    }
}
