package com.example.blockblast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.blockblast.ui.BlockBlastGameScreen
import com.example.blockblast.ui.SplashScreen
import com.example.blockblast.ui.theme.BlockBlastTheme
import com.example.blockblast.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlockBlastTheme {
                var isSplashActive by remember { mutableStateOf(true) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0C1026)
                ) {
                    Crossfade(
                        targetState = isSplashActive,
                        animationSpec = tween(durationMillis = 350),
                        label = "splash_crossfade"
                    ) { showSplash ->
                        if (showSplash) {
                            SplashScreen(
                                onSplashFinished = { isSplashActive = false }
                            )
                        } else {
                            BlockBlastGameScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeApp()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseApp()
    }
}
