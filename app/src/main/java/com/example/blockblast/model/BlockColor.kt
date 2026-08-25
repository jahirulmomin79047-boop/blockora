package com.example.blockblast.model

import androidx.compose.ui.graphics.Color
import com.example.blockblast.ui.theme.*

enum class BlockColor(
    val primary: Color,
    val highlight: Color,
    val shadow: Color,
    val glow: Color
) {
    RED(
        primary = Color(0xFFFF1744),
        highlight = Color(0xFFFF8A9E),
        shadow = Color(0xFFB70025),
        glow = Color(0x77FF1744)
    ),
    ORANGE(
        primary = Color(0xFFFF6D00),
        highlight = Color(0xFFFFAB66),
        shadow = Color(0xFFB84400),
        glow = Color(0x77FF6D00)
    ),
    YELLOW(
        primary = Color(0xFFFFD600),
        highlight = Color(0xFFFFF59D),
        shadow = Color(0xFFC69C00),
        glow = Color(0x77FFD600)
    ),
    GREEN(
        primary = Color(0xFF00E676),
        highlight = Color(0xFF81F7B8),
        shadow = Color(0xFF00963E),
        glow = Color(0x7700E676)
    ),
    CYAN(
        primary = Color(0xFF00E5FF),
        highlight = Color(0xFF8CF4FF),
        shadow = Color(0xFF0097A7),
        glow = Color(0x7700E5FF)
    ),
    BLUE(
        primary = Color(0xFF2979FF),
        highlight = Color(0xFF90CAF9),
        shadow = Color(0xFF1565C0),
        glow = Color(0x772979FF)
    ),
    PURPLE(
        primary = Color(0xFFAA00FF),
        highlight = Color(0xFFE1BEE7),
        shadow = Color(0xFF6A0080),
        glow = Color(0x77AA00FF)
    ),
    PINK(
        primary = Color(0xFFFF2A85),
        highlight = Color(0xFFFF94C2),
        shadow = Color(0xFFB3004C),
        glow = Color(0x77FF2A85)
    );

    companion object {
        fun random(): BlockColor = entries.random()
    }
}
