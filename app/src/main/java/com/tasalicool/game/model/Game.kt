package com.tasalicool.game.model

import java.util.UUID

// ================= ENUMS =================

enum class GamePhase {
    DEALING, BIDDING, PLAYING, ROUND_END, GAME_END
}

enum class BiddingPhase {
    WAITING, ACTIVE, FINISHED
}

enum class GameMode {
    DEFAULT, CUSTOM
}

// ================= GAME CLASS =================

data class Game(
    val id: String = UUID.randomUUID().toString(),

    val team1: Team,
    val team2: Team,
    val players: List<Player>,

    var round: Int = 1,
    var gameMode: GameMode = GameMode.DEFAULT,
    var duration: Long = 0L
) {

    // ================= STATE =================

    var gamePhase: GamePhase = GamePhase.DEALING
        private set

    var biddingPhase: BiddingPhase = BiddingPhase.WAITING
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

    fun currentPlayer(): Player = players[currentPlayerIndex]

    fun dealerPlayer(): Player = players[dealerIndex]

    fun rightOfDealerIndex(): Int = (dealerIndex + 1) % players.size

    fun nextPlayerIndex(): Int = (currentPlayerIndex + 1) % players.size

    fun getTeamByPlayer(playerId: Int): Team? =
        if (team1.players.any { it.id == playerId }) team1
        else if (team2.players.any { it.id == playerId }) team2
        else null

    fun getOpponentTeam(playerId: Int): Team? =
        if (team1.players.any { it.id == playerId }) team2
        else if (team2.players.any { it.id == playerId }) team1
        else null

    fun getOrCreateCurrentTrick(): Trick {
        if (tricks.isEmpty() || tricks.last().isComplete(players.size)) {
            val trick = Trick()
            tricks.add(trick)
        }
        return tricks.last()
    }

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
        round++
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
            GamePhase.DEALING -> "Dealing - Round $round"
            GamePhase.BIDDING -> "Bidding"
            GamePhase.PLAYING -> "Playing - Trick $currentTrickNumber"
            GamePhase.ROUND_END -> "Round End"
            GamePhase.GAME_END -> "Game Over"
        }
}
