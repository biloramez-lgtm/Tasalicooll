package com.tasalicool.game.engine

import com.tasalicool.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameEngine {

    private val cardRules = CardRulesEngine()
    private val scoring = ScoringEngine()
    private val bidding = BiddingEngine()

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /* ======================= INIT ======================= */

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
            players = players,
            dealerIndex = dealerIndex,
            currentPlayerToPlayIndex = (dealerIndex + 1) % 4
        )

        dealCards(game)
        _gameState.value = game
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

    fun restartGame() {
        _gameState.value?.let {
            it.resetForNewRound()
            dealCards(it)
            _gameState.value = it
        }
    }

    /* ======================= DEAL ======================= */

    private fun dealCards(game: Game) {
        val deck = Card.createGameDeck().shuffled()

        game.players.forEach { it.hand.clear() }

        deck.chunked(13).forEachIndexed { i, cards ->
            game.players[i].hand.addAll(cards)
            game.players[i].sortHand()
        }

        game.gamePhase = GamePhase.BIDDING
        game.biddingPhase = BiddingPhase.PLAYER1_BIDDING
    }

    /* ======================= BIDDING ======================= */

    fun placeBid(playerIndex: Int, bid: Int): Boolean {
        val game = _gameState.value ?: return false
        val player = game.players[playerIndex]

        val minBid = scoring.getMinimumBid(
            maxOf(game.team1.score, game.team2.score)
        )

        if (!cardRules.validateBid(bid, player.hand.size, minBid)) {
            _error.value = "Bid غير صالح"
            return false
        }

        player.bid = bid
        game.advanceBidding()

        if (game.biddingPhase == BiddingPhase.COMPLETE) {
            val total = game.players.sumOf { it.bid }
            if (total < game.getMinimumTotalBids()) {
                restartGame()
            } else {
                game.gamePhase = GamePhase.PLAYING
                game.currentPlayerToPlayIndex = game.getRightOfDealerIndex()
            }
        }

        _gameState.value = game
        return true
    }

    /* ======================= PLAY ======================= */

    fun playCard(playerIndex: Int, card: Card): Boolean {
        val game = _gameState.value ?: return false
        val player = game.players[playerIndex]

        if (playerIndex != game.currentPlayerToPlayIndex) {
            _error.value = "مش دورك"
            return false
        }

        val trick = game.currentTrick()

        if (!cardRules.canPlayCard(card, player, trick)) {
            _error.value = "كرت غير مسموح"
            return false
        }

        player.removeCard(card)
        trick.addCard(playerIndex, card)

        if (trick.isComplete(4)) {
            val winner = cardRules.calculateTrickWinner(trick)
            game.getPlayerById(winner)?.tricksWon++
            game.currentPlayerToPlayIndex = winner

            if (game.tricks.size == 13) {
                endRound(game)
            }
        } else {
            game.currentPlayerToPlayIndex = (playerIndex + 1) % 4
        }

        _gameState.value = game
        return true
    }

    /* ======================= ROUND END ======================= */

    private fun endRound(game: Game) {
        game.applyScores(scoring)

        when {
            game.team1.isWinner -> game.finish(1)
            game.team2.isWinner -> game.finish(2)
            else -> restartGame()
        }

        _gameState.value = game
    }

    /* ======================= HELPERS ======================= */

    fun clearError() {
        _error.value = null
    }
}
