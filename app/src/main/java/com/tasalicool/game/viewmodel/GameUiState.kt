package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Game

data class GameUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val errorMessage: String? = null,

    // UI helpers
    val isMyTurn: Boolean = false,
    val canBid: Boolean = false,
    val canPlayCard: Boolean = false,

    // Multiplayer
    val multiplayerState: MultiplayerState = MultiplayerState()
)
