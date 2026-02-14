package com.tasalicool.game.engine

import com.tasalicool.game.model.*
import com.tasalicool.game.rules.*

class GameEngine {

    // ==================== GAME CREATION ====================

    fun createGame(team1: Team, team2: Team, dealerIndex: Int = 0): Game {
        val players = listOf(
            team1.player1,
            team1.player2,
            team2.player1,
            team2.player2
        )

        return Game(
            team1 = team1,
            team2 = team2,
            players = players,
            dealerIndex = dealerIndex,
            currentPlayerIndex = rightOfDealer(dealerIndex),
            gamePhase = GamePhase.DEALING,
            biddingPhase = BiddingPhase.WAITING
        )
    }

    private fun rightOfDealer(dealerIndex: Int): Int =
        (dealerIndex + 1) % 4

    // ==================== ROUND FLOW ====================

    fun startRound(game: Game): Boolean {
        if (game.isGameOver) return false

        game.currentTrick = 1
        game.biddingPhase = BiddingPhase.WAITING
        game.gamePhase = GamePhase.DEALING
        game.currentPlayerIndex = rightOfDealer(game.dealerIndex)

        game.team1.resetRound()
        game.team2.resetRound()
        game.tricks.clear()

        return true
    }

    // ==================== TURN MANAGEMENT ====================

    private fun ensureTurn(game: Game, playerId: Int): Int? {
        val index = game.players.indexOfFirst { it.id == playerId }
        if (index == -1) return null
        return if (index == game.currentPlayerIndex) index else null
    }

    private fun advanceTurn(game: Game) {
        game.currentPlayerIndex = (game.currentPlayerIndex + 1) % 4
    }

    // ==================== PHASE TRANSITIONS ====================

    fun transitionToBidding(game: Game): Boolean {
        if (!GameRules.canTransition(game.gamePhase, GamePhase.BIDDING)) return false

        game.gamePhase = GamePhase.BIDDING
        game.biddingPhase = BiddingPhase.PLAYER1_BIDDING
        game.currentPlayerIndex = rightOfDealer(game.dealerIndex)

        return true
    }

    private fun transitionToPlaying(game: Game): Boolean {
        val totalBids = game.players.sumOf { it.bid }
        val maxScore = maxOf(game.team1.score, game.team2.score)

        if (!BiddingRules.isTotalBidsValid(totalBids, maxScore)) {
            return startRound(game)
        }

        game.gamePhase = GamePhase.PLAYING
        game.currentTrick = 1
        game.currentPlayerIndex = rightOfDealer(game.dealerIndex)

        return true
    }

    private fun transitionToRoundEnd(game: Game): Boolean {
        game.gamePhase = GamePhase.ROUND_END
        calculateRoundScores(game)
        return transitionToNextRoundOrEnd(game)
    }

    private fun transitionToNextRoundOrEnd(game: Game): Boolean {
        if (game.team1.isWinner) {
            endGame(game, 1)
            return true
        }
        if (game.team2.isWinner) {
            endGame(game, 2)
            return true
        }

        game.dealerIndex = (game.dealerIndex + 1) % 4
        game.currentRound++
        return startRound(game)
    }

    private fun endGame(game: Game, winnerTeamId: Int) {
        game.gamePhase = GamePhase.GAME_END
        game.isGameOver = true
        game.winningTeamId = winnerTeamId
    }

    // ==================== BIDDING ====================

    fun placeBid(game: Game, playerId: Int, bid: Int): Boolean {
        if (game.gamePhase != GamePhase.BIDDING) return false

        val playerIndex = ensureTurn(game, playerId) ?: return false
        val player = game.players[playerIndex]

        val minimumBid = BiddingRules.getMinimumBid(
            maxOf(game.team1.score, game.team2.score)
        )

        if (!BiddingRules.isValidBid(bid, player.hand.size, minimumBid)) return false

        player.bid = bid
        advanceTurn(game)

        game.biddingPhase = game.biddingPhase.next()

        if (game.biddingPhase == BiddingPhase.COMPLETE) {
            return transitionToPlaying(game)
        }

        return true
    }

    // ==================== PLAYING ====================

    fun playCard(game: Game, playerId: Int, card: Card): Boolean {
        if (game.gamePhase != GamePhase.PLAYING) return false

        val playerIndex = ensureTurn(game, playerId) ?: return false
        val player = game.players[playerIndex]

        val trick = game.getOrCreateCurrentTrick()

        if (!PlayRules.canPlayCard(card, player, trick, trick.cards.values.toList()))
            return false

        if (!player.removeCard(card)) return false

        trick.addCard(player.id, card)

        if (trick.isComplete(4)) {
            return completeTrick(game, trick)
        }

        advanceTurn(game)
        return true
    }

    private fun completeTrick(game: Game, trick: Trick): Boolean {
        val winnerId = TrickRules.calculateWinner(trick, TrickRules.getTrumpSuit())
        if (winnerId == -1) return false

        trick.winnerId = winnerId
        val winnerIndex = game.players.indexOfFirst { it.id == winnerId }

        val team = game.getTeamByPlayer(winnerId)
        team?.addTrickToPlayer(winnerId)

        if (game.currentTrick == GameRules.TOTAL_TRICKS) {
            return transitionToRoundEnd(game)
        }

        game.currentTrick++
        game.currentPlayerIndex = winnerIndex
        return true
    }

    // ==================== SCORE ====================

    private fun calculateRoundScores(game: Game) {
        val isAfter30 = maxOf(game.team1.score, game.team2.score) >= 30

        game.team1.applyRoundScore(isAfter30)
        game.team2.applyRoundScore(isAfter30)
    }
}
