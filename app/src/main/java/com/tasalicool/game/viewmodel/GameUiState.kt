package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Game

data class GameUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val errorMessage: String? = null
)
