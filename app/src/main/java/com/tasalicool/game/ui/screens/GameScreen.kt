package com.tasalicool.game.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.tasalicool.game.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit
) {
    val game by viewModel.gameState.collectAsState()
    val error by viewModel.errorState.collectAsState()

    if (game == null) {
        Text("Loading game...")
        return
    }

    if (game!!.isGameOver) {
        onGameOver()
        return
    }

    Column {
        Text("Phase: ${game!!.gamePhase}")
        Text("Current Player: ${game!!.currentPlayerToPlayIndex}")

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
