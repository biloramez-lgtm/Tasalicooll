package com.tarneeb.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarneeb.game.engine.*
import com.tarneeb.game.model.*
import com.tarneeb.game.utils.GameConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val cardRulesEngine = CardRulesEngine()
    private val scoringEngine = ScoringEngine()
    private val biddingEngine = BiddingEngine()
    private val gameEngine = GameEngine(cardRulesEngine, scoringEngine, biddingEngine)
    private val aiPlayer = AIPlayer(AIPlayer.Difficulty.MEDIUM)

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()

    private val _currentPhase = MutableStateFlow<GamePhase>(GamePhase.DEALING)
    val currentPhase: StateFlow<GamePhase> = _currentPhase.asStateFlow()

    private val _validBids = MutableStateFlow<List<Int>>(emptyList())
    val validBids: StateFlow<List<Int>> = _validBids.asStateFlow()

    private val _validCards = MutableStateFlow<List<Card>>(emptyList())
    val validCards: StateFlow<List<Card>> = _validCards.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun initializeGame(player1Name: String, player2Name: String, ai1Name: String, ai2Name: String) {
        viewModelScope.launch {
            val player1 = Player(
                id = 0,
                name = player1Name,
                isAI = false,
                position = GameConstants.SOUTH
            )
            val player2 = Player(
                id = 1,
                name = ai1Name,
                isAI = true,
                position = GameConstants.WEST
            )
            val player3 = Player(
                id = 2,
                name = player2Name,
                isAI = false,
                position = GameConstants.NORTH
            )
            val player4 = Player(
                id = 3,
                name = ai2Name,
                isAI = true,
                position = GameConstants.EAST
            )

            val team1 = Team(
                id = "1",
                name = "Team 1",
                player1 = player1,
                player2 = player3
            )
            val team2 = Team(
                id = "2",
                name = "Team 2",
                player1 = player2,
                player2 = player4
            )

            val game = gameEngine.initializeGame(team1, team2, 0)
            _gameState.emit(game)
            startNewRound()
        }
    }

    fun startNewRound() {
        viewModelScope.launch {
            val game = _gameState.value ?: return@launch
            gameEngine.dealCards(game)
            _gameState.emit(game)
            _currentPhase.emit(GamePhase.BIDDING)
            updateValidBids()
        }
    }

    fun placeBid(playerIndex: Int, bid: Int) {
        viewModelScope.launch {
            val game = _gameState.value ?: return@launch

            if (bid < 2 || bid > 13) {
                _errorMessage.emit("Invalid bid. Must be between 2 and 13.")
                return@launch
            }

            try {
                gameEngine.placeBid(game, playerIndex, bid)
                _gameState.emit(game)

                // If current phase is bidding and not complete, trigger AI if next player is AI
                if (game.gamePhase == GamePhase.BIDDING && game.biddingPhase != BiddingPhase.COMPLETE) {
                    val nextPlayerIndex = when (game.biddingPhase) {
                        BiddingPhase.PLAYER2_BIDDING -> 1
                        BiddingPhase.PLAYER3_BIDDING -> 2
                        BiddingPhase.PLAYER4_BIDDING -> 3
                        else -> -1
                    }

                    if (nextPlayerIndex >= 0) {
                        val nextPlayer = game.players[nextPlayerIndex]
                        if (nextPlayer.isAI) {
                            triggerAIBid(nextPlayerIndex)
                        }
                    }
                } else if (game.gamePhase == GamePhase.PLAYING) {
                    _currentPhase.emit(GamePhase.PLAYING)
                    updateValidCards()
                    triggerAIIfNeeded()
                }

                updateValidBids()
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Error placing bid")
            }
        }
    }

    fun playCard(playerIndex: Int, card: Card) {
        viewModelScope.launch {
            val game = _gameState.value ?: return@launch

            try {
                gameEngine.playCard(game, playerIndex, card)
                _gameState.emit(game)

                if (game.gamePhase == GamePhase.PLAYING) {
                    updateValidCards()
                    triggerAIIfNeeded()
                } else if (game.gamePhase == GamePhase.GAME_END) {
                    _currentPhase.emit(GamePhase.GAME_END)
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Error playing card")
            }
        }
    }

    private fun triggerAIBid(playerIndex: Int) {
        viewModelScope.launch {
            val game = _gameState.value ?: return@launch
            val player = game.players[playerIndex]
            val bid = aiPlayer.selectBid(player, game, biddingEngine, scoringEngine)
            placeBid(playerIndex, bid)
        }
    }

    private fun triggerAIIfNeeded() {
        viewModelScope.launch {
            val game = _gameState.value ?: return@launch
            if (game.gamePhase == GamePhase.PLAYING) {
                val currentPlayer = game.getCurrentPlayer()
                if (currentPlayer.isAI && currentPlayer.hand.isNotEmpty()) {
                    val card = aiPlayer.selectCard(currentPlayer, game, cardRulesEngine)
                    playCard(currentPlayer.id, card)
                }
            }
        }
    }

    private fun updateValidBids() {
        val game = _gameState.value ?: return
        val currentPlayerIndex = when (game.biddingPhase) {
            BiddingPhase.PLAYER1_BIDDING -> 0
            BiddingPhase.PLAYER2_BIDDING -> 1
            BiddingPhase.PLAYER3_BIDDING -> 2
            BiddingPhase.PLAYER4_BIDDING -> 3
            else -> -1
        }

        if (currentPlayerIndex >= 0) {
            val player = game.players[currentPlayerIndex]
            val minimumBid = scoringEngine.getMinimumBid(
                maxOf(game.team1.score, game.team2.score)
            )
            val validBids = biddingEngine.getValidBids(player, minimumBid)
            viewModelScope.launch {
                _validBids.emit(validBids)
            }
        }
    }

    private fun updateValidCards() {
        val game = _gameState.value ?: return
        val currentPlayer = game.getCurrentPlayer()
        val currentTrick = game.tricks.lastOrNull() ?: Trick()
        val validCards = cardRulesEngine.getValidPlayableCards(currentPlayer, currentTrick)
        viewModelScope.launch {
            _validCards.emit(validCards)
        }
    }

    fun clearError() {
        viewModelScope.launch {
            _errorMessage.emit(null)
        }
    }
}
