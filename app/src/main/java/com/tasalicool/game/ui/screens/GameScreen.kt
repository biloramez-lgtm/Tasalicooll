package com.tasalicool.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasalicool.game.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit
) {
    val game by viewModel.gameState.collectAsState()
    val error by viewModel.errorState.collectAsState()

    // ✅ Start game once
    LaunchedEffect(Unit) {
        viewModel.startGame("Team 1", "Team 2")
    }

    // ✅ Handle game over safely
    LaunchedEffect(game?.isGameOver) {
        if (game?.isGameOver == true) {
            onGameOver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasalicool Card Game") }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {

            if (game == null) {
                CircularProgressIndicator()
                return@Box
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Phase: ${game!!.gamePhase}")
                Text("Current Player: ${game!!.currentPlayerToPlayIndex}")

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { viewModel.restartGame() }) {
                    Text("Restart Game")
                }
            }
        }
    }
}
