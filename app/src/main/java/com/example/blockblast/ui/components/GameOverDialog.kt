package com.example.blockblast.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameOverDialog(
    score: Int,
    highScore: Int,
    isNewHighScore: Boolean,
    totalLinesCleared: Int,
    onRestart: () -> Unit
) {
    // Entrance animation controllers
    var isVisible by remember { mutableStateOf(false) }
    var animatedScore by remember { mutableIntStateOf(0) }
    val cardScale = remember { Animatable(0.7f) }
    val cardAlpha = remember { Animatable(0f) }

    // Pulsing button glow
    val infiniteTransition = rememberInfiniteTransition(label = "gameOverGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Trigger sequential animations
    LaunchedEffect(Unit) {
        isVisible = true
        // Card pop-in spring
        launch {
            cardAlpha.animateTo(1f, tween(300))
        }
        launch {
            cardScale.animateTo(1.04f, tween(280, easing = FastOutSlowInEasing))
            cardScale.animateTo(1.0f, spring(dampingRatio = 0.65f, stiffness = 400f))
        }

        // Animated score roll-up
        delay(250)
        val durationMs = 1100L
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < durationMs) {
            val progress = ((System.currentTimeMillis() - startTime).toFloat() / durationMs).coerceIn(0f, 1f)
            // Ease out cubic
            val eased = 1f - (1f - progress) * (1f - progress) * (1f - progress)
            animatedScore = (score * eased).toInt()
            delay(16)
        }
        animatedScore = score
    }

    // Dismiss animation handler on Play Again
    var isExiting by remember { mutableStateOf(false) }
    val handleRestart = {
        if (!isExiting) {
            isExiting = true
            onRestart()
        }
    }

    // Fullscreen Overlay with frosted dark backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC080B1E))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Intercept clicks outside dialog */ }
            )
            .testTag("game_over_dialog"),
        contentAlignment = Alignment.Center
    ) {
        // Decorative ambient particles / confetti for new best score
        if (isNewHighScore) {
            CelebrationParticleField()
        }

        // Modern Glowing Game Over Modal Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .graphicsLayer {
                    scaleX = cardScale.value
                    scaleY = cardScale.value
                    alpha = cardAlpha.value
                }
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = if (isNewHighScore) Color(0xFFFFD200).copy(alpha = 0.6f) else Color(0xFFFF2A85).copy(alpha = 0.55f),
                    spotColor = Color(0xFF00E5FF).copy(alpha = 0.45f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF221644),
                            Color(0xFF160F30),
                            Color(0xFF0D0922)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFF2A85),
                            Color(0xFF7C4DFF),
                            Color(0xFF00E5FF),
                            Color(0xFFFFD200),
                            Color(0xFFFF2A85)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isNewHighScore) {
                                    listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                                } else {
                                    listOf(Color(0xFFFF1744).copy(alpha = 0.25f), Color(0xFFFF5252).copy(alpha = 0.15f))
                                }
                            )
                        )
                        .border(
                            1.dp,
                            if (isNewHighScore) Color(0xFFFFD200).copy(alpha = 0.8f) else Color(0xFFFF1744).copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isNewHighScore) "🏆 NEW RECORD REACHED!" else "NO MOVES LEFT",
                        color = if (isNewHighScore) Color.White else Color(0xFFFF5252),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = "GAME OVER",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Score Card with Glowing Neon Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1D1438),
                                    Color(0xFF120C26)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.6f),
                                    Color(0xFF7C4DFF).copy(alpha = 0.7f),
                                    Color(0xFFFF2A85).copy(alpha = 0.6f)
                                )
                            ),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FINAL SCORE",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = animatedScore.toString(),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row (All-time Best & Total Blasts)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Best Score Tile
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF181033))
                            .border(1.dp, Color(0xFFFFD200).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD200),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "BEST",
                                    color = Color(0xFFFFD200),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = highScore.toString(),
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Total Lines Cleared / Blasts Tile
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF181033))
                            .border(1.dp, Color(0xFF69F0AE).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFF69F0AE),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "BLASTS",
                                    color = Color(0xFF69F0AE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = totalLinesCleared.toString(),
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Play Again Button with Pulsing Emerald-Cyan Glow
                Button(
                    onClick = handleRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = (12 * pulseGlow).dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF00E676).copy(alpha = 0.6f),
                            spotColor = Color(0xFF00E5FF).copy(alpha = 0.7f)
                        )
                        .testTag("play_again_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF00E5FF)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFF0A1A12),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "PLAY AGAIN",
                                color = Color(0xFF0A1A12),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CelebrationParticleField() {
    val particles = remember {
        List(24) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 80f + Random.nextFloat() * 160f
            val color = when (it % 4) {
                0 -> Color(0xFFFFD200)
                1 -> Color(0xFFFF2A85)
                2 -> Color(0xFF00E5FF)
                else -> Color(0xFF00E676)
            }
            CelebrationParticle(
                x = cos(angle) * speed,
                y = sin(angle) * speed,
                color = color,
                size = 4f + Random.nextFloat() * 6f
            )
        }
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.35f
        val progress = animProgress.value

        particles.forEach { p ->
            val curX = centerX + p.x * progress
            val curY = centerY + p.y * progress + (progress * progress * 60f) // gravity
            val curAlpha = (1f - progress).coerceIn(0f, 1f)

            drawCircle(
                color = p.color.copy(alpha = curAlpha),
                radius = p.size * (1f - progress * 0.3f),
                center = Offset(curX, curY)
            )
        }
    }
}

private data class CelebrationParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float
)
