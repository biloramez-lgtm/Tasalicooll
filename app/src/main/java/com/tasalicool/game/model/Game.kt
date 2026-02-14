package com.tasalicool.game.model

import java.util.UUID

/**
 * Game - Orchestrator
 *
 * Responsible ONLY for:
 * - Holding game state
 * - Tracking turns & phases
 * - Delegating logic to external engines
 *
 * NO rules, NO calculations, NO decisions
 */
data class Game(
    val id: String = UUID.randomUUID().toString(),

    val team1: Team,
    val team2: Team,
    val players: List<Player>,

    // ================= STATE =================

    var gamePhase: GamePhase = GamePhase.DEALING,
    var biddingPhase: BiddingPhase = BiddingPhase.WAITING,

    var currentRound: Int = 1,
    var currentTrick: Int = 1,

    var dealerIndex: Int = 0,
    var currentPlayerIndex: Int = 0,

    var isGameOver: Boolean = false,
    var winningTeamId: Int? = null
) {

    // ================= GETTERS =================

    fun currentPlayer(): Player =
        players[currentPlayerIndex]

    fun dealerPlayer(): Player =
        players[dealerIndex]

    fun rightOfDealer(): Player =
        players[(dealerIndex + 1) % players.size]

    fun nextPlayerIndex(): Int =
        (currentPlayerIndex + 1) % players.size

    // ================= PHASE TRANSITIONS =================

    fun startDealing() {
        gamePhase = GamePhase.DEALING
        biddingPhase = BiddingPhase.WAITING
        currentTrick = 1
        currentPlayerIndex = rightOfDealer().position
    }

    fun startBidding() {
        gamePhase = GamePhase.BIDDING
        biddingPhase = BiddingPhase.ACTIVE
        currentPlayerIndex = rightOfDealer().position
    }

    fun startPlaying() {
        gamePhase = GamePhase.PLAYING
        currentTrick = 1
        currentPlayerIndex = rightOfDealer().position
    }

    fun endTrick(nextLeaderIndex: Int) {
        currentPlayerIndex = nextLeaderIndex
        currentTrick++
    }

    fun endRound() {
        gamePhase = GamePhase.ROUND_END
    }

    fun startNextRound() {
        dealerIndex = (dealerIndex + 1) % players.size
        currentRound++
        startDealing()
    }

    fun endGame(winnerTeamId: Int) {
        this.winningTeamId = winnerTeamId
        gamePhase = GamePhase.GAME_END
        isGameOver = true
    }

    // ================= TURN CONTROL =================

    fun advanceTurn() {
        currentPlayerIndex = nextPlayerIndex()
    }

    // ================= STATUS =================

    fun statusText(): String =
        when (gamePhase) {
            GamePhase.DEALING   -> "Dealing - Round $currentRound"
            GamePhase.BIDDING   -> "Bidding"
            GamePhase.PLAYING   -> "Playing - Trick $currentTrick"
            GamePhase.ROUND_END -> "Round End"
            GamePhase.GAME_END  -> "Game Over"
        }
}
