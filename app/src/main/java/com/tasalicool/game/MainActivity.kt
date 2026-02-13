package com.tarneeb.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarneeb.game.model.GamePhase
import com.tarneeb.game.ui.screens.*
import com.tarneeb.game.ui.theme.TarneebTheme
import com.tarneeb.game.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TarneebTheme(useDarkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TarneebGameApp()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun TarneebGameApp() {
    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()
    val currentPhase by viewModel.currentPhase.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showMainScreen by remember { mutableStateOf(true) }

    if (errorMessage != null) {
        // Show error dialog
        ErrorDialog(
            message = errorMessage ?: "Unknown error",
            onDismiss = { viewModel.clearError() }
        )
    }

    when {
        showMainScreen && gameState == null -> {
            MainScreen(
                onStartGame = {
                    viewModel.initializeGame("You", "AI 1", "Friend", "AI 2")
                    showMainScreen = false
                }
            )
        }
        gameState != null -> {
            val game = gameState!!
            when (game.gamePhase) {
                GamePhase.DEALING -> {
                    MainScreen(
                        onStartGame = {
                            viewModel.initializeGame("You", "AI 1", "Friend", "AI 2")
                        }
                    )
                }
                GamePhase.BIDDING -> {
                    BiddingScreen(viewModel = viewModel, game = game)
                }
                GamePhase.PLAYING -> {
                    GamePlayScreen(viewModel = viewModel, game = game)
                }
                GamePhase.GAME_END -> {
                    GameOverScreen(
                        game = game,
                        onPlayAgain = {
                            showMainScreen = true
                            viewModel.initializeGame("You", "AI 1", "Friend", "AI 2")
                        }
                    )
                }
                else -> {
                    MainScreen(onStartGame = {
                        viewModel.initializeGame("You", "AI 1", "Friend", "AI 2")
                    })
                }
            }
        }
    }
}

@androidx.compose.material3.Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            androidx.compose.material3.Text("Error")
        },
        text = {
            androidx.compose.material3.Text(message)
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("OK")
            }
        }
    )
}
