package com.tasalicool.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasalicool.game.model.Card
import com.tasalicool.game.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
    }

    /* ================= OBSERVE REPOSITORY ================= */

    private fun observeRepository() {
        viewModelScope.launch {
            repository.gameState.collect { game ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    game = game
                )
            }
        }

        viewModelScope.launch {
            repository.error.collect { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error
                )
            }
        }
    }

    /* ================= GAME FLOW ================= */

    fun startGame(team1Name: String, team2Name: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.startGame(team1Name, team2Name)
        }
    }

    fun restartGame() {
        _uiState.value = GameUiState(isLoading = true)
        repository.restartGame()
    }

    /* ================= BIDDING ================= */

    fun placeBid(playerIndex: Int, bid: Int) {
        viewModelScope.launch {
            repository.placeBid(playerIndex, bid)
        }
    }

    /* ================= PLAYING ================= */

    fun playCard(playerIndex: Int, card: Card) {
        viewModelScope.launch {
            repository.playCard(playerIndex, card)
        }
    }

    /* ================= ERROR ================= */

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        repository.clearError()
    }
}
