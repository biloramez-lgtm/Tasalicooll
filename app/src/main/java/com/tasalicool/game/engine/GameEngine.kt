package com.tasalicool.game.engine

import com.tasalicool.game.model.*
import com.tasalicool.game.rules.*
import com.tasalicool.game.engine.ai.AiEngine
import com.tasalicool.game.network.NetworkCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameEngine {

    private val aiEngine = AiEngine()
    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var isNetworkClientMode = false
    fun enableNetworkClientMode() { isNetworkClientMode = true }

    // =================== GAME LOGIC ===================

    fun initializeGame(team1: Team, team2: Team, dealerIndex: Int = 0) {
        val players = team1.players + team2.players
        val game = Game(team1 = team1, team2 = team2, players = players)
        game.startDealing()
        setDealer(game, dealerIndex)
        dealCards(game)
        _gameState.value = game

        // 🔥 ربط كل لاعب بالـ Engine
        game.players.forEachIndexed { index, player ->
            player.onPlayCard = { card ->
                this.playCard(index, card)
            }
        }

        playAiTurnIfNeeded()
    }

    fun initializeDefaultGame(team1Name: String, team2Name: String) {
        val p1 = Player(0, "$team1Name-1")
        val p2 = Player(1, "$team2Name-1", isAI = true)
        val p3 = Player(2, "$team1Name-2")
        val p4 = Player(3, "$team2Name-2", isAI = true)

        initializeGame(
            Team(1, team1Name, listOf(p1, p3)),
            Team(2, team2Name, listOf(p2, p4))
        )
    }

    private fun dealCards(game: Game) {
        val deck = Card.createGameDeck().shuffled()
        game.players.forEach { it.hand.clear() }
        deck.chunked(13).forEachIndexed { index, cards ->
            game.players[index].hand.addAll(cards)
            game.players[index].sortHand()
        }
        game.startBidding()
    }

    fun placeBid(playerIndex: Int, bid: Int): Boolean {
        if (isNetworkClientMode) return false
        val game = _gameState.value ?: return false
        if (playerIndex != game.currentPlayerIndex) {
            emitError("مش دورك بالبيد")
            return false
        }
        val player = game.players[playerIndex]
        val minBid = BiddingRules.getMinimumBid(game.getTeamByPlayer(player.id)?.score ?: 0)
        if (!BiddingRules.isValidBid(bid, player.hand.size, minBid)) {
            emitError("Bid غير صالح")
            return false
        }
        player.bid = bid
        game.advanceBidding()
        _gameState.value = game
        playAiTurnIfNeeded()
        return true
    }

    fun playCard(playerIndex: Int, card: Card): Boolean {
        if (isNetworkClientMode) return false
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
        if (!PlayRules.canPlayCard(card, player, trick)) {
            emitError("كرت غير مسموح")
            return false
        }

        player.removeCard(card)
        trick.playCard(player, card)

        if (trick.isComplete(game.players.size)) {
            val winnerIndex = TrickRules.calculateWinner(trick)
            game.endTrick(winnerIndex)
            if (game.tricks.size == 13) endRound(game)
        } else {
            game.advanceTurn()
        }

        _gameState.value = game
        playAiTurnIfNeeded()
        return true
    }

    private fun playAiTurnIfNeeded() {
        if (isNetworkClientMode) return
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
        val minBid = BiddingRules.getMinimumBid(game.getTeamByPlayer(player.id)?.score ?: 0)
        val bid = aiEngine.decideBid(
            hand = player.hand,
            teamScore = game.getTeamByPlayer(player.id)?.score ?: 0,
            opponentScore = game.getOpponentTeam(player.id)?.score ?: 0,
            minimumBid = minBid
        )
        placeBid(game.currentPlayerIndex, bid)
    }

    private fun handleAiPlay(game: Game, player: Player) {
        val trick = game.getOrCreateCurrentTrick()
        val validCards = PlayRules.getValidCards(player, trick)
        val card = aiEngine.decideCard(
            hand = player.hand,
            validCards = validCards,
            trickSuit = trick.trickSuit,
            playedCards = trick.cards,
            trickNumber = game.tricks.size,
            teamScore = game.getTeamByPlayer(player.id)?.score ?: 0,
            opponentScore = game.getOpponentTeam(player.id)?.score ?: 0,
            tricksBidded = game.getTeamByPlayer(player.id)?.totalBid ?: 0,
            tricksWon = game.getTeamByPlayer(player.id)?.totalTricksWon ?: 0,
            currentTrickWinnerId = trick.winnerId ?: -1,
            playerId = player.id
        )
        playCard(game.currentPlayerIndex, card)
    }

    private fun endRound(game: Game) {
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

    private fun setDealer(game: Game, dealerIndex: Int) {
        repeat(dealerIndex) { game.startNextRound() }
    }

    private fun emitError(message: String) { _error.value = message }
    fun clearError() { _error.value = null }

    // ================= NETWORK SUPPORT =================

    fun onNetworkCommand(command: NetworkCommand) {
        when (command) {
            is NetworkCommand.BidPlaced -> placeBid(findPlayerIndexById(command.playerId), command.bidValue)
            is NetworkCommand.CardPlayed -> {
                val playerIndex = findPlayerIndexById(command.playerId)
                val card = _gameState.value?.players?.get(playerIndex)?.hand?.firstOrNull {
                    it.rank.name == command.cardRank && it.suit.name == command.cardSuit
                }
                if (card != null) playCard(playerIndex, card)
            }
            else -> {}
        }
    }

    private fun findPlayerIndexById(playerId: String): Int {
        return _gameState.value?.players?.indexOfFirst { it.id.toString() == playerId } ?: -1
    }
}
