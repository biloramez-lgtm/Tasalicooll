package com.tasalicool.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasalicool.game.model.GamePhase
import com.tasalicool.game.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit
) {
    val game by viewModel.gameState.collectAsState()
    val error by viewModel.errorState.collectAsState()

    /* ======================= START GAME ONCE ======================= */
    LaunchedEffect(Unit) {
        viewModel.startGame("Team 1", "Team 2")
    }

    /* ======================= HANDLE GAME OVER ======================= */
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

            /* ======================= LOADING ======================= */
            if (game == null) {
                CircularProgressIndicator()
                return@Box
            }

            /* ======================= GAME SCREENS ======================= */
            when {
                game!!.isGameOver -> {
                    GameOverScreen(
                        game = game!!,
                        onPlayAgain = {
                            viewModel.restartGame()
                        }
                    )
                }

                else -> {
                    when (game!!.gamePhase) {

                        GamePhase.BIDDING -> {
                            BiddingScreen(
                                viewModel = viewModel,
                                game = game!!
                            )
                        }

                        GamePhase.PLAYING -> {
                            GamePlayScreen(
                                viewModel = viewModel,
                                game = game!!
                            )
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Preparing game...")
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            /* ======================= ERROR ======================= */
            error?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(it)
                }
            }
        }
    }
}
