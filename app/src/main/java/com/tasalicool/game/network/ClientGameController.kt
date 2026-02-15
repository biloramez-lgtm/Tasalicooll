package com.tasalicool.game.network

import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClientGameController(
    private val gameEngine: GameEngine,
    private val multiplayerManager: MultiplayerManager
) {

    // ================= STATE =================

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _playerInfo = MutableStateFlow<NetworkPlayer?>(null)
    val playerInfo: StateFlow<NetworkPlayer?> = _playerInfo.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _gameEvents = MutableSharedFlow<GameEvent>()
    val gameEvents = _gameEvents.asSharedFlow()

    // ================= INITIALIZE =================

    fun initialize(scope: CoroutineScope) {
        scope.launch {
            multiplayerManager.commands.collect { command ->
                handleNetworkCommand(command)
            }
        }

        scope.launch {
            multiplayerManager.events.collect { event ->
                handleNetworkEvent(event)
            }
        }
    }

    // ================= CONNECTION =================

    suspend fun connect(
        hostAddress: String,
        playerName: String
    ): Boolean {
        return try {
            val result = multiplayerManager.connectToServer(hostAddress, playerName)
            _isConnected.value = result
            result
        } catch (e: Exception) {
            _errorMessage.value = e.message
            false
        }
    }

    fun disconnect() {
        multiplayerManager.disconnect()
        _isConnected.value = false
        _gameState.value = null
        _playerInfo.value = null
    }

    // ================= SEND ACTIONS =================

    fun placeBid(bid: Int) {
        val player = _playerInfo.value ?: return
        multiplayerManager.sendToServer(
            NetworkCommand.BidPlaced(playerId = player.id, bidValue = bid)
        )
    }

    fun playCard(card: Card) {
        val player = _playerInfo.value ?: return
        multiplayerManager.sendToServer(
            NetworkCommand.CardPlayed(
                playerId = player.id,
                cardRank = card.rank.name, // ممكن تغييره حسب توافق Rank مع MultiplayerController
                cardSuit = card.suit.name  // ممكن تغييره حسب توافق Suit مع MultiplayerController
            )
        )
    }

    fun sendChat(message: String) {
        val player = _playerInfo.value ?: return
        multiplayerManager.sendToServer(
            NetworkCommand.ChatMessage(player.id, message)
        )
    }

    // ================= RECEIVE =================

    private suspend fun handleNetworkCommand(command: NetworkCommand) {

        // معالجة المنطق عبر GameEngine
        gameEngine.receiveNetworkCommand(command)

        // تحديث UI
        when (command) {

            is NetworkCommand.GameStarted -> {
                val game = gameEngine.currentGame
                _gameState.value = game
                _gameEvents.emit(GameEvent.GameStarted(game))
            }

            is NetworkCommand.BidPlaced -> {
                _gameEvents.emit(
                    GameEvent.BidPlaced(command.playerId, command.bidValue)
                )
            }

            is NetworkCommand.CardPlayed -> {
                val card = Card(
                    suit = Suit.valueOf(command.cardSuit),
                    rank = Rank.valueOf(command.cardRank)
                )

                _gameEvents.emit(
                    GameEvent.CardPlayed(command.playerId, card)
                )
            }

            is NetworkCommand.TrickCompleted -> {
                _gameEvents.emit(
                    GameEvent.TrickWon(
                        winnerId = command.winnerPlayerId,
                        trickNumber = command.trickNumber
                    )
                )
            }

            is NetworkCommand.RoundCompleted -> {
                _gameEvents.emit(
                    GameEvent.RoundEnded(
                        roundNumber = command.roundNumber,
                        team1Score = command.team1Score,
                        team2Score = command.team2Score
                    )
                )
            }

            is NetworkCommand.GameEnded -> {
                _gameEvents.emit(
                    GameEvent.GameEnded(
                        winningTeamId = command.winningTeamId,
                        finalScore1 = command.finalScoreTeam1,
                        finalScore2 = command.finalScoreTeam2
                    )
                )
            }

            is NetworkCommand.ChatMessage -> {
                _gameEvents.emit(
                    GameEvent.ChatMessage(
                        playerId = command.playerId,
                        message = command.message
                    )
                )
            }

            else -> {}
        }
    }

    private suspend fun handleNetworkEvent(event: NetworkEvent) {
        when (event) {

            is NetworkEvent.PlayerConnected -> {
                _gameEvents.emit(
                    GameEvent.PlayerJoined(event.player)
                )
            }

            is NetworkEvent.PlayerDisconnected -> {
                _gameEvents.emit(
                    GameEvent.PlayerDisconnected(event.playerId)
                )
            }

            is NetworkEvent.ConnectionError -> {
                _errorMessage.value = event.message
            }

            else -> {}
        }
    }
}

/**
 * GameEvent - أحداث للـ UI فقط
 */
sealed class GameEvent {
    data class GameStarted(val game: Game?) : GameEvent()
    data class BidPlaced(val playerId: String, val bid: Int) : GameEvent()
    data class CardPlayed(val playerId: String, val card: Card) : GameEvent()
    data class TrickWon(val winnerId: String, val trickNumber: Int) : GameEvent()
    data class RoundEnded(val roundNumber: Int, val team1Score: Int, val team2Score: Int) : GameEvent()
    data class GameEnded(val winningTeamId: Int, val finalScore1: Int, val finalScore2: Int) : GameEvent()
    data class ChatMessage(val playerId: String, val message: String) : GameEvent()
    data class PlayerJoined(val player: NetworkPlayer) : GameEvent()
    data class PlayerDisconnected(val playerId: String) : GameEvent()
}
