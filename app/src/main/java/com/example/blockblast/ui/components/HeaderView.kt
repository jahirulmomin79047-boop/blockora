package com.example.blockblast.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blockblast.model.GameState
import com.example.blockblast.ui.theme.*

@Composable
fun HeaderView(
    state: GameState,
    onToggleSfx: () -> Unit,
    onToggleBgm: () -> Unit,
    onOpenSettings: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun scoreFontSize(digits: Int): TextUnit = when {
        digits >= 7 -> 15.sp
        digits >= 5 -> 18.sp
        digits >= 4 -> 20.sp
        else -> 23.sp
    }

    val animatedScore by animateIntAsState(
        targetValue = state.score,
        animationSpec = tween(durationMillis = 120),
        label = "animated_score"
    )

    // Gentle scale-up animation on score increase
    val scoreScale = remember { Animatable(1f) }
    var previousScore by remember { mutableIntStateOf(state.score) }

    LaunchedEffect(state.score) {
        if (state.score > previousScore) {
            scoreScale.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(durationMillis = 85, easing = FastOutSlowInEasing)
            )
            scoreScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f)
            )
        }
        previousScore = state.score
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Action Bar: App title & Clean Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF7C4DFF))
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFFF2A85))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "B",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "BLOCKORA",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    letterSpacing = 1.sp
                )
            }

            // Action Buttons with crisp borders
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleSfx,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (state.soundEffectsEnabled) Color(0xFF2C1F4E) else Color(0xFF191233))
                        .border(1.2.dp, if (state.soundEffectsEnabled) Color(0xFF00E5FF) else Color(0xFF453378), CircleShape)
                        .testTag("toggle_sfx_button")
                ) {
                    Icon(
                        imageVector = if (state.soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = if (state.soundEffectsEnabled) "Mute Sound Effects" else "Unmute Sound Effects",
                        tint = if (state.soundEffectsEnabled) BlockCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleBgm,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (state.backgroundMusicEnabled) Color(0xFF2C1F4E) else Color(0xFF191233))
                        .border(1.2.dp, if (state.backgroundMusicEnabled) Color(0xFFFF2A85) else Color(0xFF453378), CircleShape)
                        .testTag("toggle_bgm_button")
                ) {
                    Icon(
                        imageVector = if (state.backgroundMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = if (state.backgroundMusicEnabled) "Turn Music Off" else "Turn Music On",
                        tint = if (state.backgroundMusicEnabled) BlockPink else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C1F4E))
                        .border(1.2.dp, Color(0xFF473672), CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Open Settings",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C1F4E))
                        .border(1.2.dp, Color(0xFF473672), CircleShape)
                        .testTag("restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Game",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Score Boards Row (Left: High Score Card, Right: Current Score Card)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. High Score Card
            val displayedHighScore = maxOf(state.sessionHighScore, state.highScore)
            val highScoreStr = displayedHighScore.toString()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = Color(0xFFFF2A85).copy(alpha = 0.35f),
                        spotColor = Color(0xFFFFD700).copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .clipToBounds()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF800C24), Color(0xFF480514))
                        )
                    )
                    .border(1.5.dp, Color(0xFFFF4757), RoundedCornerShape(14.dp))
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "HIGH SCORE",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = highScoreStr,
                        color = Color.White,
                        fontSize = scoreFontSize(highScoreStr.length),
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 2. Current Score Card
            val scoreStr = animatedScore.toString()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = Color(0xFF00E676).copy(alpha = 0.35f),
                        spotColor = Color(0xFF00E5FF).copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .clipToBounds()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0E523A), Color(0xFF072B1E))
                        )
                    )
                    .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(14.dp))
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SCORE",
                        color = Color(0xFF69F0AE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = scoreStr,
                        color = Color.White,
                        fontSize = scoreFontSize(scoreStr.length),
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scoreScale.value
                            scaleY = scoreScale.value
                        }
                    )
                }
            }
        }

        // Streak Progress Bar
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF241A44))
                .border(1.2.dp, Color(0xFF483778), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.streakProgress}/${state.streakGoal}",
                color = Color(0xFFD4CEFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { (state.streakProgress.toFloat() / state.streakGoal.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF9E00),
                trackColor = Color(0xFF140D2B),
                strokeCap = StrokeCap.Round
            )

            Text(
                text = "x${state.streakMultiplier}",
                color = Color(0xFFFFD200),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Combo Counter Banner (shows when combo > 1)
        AnimatedVisibility(
            visible = state.comboCount > 1,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF2A85), Color(0xFFFF7A00), Color(0xFFFFD200))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "COMBO x${state.comboCount}!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
