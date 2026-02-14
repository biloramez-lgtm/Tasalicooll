package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Game

data class GameUiState(

    /* ---------- Core ---------- */
    val isLoading: Boolean = true,
    val game: Game? = null,

    /* ---------- Error & Retry ---------- */
    val error: UiError? = null,
    val canRetry: Boolean = false,

    /* ---------- UI Logic ---------- */
    val isMyTurn: Boolean = false,
    val canBid: Boolean = false,
    val canPlayCard: Boolean = false,
    val showGameOver: Boolean = false,

    /* ---------- Multiplayer ---------- */
    val multiplayer: MultiplayerState = MultiplayerState(),

    /* ---------- Sync / Debug ---------- */
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)
