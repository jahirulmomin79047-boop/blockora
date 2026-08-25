package com.example.blockblast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.blockblast.ui.theme.*

@Composable
fun SettingsDialog(
    soundEffectsEnabled: Boolean,
    backgroundMusicEnabled: Boolean,
    onToggleSoundEffects: () -> Unit,
    onToggleBackgroundMusic: () -> Unit,
    onResetHighScore: () -> Unit,
    onDismiss: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    val dialogBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFF7C4DFF),
            Color(0xFFFF2A85)
        )
    )

    val dialogBgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF26184C),
            Color(0xFF180F33),
            Color(0xFF0E0822)
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF7C4DFF))
                .clip(RoundedCornerShape(24.dp))
                .background(dialogBgBrush)
                .border(2.dp, dialogBorderBrush, RoundedCornerShape(24.dp))
                .testTag("settings_dialog"),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Game Settings",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Sound Effects Toggle Item
                SettingToggleRow(
                    title = "Sound Effects",
                    subtitle = "Blast chimes, placement and combos",
                    icon = if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    iconColor = BlockCyan,
                    isChecked = soundEffectsEnabled,
                    onCheckedChange = { onToggleSoundEffects() },
                    testTag = "sfx_switch"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Background Music Toggle Item
                SettingToggleRow(
                    title = "Background Music",
                    subtitle = "Relaxing melodic game soundtrack",
                    icon = if (backgroundMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    iconColor = BlockPink,
                    isChecked = backgroundMusicEnabled,
                    onCheckedChange = { onToggleBackgroundMusic() },
                    testTag = "bgm_switch"
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFF3F2D6B), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Game Info / Guide
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E143B))
                        .border(1.dp, Color(0xFF3F2D6B), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BlockYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "How to Play",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Drag blocks onto the 8x8 grid. Complete full horizontal or vertical lines to blast them and trigger combos!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Reset Best Score Button
                if (!showResetConfirm) {
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_high_score_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextMuted
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF473672))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Best Score",
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showResetConfirm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                onResetHighScore()
                                showResetConfirm = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlockRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E143B))
            .border(1.dp, Color(0xFF3F2D6B), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BlockCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color(0xFF160E2E)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
