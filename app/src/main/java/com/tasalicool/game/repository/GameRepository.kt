package com.tasalicool.game.repository

import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.engine.ai.AiEngine
import com.tasalicool.game.model.*
import com.tasalicool.game.network.ConnectionState
import com.tasalicool.game.network.NetworkPlayer
import com.tasalicool.game.network.impl.LocalMultiplayerManager
import com.tasalicool.game.rules.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GameRepository(
    private val engine: GameEngine = GameEngine(),
    private val aiEngine: AiEngine = AiEngine(),
    private val multiplayerManager: LocalMultiplayerManager = LocalMultiplayerManager(),
    private val gameHistoryManager: GameHistoryManager = GameHistoryManager()
) {

    // ==================== COROUTINE SCOPE ====================

    private val repositoryScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ==================== STATE FLOWS ====================

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()

    private val _connectionState =
        MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> =
        _connectionState.asStateFlow()

    private val _connectedPlayers =
        MutableStateFlow<List<NetworkPlayer>>(emptyList())
    val connectedPlayers: StateFlow<List<NetworkPlayer>> =
        _connectedPlayers.asStateFlow()

    private val _error = MutableStateFlow<GameError?>(null)
    val error: StateFlow<GameError?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _gameHistory =
        MutableStateFlow<List<GameRecord>>(emptyList())
    val gameHistory: StateFlow<List<GameRecord>> =
        _gameHistory.asStateFlow()

    private val _playerStatistics =
        MutableStateFlow<PlayerStatistics?>(null)
    val playerStatistics: StateFlow<PlayerStatistics?> =
        _playerStatistics.asStateFlow()

    val uiState: StateFlow<GameUIState> = combine(
        gameState,
        error,
        isLoading,
        connectionState
    ) { game, err, loading, conn ->
        GameUIState(
            game = game,
            error = err,
            isLoading = loading,
            connectionState = conn,
            isGameActive = game?.gamePhase != GamePhase.GAME_END && game != null,
            canPerformAction = !loading && game != null
        )
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = GameUIState()
    )

    // ==================== START GAME ====================

    fun startGame(
        team1Name: String = "Team 1",
        team2Name: String = "Team 2"
    ) {
        try {
            _isLoading.value = true
            _error.value = null

            val team1 = Team(
                id = 1,
                name = team1Name,
                player1 = Player(id = 0, name = "You"),
                player2 = Player(id = 2, name = "AI Partner", isAI = true)
            )

            val team2 = Team(
                id = 2,
                name = team2Name,
                player1 = Player(id = 1, name = "AI 1", isAI = true),
                player2 = Player(id = 3, name = "AI 2", isAI = true)
            )

            val game = engine.createGame(team1, team2)
            engine.startRound(game)

            _gameState.value = game
            gameHistoryManager.startNewGame(game)

        } catch (e: Exception) {
            _error.value =
                GameError.InitializationError(e.message ?: "Start error")
        } finally {
            _isLoading.value = false
        }
    }

    // ==================== ACTIONS ====================

    fun placeBid(playerIndex: Int, bid: Int): Boolean {
        val game = _gameState.value ?: return false
        _error.value = null

        val player = game.players.getOrNull(playerIndex) ?: return false
        val minBid = BiddingRules.getMinimumBid(
            maxOf(game.team1.score, game.team2.score)
        )

        if (!BiddingRules.isValidBid(bid, player.hand.size, minBid)) {
            _error.value = GameError.InvalidBid("Invalid bid")
            return false
        }

        val result = engine.placeBid(game, playerIndex, bid)
        if (result) {
            _gameState.value = game
            handleAITurn(game)
        }

        return result
    }

    fun playCard(playerIndex: Int, card: Card): Boolean {
        val game = _gameState.value ?: return false
        _error.value = null

        val validCards = getValidCards(playerIndex)
        if (!validCards.contains(card)) {
            _error.value = GameError.InvalidCard("Invalid card")
            return false
        }

        val result = engine.playCard(game, playerIndex, card)
        if (result) {
            _gameState.value = game
            handleAITurn(game)
        }

        return result
    }

    // ==================== AI ENGINE LOOP ====================

    private fun handleAITurn(game: Game) {

        repositoryScope.launch {

            var currentGame = game

            while (true) {

                val index = currentGame.currentPlayerToPlayIndex
                val player = currentGame.players[index]

                if (!player.isAI) break

                delay(600)

                when (currentGame.gamePhase) {

                    GamePhase.BIDDING -> {

                        val minBid = BiddingRules.getMinimumBid(
                            maxOf(
                                currentGame.team1.score,
                                currentGame.team2.score
                            )
                        )

                        val bid = aiEngine.decideBid(
                            hand = player.hand,
                            teamScore = currentGame
                                .getTeamByPlayer(index)?.score ?: 0,
                            opponentScore = 0,
                            minimumBid = minBid
                        )

                        engine.placeBid(currentGame, index, bid)
                    }

                    GamePhase.PLAYING -> {

                        val validCards = getValidCards(index)

                        if (validCards.isNotEmpty()) {
                            val chosenCard = aiEngine.decideCard(
                                hand = player.hand,
                                validCards = validCards,
                                trick = currentGame.getCurrentTrick()
                            )
                            engine.playCard(currentGame, index, chosenCard)
                        }
                    }

                    else -> break
                }

                _gameState.value = currentGame

                if (currentGame.gamePhase == GamePhase.GAME_END)
                    break
            }
        }
    }

    // ==================== HELPERS ====================

    fun getValidCards(playerIndex: Int): List<Card> {
        val game = _gameState.value ?: return emptyList()
        val player = game.players.getOrNull(playerIndex) ?: return emptyList()
        val trick = game.getCurrentTrick() ?: return player.hand.toList()
        return PlayRules.getValidCards(player, trick)
    }

    fun clearError() {
        _error.value = null
    }

    fun cleanup() {
        repositoryScope.cancel()
        _gameState.value = null
        _error.value = null
        _isLoading.value = false
        _connectedPlayers.value = emptyList()
    }
}
