package com.tasalicool.game.viewmodel

sealed class GameEffect {

    /* ================= NAVIGATION ================= */

    object NavigateToGameOver : GameEffect()
    object NavigateBack : GameEffect()

    /* ================= UI FEEDBACK ================= */

    data class ShowSnackbar(
        val message: String,
        val isError: Boolean = false
    ) : GameEffect()

    data class ShowDialog(
        val title: String,
        val message: String
    ) : GameEffect()

    /* ================= GAME VISUALS ================= */

    object PlayCardAnimation : GameEffect()
    object DealCardsAnimation : GameEffect()
    object BiddingAnimation : GameEffect()

    /* ================= MULTIPLAYER ================= */

    object PlayerConnected : GameEffect()
    object PlayerDisconnected : GameEffect()
    object SyncStarted : GameEffect()
    object SyncCompleted : GameEffect()

    /* ================= SYSTEM ================= */

    object Vibrate : GameEffect()
}
