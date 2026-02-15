package com.tasalicool.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.ui.GameScreen
import com.tasalicool.game.ui.theme.TasalicoollTheme

class MainActivity : ComponentActivity() {

    private val engine = GameEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TasalicoollTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    GameScreen(engine = engine)
                }
            }
        }
        engine.initializeDefaultGame("TeamA", "TeamB")
    }
}
