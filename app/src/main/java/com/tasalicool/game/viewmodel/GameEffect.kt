package com.tasalicool.game.viewmodel

sealed class GameEffect {

    /* -------- Navigation -------- */
    object NavigateToGameOver : GameEffect()
    object NavigateToLobby : GameEffect()

    /* -------- UI Feedback -------- */
    data class ShowSnackbar(val message: String) : GameEffect()
    data class ShowDialog(
        val title: String,
        val message: String
    ) : GameEffect()

    /* -------- Animations -------- */
    object PlayCardAnimation : GameEffect()
    object PlayBidAnimation : GameEffect()

    /* -------- Sound / Haptics -------- */
    object PlayCardSound : GameEffect()
    object VibrateOnTurn : GameEffect()

    /* -------- Multiplayer -------- */
    object SendGameStateToPeer : GameEffect()
    object SyncWithPeer : GameEffect()
}
