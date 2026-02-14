package com.tasalicool.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasalicool.game.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    /* ================= STATE ================= */

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<GameEffect>()
    val effect: SharedFlow<GameEffect> = _effect.asSharedFlow()

    init {
        observeGame()
    }

    /* ================= ACTION HANDLER ================= */

    fun onAction(action: GameAction) {
        when (action) {

            is GameAction.StartGame -> {
                startGame(action.team1Name, action.team2Name)
            }

            GameAction.RestartGame -> restartGame()

            is GameAction.PlaceBid -> {
                repository.placeBid(action.playerIndex, action.bid)
            }

            is GameAction.PlayCard -> {
                repository.playCard(action.playerIndex, action.card)
            }

            GameAction.ClearError -> clearError()

            /* -------- Multiplayer -------- */

            GameAction.CreateRoom -> createRoom()
            is GameAction.JoinRoom -> joinRoom(action.roomCode)
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

                    isMyTurn = game?.let {
                        it.currentPlayerToPlayIndex == 0 // لاحقًا: playerId
                    } ?: false,

                    canBid = game?.gamePhase?.name == "BIDDING",
                    canPlayCard = game?.gamePhase?.name == "PLAYING"
                )

            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    /* ================= GAME FLOW ================= */

    private fun startGame(team1: String, team2: String) {
        repository.startGame(team1, team2)
    }

    private fun restartGame() {
        repository.restartGame()
    }

    private fun clearError() {
        repository.clearError()
    }

    /* ================= MULTIPLAYER (READY) ================= */

    private fun createRoom() {
        _uiState.update {
            it.copy(
                multiplayerState = it.multiplayerState.copy(
                    enabled = true,
                    isHost = true,
                    status = ConnectionStatus.CREATING_ROOM
                )
            )
        }
    }

    private fun joinRoom(roomCode: String) {
        _uiState.update {
            it.copy(
                multiplayerState = it.multiplayerState.copy(
                    enabled = true,
                    isHost = false,
                    roomCode = roomCode,
                    status = ConnectionStatus.CONNECTED
                )
            )
        }
    }

    private fun leaveRoom() {
        _uiState.update {
            it.copy(multiplayerState = MultiplayerState())
        }
    }
}
