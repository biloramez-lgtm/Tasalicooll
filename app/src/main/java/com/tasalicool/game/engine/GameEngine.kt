package com.tasalicool.game.engine

import com.tasalicool.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameEngine {

    // ================= SUB ENGINES =================

    private val cardRules = CardRulesEngine()
    private val scoringEngine = ScoringEngine()
    private val biddingEngine = BiddingEngine()

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
        val currentTrick = game.tricks.lastOrNull()
            ?: Trick(game.currentTrickNumber).also { game.tricks.add(it) }

        if (!cardRules.canPlayCard(card, player, currentTrick)) {
            emitError("كرت غير مسموح")
            return false
        }

        player.removeCard(card)
        currentTrick.play(playerIndex, card)

        if (currentTrick.isComplete(game.players.size)) {
            val winnerIndex = cardRules.calculateTrickWinner(currentTrick)
            game.endTrick(winnerIndex)

            if (game.tricks.size == 13) {
                endRound(game)
            }
        } else {
            game.advanceTurn()
        }

        _gameState.value = game
        return true
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

    // ================= HELPERS =================

    private fun setDealer(game: Game, dealerIndex: Int) {
        repeat(dealerIndex) {
            game.startNextRound()
        }
    }

    private fun emitError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }
}
