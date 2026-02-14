package com.tasalicool.game.viewmodel

sealed class GameEvent {

    /* -------- Navigation -------- */
    object NavigateToGameOver : GameEvent()

    /* -------- UI Messages -------- */
    data class ShowSnackbar(val message: String) : GameEvent()

    /* -------- Retry -------- */
    object Retry : GameEvent()

    /* -------- Animations / Effects -------- */
    object PlayCardAnimation : GameEvent()
}
