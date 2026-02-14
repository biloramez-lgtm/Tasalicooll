package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Game

/**
 * Single source of truth for the whole Game UI
 * Production ready – scalable – multiplayer friendly
 */
data class GameUiState(

    /* ================= CORE ================= */
    val isLoading: Boolean = true,
    val game: Game? = null,

    /* ================= ERROR / RETRY ================= */
    val error: UiError? = null,
    val canRetry: Boolean = false,

    /* ================= UI HELPERS ================= */
    val isMyTurn: Boolean = false,
    val canBid: Boolean = false,
    val canPlayCard: Boolean = false,
    val showGameOver: Boolean = false,

    /* ================= MULTIPLAYER ================= */
    val multiplayer: MultiplayerState = MultiplayerState(),

    /* ================= SYNC / DEBUG ================= */
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)
