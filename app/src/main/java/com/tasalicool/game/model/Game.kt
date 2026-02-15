package com.tasalicool.game.model

import java.util.UUID

/**
 * Game - Orchestrator (STATE ONLY)
 *
 * Holds ONLY game state.
 * No rules, no calculations, no decisions.
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

    var currentTrickNumber: Int = 1
        private set

    var dealerIndex: Int = 0
        private set

    var currentPlayerIndex: Int = 0
        private set

    var isGameOver: Boolean = false
        private set

    var winningTeamId: Int? = null
        private set

    val tricks: MutableList<Trick> = mutableListOf()

    // ================= GETTERS =================

    fun currentPlayer(): Player =
        players[currentPlayerIndex]

    fun dealerPlayer(): Player =
        players[dealerIndex]

    /** اللاعب الذي على يمين الموزّع */
    fun rightOfDealerIndex(): Int =
        (dealerIndex + 1) % players.size

    fun nextPlayerIndex(): Int =
        (currentPlayerIndex + 1) % players.size

    // ================= PHASE TRANSITIONS =================

    fun startDealing() {
        gamePhase = GamePhase.DEALING
        biddingPhase = BiddingPhase.WAITING
        currentTrickNumber = 1
        tricks.clear()
        currentPlayerIndex = rightOfDealerIndex()
    }

    fun startBidding() {
        gamePhase = GamePhase.BIDDING
        biddingPhase = BiddingPhase.ACTIVE
        currentPlayerIndex = rightOfDealerIndex()
    }

    fun startPlaying() {
        gamePhase = GamePhase.PLAYING
        currentTrickNumber = 1
        tricks.clear()
        currentPlayerIndex = rightOfDealerIndex()
    }

    fun advanceBidding() {
        currentPlayerIndex = nextPlayerIndex()
    }

    fun endTrick(nextLeaderIndex: Int) {
        currentPlayerIndex = nextLeaderIndex
        currentTrickNumber++
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
            GamePhase.PLAYING   -> "Playing - Trick $currentTrickNumber"
            GamePhase.ROUND_END -> "Round End"
            GamePhase.GAME_END  -> "Game Over"
        }

    // ================= TEAM HELPERS =================

    /**
     * الحصول على فريق اللاعب
     */
    fun getTeamByPlayer(playerId: Int): Team? {
        return when {
            team1.players.any { it.id == playerId } -> team1
            team2.players.any { it.id == playerId } -> team2
            else -> null
        }
    }

    /**
     * الحصول على فريق الخصم
     */
    fun getOpponentTeam(playerId: Int): Team? {
        val playerTeam = getTeamByPlayer(playerId)
        return when (playerTeam?.id) {
            team1.id -> team2
            team2.id -> team1
            else -> null
        }
    }

    // ================= TRICK HELPERS =================

    /**
     * الحصول على أو إنشاء الخدعة الحالية
     */
    fun getOrCreateCurrentTrick(): Trick {
        var currentTrick = tricks.lastOrNull()
        
        if (currentTrick == null || currentTrick.isComplete(players.size)) {
            currentTrick = Trick()
            tricks.add(currentTrick)
        }
        
        return currentTrick
    }

    /**
     * الخدعة الحالية
     */
    val currentTrick: Trick?
        get() = tricks.lastOrNull()
}
