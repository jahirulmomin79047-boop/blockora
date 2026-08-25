package com.example.blockblast.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Timed progress animation for loading bar (0f to 1f over 1.6s)
    val progressAnim = remember { Animatable(0f) }
    val entryAlpha = remember { Animatable(0f) }
    val entryScale = remember { Animatable(0.82f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")

    // Continuous floating and bobbing animations for emojis & 3D blocks
    val floatAnim1 by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    val floatAnim3 by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float3"
    )
    // Pulsing golden glow
    val goldenPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "golden_pulse"
    )
    // Specular shine sweep across the 3D logo
    val shineSweep by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine_sweep"
    )

    LaunchedEffect(Unit) {
        // Smooth scale-up & fade-in entry
        entryAlpha.animateTo(1f, animationSpec = tween(380, easing = EaseOutCubic))
        entryScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow))

        // Progress bar smooth fill
        progressAnim.animateTo(1f, animationSpec = tween(durationMillis = 1450, easing = FastOutSlowInEasing))

        // Brief delay before transitioning to the game
        delay(180)
        onSplashFinished()
    }

    // Warm golden/brown background gradient with rich ambient depth
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFBEA), // Soft warm champagne highlight
            Color(0xFFFEEFB3), // Honey gold
            Color(0xFFF7D58B), // Warm amber gold
            Color(0xFFE8B25A), // Golden caramel
            Color(0xFFC9842F), // Warm golden brown
            Color(0xFF8A4E17)  // Deep chestnut base
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Background golden radial aura & ambient lighting
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Center large golden glowing bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x88FFD54F),
                        Color(0x55F59E0B),
                        Color(0x22FBBF24),
                        Color(0x00FBBF24)
                    ),
                    center = Offset(w * 0.5f, h * 0.44f),
                    radius = w * 0.85f * goldenPulse
                ),
                radius = w * 0.85f * goldenPulse,
                center = Offset(w * 0.5f, h * 0.44f)
            )

            // Top-left golden flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x40FFE082), Color(0x00FFE082)),
                    center = Offset(w * 0.15f, h * 0.15f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.15f, h * 0.15f)
            )

            // Bottom warm amber flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x35FB923C), Color(0x00FB923C)),
                    center = Offset(w * 0.5f, h * 0.88f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.5f, h * 0.88f)
            )

            // Floating 3D Isometric Puzzle Blocks around screen
            // Block 1: Cyan Cube (Top-Left)
            drawIsometricCube(
                center = Offset(w * 0.14f, h * 0.24f + floatAnim1 * 2.2f),
                size = 32f,
                topColor = Color(0xFF67E8F9),
                leftColor = Color(0xFF06B6D4),
                rightColor = Color(0xFF0891B2),
                rotationAngle = 12f + floatAnim1
            )

            // Block 2: Vivid Pink Cube (Top-Right)
            drawIsometricCube(
                center = Offset(w * 0.86f, h * 0.22f + floatAnim2 * 2f),
                size = 34f,
                topColor = Color(0xFFF472B6),
                leftColor = Color(0xFFEC4899),
                rightColor = Color(0xFFDB2777),
                rotationAngle = -15f + floatAnim2
            )

            // Block 3: Emerald Green Cube (Mid-Left)
            drawIsometricCube(
                center = Offset(w * 0.09f, h * 0.56f + floatAnim3 * 2f),
                size = 30f,
                topColor = Color(0xFF6EE7B7),
                leftColor = Color(0xFF10B981),
                rightColor = Color(0xFF059669),
                rotationAngle = floatAnim3 * 1.5f
            )

            // Block 4: Purple Cube (Mid-Right)
            drawIsometricCube(
                center = Offset(w * 0.91f, h * 0.58f + floatAnim1 * 2f),
                size = 32f,
                topColor = Color(0xFFC084FC),
                leftColor = Color(0xFFA855F7),
                rightColor = Color(0xFF7E22CE),
                rotationAngle = -10f + floatAnim1
            )

            // Block 5: Sunny Yellow Cube (Bottom-Left)
            drawIsometricCube(
                center = Offset(w * 0.16f, h * 0.80f + floatAnim2 * 1.8f),
                size = 28f,
                topColor = Color(0xFFFDE047),
                leftColor = Color(0xFFFACC15),
                rightColor = Color(0xFFCA8A04),
                rotationAngle = floatAnim2 * 2f
            )

            // Block 6: Orange/Coral Cube (Bottom-Right)
            drawIsometricCube(
                center = Offset(w * 0.84f, h * 0.78f + floatAnim3 * 2.2f),
                size = 30f,
                topColor = Color(0xFFFDBA74),
                leftColor = Color(0xFFFB923C),
                rightColor = Color(0xFFEA580C),
                rotationAngle = 8f - floatAnim3
            )
        }

        // Center Content Column (3D Logo, Tagline & Loading)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = entryAlpha.value
                    scaleX = entryScale.value
                    scaleY = entryScale.value
                }
        ) {
            // 3D Logo Plate Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 12.dp)
            ) {
                // 3D Backing Plate with 3D BLOCKORA Letters
                Blockora3DLogoPlate(shineSweep = shineSweep)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subtitle Tagline Pill: "PUZZLE BLAST ADVENTURE"
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0xFFD97706).copy(alpha = 0.35f),
                        spotColor = Color(0xFFFFD200).copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFFBEB),
                                Color(0xFFFEF3C7),
                                Color(0xFFFDE68A)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFF59E0B), Color(0xFFD97706))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 22.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "PUZZLE BLAST ADVENTURE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF78350F),
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(52.dp))

            // Loading Bar Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 44.dp)
            ) {
                // Golden Glow Shadow Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(7.dp),
                            ambientColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
                            spotColor = Color(0xFFFFD700).copy(alpha = 0.7f)
                        )
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF422206)) // Deep warm chocolate backing
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFFFE082), Color(0xFFF59E0B), Color(0xFFB45309))
                            ),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .padding(2.dp)
                ) {
                    // Animated Golden/Yellow Progress Bar Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAnim.value)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF7A00),
                                        Color(0xFFFFB703),
                                        Color(0xFFFFF066),
                                        Color(0xFFFFD700)
                                    )
                                )
                            )
                    ) {
                        // Glossy top-half shine on progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.45f)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.05f))
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "LOADING..." Label with Glowing Golden Typography
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "LOADING...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 3.sp,
                        color = Color(0xFFFFFBEA),
                        modifier = Modifier.shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(4.dp),
                            ambientColor = Color(0xFF78350F)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(progressAnim.value * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFE082),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * 3D Golden/Orange Backing Plate hosting 8 glossy colorful 3D block letters for "B L O C K O R A"
 */
@Composable
private fun Blockora3DLogoPlate(
    shineSweep: Float,
    modifier: Modifier = Modifier
) {
    // Definition of the 8 3D block letters: Character, Top Gradient, Bottom/Side Depth Color
    val letterBlocks = remember {
        listOf(
            Triple("B", listOf(Color(0xFF4ADE80), Color(0xFF16A34A)), Color(0xFF14532D)), // Emerald Green
            Triple("L", listOf(Color(0xFF38BDF8), Color(0xFF0284C7)), Color(0xFF075985)), // Sky Blue
            Triple("O", listOf(Color(0xFFFBBF24), Color(0xFFD97706)), Color(0xFF78350F)), // Golden Amber
            Triple("C", listOf(Color(0xFFF472B6), Color(0xFFDB2777)), Color(0xFF831843)), // Hot Pink
            Triple("K", listOf(Color(0xFFC084FC), Color(0xFF9333EA)), Color(0xFF581C87)), // Royal Purple
            Triple("O", listOf(Color(0xFFFB923C), Color(0xFFEA580C)), Color(0xFF7C2D12)), // Vibrant Orange
            Triple("R", listOf(Color(0xFF2DD4BF), Color(0xFF0D9488)), Color(0xFF134E4A)), // Teal Cyan
            Triple("A", listOf(Color(0xFFFB7185), Color(0xFFE11D48)), Color(0xFF881337))  // Ruby Red
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 18.dp)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0xFFB45309).copy(alpha = 0.7f),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            )
            .clip(RoundedCornerShape(32.dp))
            // 3D Multi-Layered Golden Backing Plate Gradient
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFDF7A), // Top golden highlight bevel
                        Color(0xFFF59E0B), // Warm amber gold
                        Color(0xFFD97706), // Rich orange gold
                        Color(0xFF92400E), // Deep gold plate base
                        Color(0xFF451A03)  // Bottom 3D extrude shadow
                    )
                )
            )
            .border(
                width = 3.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF7CC),
                        Color(0xFFFFD54F),
                        Color(0xFFF59E0B),
                        Color(0xFF78350F)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        // Inner Dark Recessed Inset for high contrast popping of block letters
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0A2E), // Deep obsidian purple
                            Color(0xFF11061F)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF4C1D95).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 10.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Row of 8 3D Glossy Puzzle Block Letters
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                letterBlocks.forEach { (char, faceGradient, depthColor) ->
                    Glossy3DBlockLetter(
                        letter = char,
                        faceColors = faceGradient,
                        depthColor = depthColor
                    )
                }
            }
        }
    }
}

/**
 * Individual 3D Glossy Puzzle Block Letter with 3D extrusion, beveled face, specular highlight & bold typography
 */
@Composable
private fun Glossy3DBlockLetter(
    letter: String,
    faceColors: List<Color>,
    depthColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(34.dp)
            .height(46.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(9.dp),
                ambientColor = depthColor.copy(alpha = 0.6f),
                spotColor = faceColors.first().copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(9.dp))
            // 3D Bottom/Extrusion Darker Shading
            .background(depthColor)
    ) {
        // 3D Front Face Block (Slightly raised with 3.5dp bottom lip for 3D extrusion effect)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp, bottomStart = 7.dp, bottomEnd = 7.dp))
                .background(Brush.verticalGradient(faceColors))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.1f))
                    ),
                    shape = RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp, bottomStart = 7.dp, bottomEnd = 7.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glossy top specular sheen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                        )
                    )
            )

            // Letter shadow for 3D embedded look
            Text(
                text = letter,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color.Black.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = 1.5.dp)
            )

            // Crisp White Lettering
            Text(
                text = letter,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Draws a 3D Isometric Jewel Cube with lit top face, shaded left face, and dark right face.
 */
private fun DrawScope.drawIsometricCube(
    center: Offset,
    size: Float,
    topColor: Color,
    leftColor: Color,
    rightColor: Color,
    rotationAngle: Float
) {
    val hSize = size * 0.5f
    val rad = Math.toRadians(rotationAngle.toDouble())
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()

    fun rotate(x: Float, y: Float): Offset {
        val rx = x * cosA - y * sinA
        val ry = x * sinA + y * cosA
        return Offset(center.x + rx, center.y + ry)
    }

    // Top Face (Rhombus)
    val pTop = rotate(0f, -size * 0.75f)
    val pRight = rotate(size * 0.85f, -size * 0.25f)
    val pBottom = rotate(0f, size * 0.25f)
    val pLeft = rotate(-size * 0.85f, -size * 0.25f)

    val topPath = Path().apply {
        moveTo(pTop.x, pTop.y)
        lineTo(pRight.x, pRight.y)
        lineTo(pBottom.x, pBottom.y)
        lineTo(pLeft.x, pLeft.y)
        close()
    }
    drawPath(topPath, color = topColor, style = Fill)

    // Left Face
    val pLeftBottom = rotate(-size * 0.85f, size * 0.65f)
    val pBottomBottom = rotate(0f, size * 1.15f)

    val leftPath = Path().apply {
        moveTo(pLeft.x, pLeft.y)
        lineTo(pBottom.x, pBottom.y)
        lineTo(pBottomBottom.x, pBottomBottom.y)
        lineTo(pLeftBottom.x, pLeftBottom.y)
        close()
    }
    drawPath(leftPath, color = leftColor, style = Fill)

    // Right Face
    val pRightBottom = rotate(size * 0.85f, size * 0.65f)

    val rightPath = Path().apply {
        moveTo(pRight.x, pRight.y)
        lineTo(pBottom.x, pBottom.y)
        lineTo(pBottomBottom.x, pBottomBottom.y)
        lineTo(pRightBottom.x, pRightBottom.y)
        close()
    }
    drawPath(rightPath, color = rightColor, style = Fill)

    // Specular Bevel Highlights
    drawPath(topPath, color = Color.White.copy(alpha = 0.55f), style = Stroke(width = 1.8f))
}
