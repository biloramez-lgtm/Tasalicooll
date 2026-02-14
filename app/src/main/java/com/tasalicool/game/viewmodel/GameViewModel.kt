package com.tasalicool.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasalicool.game.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<GameEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeGame()
    }

    /* ================= ACTION HANDLER ================= */

    fun onAction(action: GameAction) {
        when (action) {

            is GameAction.StartGame -> startGame(action.team1, action.team2)
            is GameAction.RestartGame -> restartGame()

            is GameAction.PlaceBid ->
                repository.placeBid(action.playerIndex, action.bid)

            is GameAction.PlayCard ->
                repository.playCard(action.playerIndex, action.card)

            GameAction.ClearError ->
                clearError()

            /* Multiplayer (جاهز) */
            GameAction.CreateRoom -> createRoom()
            GameAction.JoinRoom -> joinRoom()
            GameAction.LeaveRoom -> leaveRoom()
        }
    }

    /* ================= GAME OBSERVER ================= */

    private fun observeGame() {
        viewModelScope.launch {
            combine(
                repository.gameState,
                repository.error
            ) { game, error ->
                _uiState.value.copy(
                    isLoading = game == null,
                    game = game,
                    errorMessage = error,
                    isMyTurn = game?.currentPlayerToPlayIndex == 0
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    /* ================= GAME FLOW ================= */

    private fun startGame(t1: String, t2: String) {
        repository.startGame(t1, t2)
    }

    private fun restartGame() {
        repository.restartGame()
    }

    private fun clearError() {
        repository.clearError()
    }

    /* ================= MULTIPLAYER PLACEHOLDER ================= */

    private fun createRoom() {
        _uiState.value = _uiState.value.copy(
            multiplayerState = _uiState.value.multiplayerState.copy(
                enabled = true,
                isHost = true,
                connectionStatus = ConnectionStatus.CREATING_ROOM
            )
        )
    }

    private fun joinRoom() {}
    private fun leaveRoom() {}
}
