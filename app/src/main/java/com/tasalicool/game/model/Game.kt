package com.tasalicool.game.model

import java.util.UUID

/**
 * Game - Orchestrator (STATE ONLY)
 *
 * Responsible ONLY for:
 * - Holding game state
 * - Tracking turns & phases
 *
 * NO rules
 * NO calculations
 * NO decisions
 */
data class Game(
    val id: String = UUID.randomUUID().toString(),

    val team1: Team,
    val team2: Team,
    val players: List<Player>
) {

    // ================= STATE =================

    var gamePhase: GamePhase = GamePhase.DEALING
        private set

    var biddingPhase: BiddingPhase = BiddingPhase.WAITING
        private set

    var currentRound: Int = 1
        private set

    var currentTrick: Int = 1
        private set

    var dealerIndex: Int = 0
        private set

    var currentPlayerIndex: Int = 0
        private set

    var isGameOver: Boolean = false
        private set

    var winningTeamId: Int? = null
        private set

    // ================= GETTERS =================

    fun currentPlayer(): Player =
        players[currentPlayerIndex]

    fun dealerPlayer(): Player =
        players[dealerIndex]

    // 👈 أهم دالة
    fun rightOfDealerIndex(): Int =
        (dealerIndex + 1) % players.size

    fun nextPlayerIndex(): Int =
        (currentPlayerIndex + 1) % players.size

    // ================= PHASE TRANSITIONS =================

    fun startDealing() {
        gamePhase = GamePhase.DEALING
        biddingPhase = BiddingPhase.WAITING
        currentTrick = 1
        currentPlayerIndex = rightOfDealerIndex()
    }

    fun startBidding() {
        gamePhase = GamePhase.BIDDING
        biddingPhase = BiddingPhase.ACTIVE
        currentPlayerIndex = rightOfDealerIndex()
    }

    fun startPlaying() {
        gamePhase = GamePhase.PLAYING
        currentTrick = 1
        currentPlayerIndex = rightOfDealerIndex()
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
        winningTeamId = winnerTeamId
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
