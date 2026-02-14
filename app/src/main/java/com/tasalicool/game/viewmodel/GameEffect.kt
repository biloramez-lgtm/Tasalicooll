package com.tasalicool.game.viewmodel

sealed class GameEffect {
    data class ShowToast(val message: String) : GameEffect()
    object NavigateToGameOver : GameEffect()
}
