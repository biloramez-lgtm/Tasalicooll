package com.tasalicool.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasalicool.game.model.GamePhase
import com.tasalicool.game.viewmodel.GameAction
import com.tasalicool.game.viewmodel.GameEffect
import com.tasalicool.game.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToGameOver: () -> Unit
) {

    /* ================= STATE ================= */

    val uiState by viewModel.uiState.collectAsState()

    /* ================= EFFECT LISTENER ================= */

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {

                is GameEffect.NavigateToGameOver -> {
                    onNavigateToGameOver()
                }

                is GameEffect.ShowSnackbar -> {
                    // لاحقاً فيك تربط SnackbarHost
                }

                else -> {}
            }
        }
    }

    /* ================= UI ================= */

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

            /* ---------- Loading ---------- */

            if (uiState.isLoading) {
                CircularProgressIndicator()
                return@Box
            }

            val game = uiState.game ?: return@Box

            /* ---------- Phase Router ---------- */

            when (game.gamePhase) {

                /* ===== BIDDING ===== */

                GamePhase.BIDDING -> {
                    BiddingScreen(
                        viewModel = viewModel,
                        game = game
                    )
                }

                /* ===== PLAYING ===== */

                GamePhase.PLAYING -> {
                    GamePlayScreen(
                        game = game,
                        currentPlayer = game.players[game.currentPlayerIndex],
                        validCards = game.players[game.currentPlayerIndex].hand,
                        onCardPlay = { card ->
                            viewModel.onAction(
                                GameAction.PlayCard(
                                    playerIndex = game.currentPlayerIndex,
                                    card = card
                                )
                            )
                        },
                        onConcede = {
                            viewModel.onAction(GameAction.RestartGame)
                        },
                        isCurrentTurn = uiState.isMyTurn
                    )
                }

                /* ===== OTHER STATES ===== */

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
}
