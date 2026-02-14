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

    private fun observeRepository() {

        viewModelScope.launch {
            repository.gameState.collect { game ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    game = game
                )

                if (game?.isGameOver == true) {
                    sendEvent(GameEvent.NavigateToGameOver)
                }
            }
        }

        viewModelScope.launch {
            repository.error.collect { error ->
                error?.let {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = it,
                        event = GameEvent.ShowSnackbar(it)
                    )
                }
            }
        }
    }

    /* ================= EVENTS ================= */

    private fun sendEvent(event: GameEvent) {
        _uiState.value = _uiState.value.copy(event = event)
    }

    fun consumeEvent() {
        _uiState.value = _uiState.value.copy(event = null)
    }

    /* ================= GAME FLOW ================= */

    fun startGame(team1: String, team2: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.startGame(team1, team2)
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
            sendEvent(GameEvent.PlayCardAnimation)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        repository.clearError()
    }
}
