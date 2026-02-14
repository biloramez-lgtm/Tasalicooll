package com.tasalicool.game.engine

import com.tasalicool.game.engine.ai.AiEngine
import com.tasalicool.game.model.*
import com.tasalicool.game.network.NetworkCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameEngine {

    // ================= SUB ENGINES =================

    private val cardRules = CardRulesEngine()
    private val scoringEngine = ScoringEngine()
    private val biddingEngine = BiddingEngine()
    private val aiEngine = AiEngine()

    // ================= STATE =================

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ================= INIT =================

    fun initializeGame(
        team1: Team,
        team2: Team,
        dealerIndex: Int = 0
    ) {
        val players = listOf(
            team1.player1,
            team2.player1,
            team1.player2,
            team2.player2
        )

        val game = Game(
            team1 = team1,
            team2 = team2,
            players = players
        )

        game.startDealing()
        setDealer(game, dealerIndex)
        dealCards(game)

        _gameState.value = game
        playAiTurnIfNeeded()
    }

    fun initializeDefaultGame(team1Name: String, team2Name: String) {
        val p1 = Player(0, "$team1Name-1")
        val p2 = Player(1, "$team2Name-1", isAI = true)
        val p3 = Player(2, "$team1Name-2")
        val p4 = Player(3, "$team2Name-2", isAI = true)

        initializeGame(
            Team(1, team1Name, p1, p3),
            Team(2, team2Name, p2, p4)
        )
    }

    // ================= DEAL =================

    private fun dealCards(game: Game) {
        val deck = Card.createGameDeck().shuffled()

        game.players.forEach { it.hand.clear() }

        deck.chunked(13).forEachIndexed { index, cards ->
            game.players[index].hand.addAll(cards)
            game.players[index].sortHand()
        }

        game.startBidding()
    }

    // ================= BIDDING =================

    fun placeBid(playerIndex: Int, bid: Int): Boolean {
        val game = _gameState.value ?: return false

        if (playerIndex != game.currentPlayerIndex) {
            emitError("مش دورك بالبيد")
            return false
        }

        val player = game.players[playerIndex]

        if (!biddingEngine.isValidBid(bid, player, game)) {
            emitError("Bid غير صالح")
            return false
        }

        player.bid = bid
        game.advanceBidding()

        if (biddingEngine.isBiddingComplete(game)) {
            game.startPlaying()
        }

        _gameState.value = game
        playAiTurnIfNeeded()
        return true
    }

    // ================= PLAY =================

    fun playCard(playerIndex: Int, card: Card): Boolean {
        val game = _gameState.value ?: return false

        if (game.gamePhase != GamePhase.PLAYING) {
            emitError("اللعب غير مسموح الآن")
            return false
        }

        if (playerIndex != game.currentPlayerIndex) {
            emitError("مش دورك")
            return false
        }

        val player = game.players[playerIndex]
        val trick = game.getOrCreateCurrentTrick()

        if (!cardRules.canPlayCard(card, player, trick)) {
            emitError("كرت غير مسموح")
            return false
        }

        player.removeCard(card)
        trick.play(playerIndex, card)

        if (trick.isComplete(game.players.size)) {
            val winner = cardRules.calculateTrickWinner(trick)
            game.endTrick(winner)

            if (game.tricks.size == 13) {
                endRound(game)
            }
        } else {
            game.advanceTurn()
        }

        _gameState.value = game
        playAiTurnIfNeeded()
        return true
    }

    // ================= AI =================

    private fun playAiTurnIfNeeded() {
        val game = _gameState.value ?: return
        val player = game.players[game.currentPlayerIndex]

        if (!player.isAI || game.isGameOver) return

        when (game.gamePhase) {
            GamePhase.BIDDING -> handleAiBid(game, player)
            GamePhase.PLAYING -> handleAiPlay(game, player)
            else -> Unit
        }
    }

    private fun handleAiBid(game: Game, player: Player) {
        val minBid = biddingEngine.getMinimumBid(game)

        val bid = aiEngine.decideBid(
            hand = player.hand,
            teamScore = game.getTeamByPlayer(player.id)!!.score,
            opponentScore = game.getOpponentTeam(player.id)!!.score,
            minimumBid = minBid
        )

        placeBid(game.currentPlayerIndex, bid)
    }

    private fun handleAiPlay(game: Game, player: Player) {
        val trick = game.getOrCreateCurrentTrick()
        val validCards = cardRules.getValidPlayableCards(player, trick)

        val card = aiEngine.decideCard(
            hand = player.hand,
            validCards = validCards,
            trickSuit = trick.trickSuit,
            playedCards = trick.cards,
            trickNumber = game.currentTrick,
            teamScore = game.getTeamByPlayer(player.id)!!.score,
            opponentScore = game.getOpponentTeam(player.id)!!.score,
            tricksBidded = game.getTeamByPlayer(player.id)!!.getTotalBid(),
            tricksWon = game.getTeamByPlayer(player.id)!!.getTotalTricksWon(),
            currentTrickWinnerId = trick.currentWinnerId,
            playerId = player.id
        )

        playCard(game.currentPlayerIndex, card)
    }

    // ================= ROUND END =================

    private fun endRound(game: Game) {
        scoringEngine.applyScores(game)

        when {
            game.team1.isWinner -> game.endGame(game.team1.id)
            game.team2.isWinner -> game.endGame(game.team2.id)
            else -> {
                game.endRound()
                game.startNextRound()
                dealCards(game)
            }
        }
    }

    // ================= NETWORK SUPPORT (CLIENT MODE CLEAN) =================

    fun onNetworkCommand(command: NetworkCommand) {
        val game = _gameState.value ?: return

        when (command) {

            is NetworkCommand.GameStarted -> {
                _gameState.value = game
            }

            is NetworkCommand.TurnChanged -> {
                val newIndex = game.players.indexOfFirst {
                    it.id == command.currentPlayerId
                }
                if (newIndex != -1) {
                    game.currentPlayerIndex = newIndex
                    _gameState.value = game
                }
            }

            is NetworkCommand.BidPlaced -> {
                val playerIndex = game.players.indexOfFirst {
                    it.id == command.playerId
                }
                if (playerIndex != -1) {
                    game.players[playerIndex].bid = command.bidValue
                    _gameState.value = game
                }
            }

            is NetworkCommand.CardPlayed -> {
                val playerIndex = game.players.indexOfFirst {
                    it.id == command.playerId
                }
                if (playerIndex == -1) return

                val player = game.players[playerIndex]

                val card = Card(
                    suit = Suit.valueOf(command.cardSuit),
                    rank = Rank.valueOf(command.cardRank)
                )

                val trick = game.getOrCreateCurrentTrick()

                player.removeCard(card)
                trick.play(playerIndex, card)

                _gameState.value = game
            }

            is NetworkCommand.TrickCompleted -> {
                val winnerIndex = game.players.indexOfFirst {
                    it.id == command.winnerPlayerId
                }
                if (winnerIndex != -1) {
                    game.endTrick(winnerIndex)
                    _gameState.value = game
                }
            }

            is NetworkCommand.RoundCompleted -> {
                game.team1.score = command.team1Score
                game.team2.score = command.team2Score
                game.endRound()
                _gameState.value = game
            }

            is NetworkCommand.GameEnded -> {
                game.endGame(command.winningTeamId)
                _gameState.value = game
            }

            is NetworkCommand.SyncState -> {
                // optional future full state sync
            }

            else -> Unit
        }
    }

    // ================= HELPERS =================

    private fun setDealer(game: Game, dealerIndex: Int) {
        repeat(dealerIndex) { game.startNextRound() }
    }

    private fun emitError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }
}
