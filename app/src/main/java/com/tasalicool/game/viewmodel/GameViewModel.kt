package com.tasalicool.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasalicool.game.model.GamePhase
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

                GameUiState(
                    isLoading = game == null,
                    game = game,
                    errorMessage = error,

                    /* -------- UI Helpers -------- */

                    isMyTurn = game?.currentPlayerToPlayIndex == 0, // لاحقًا playerId
                    canBid = game?.gamePhase == GamePhase.BIDDING,
                    canPlayCard = game?.gamePhase == GamePhase.PLAYING,

                    multiplayerState = _uiState.value.multiplayerState
                )

            }.collect { newState ->

                _uiState.value = newState

                /* -------- Effects -------- */

                if (newState.game?.isGameOver == true) {
                    _effect.emit(GameEffect.NavigateToGameOver)
                }

                if (newState.errorMessage != null) {
                    _effect.emit(
                        GameEffect.ShowSnackbar(
                            message = newState.errorMessage,
                            isError = true
                        )
                    )
                }
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

        viewModelScope.launch {
            _effect.emit(GameEffect.ShowSnackbar("Room created"))
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

        viewModelScope.launch {
            _effect.emit(GameEffect.PlayerConnected)
        }
    }

    private fun leaveRoom() {
        _uiState.update {
            it.copy(multiplayerState = MultiplayerState())
        }

        viewModelScope.launch {
            _effect.emit(GameEffect.PlayerDisconnected)
        }
    }
}
