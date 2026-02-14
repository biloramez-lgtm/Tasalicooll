package com.tasalicool.game.model

data class Team(
    val id: Int,
    val name: String,
    val players: List<Player>
) {

    init {
        require(players.size == 2) {
            "Team must contain exactly 2 players"
        }
    }

    /* ================= SCORE ================= */

    val score: Int
        get() = players.sumOf { it.score }

    val totalBid: Int
        get() = players.sumOf { it.bid }

    val totalTricksWon: Int
        get() = players.sumOf { it.tricksWon }

    /* ================= RULES ================= */

    val isBidMet: Boolean
        get() = totalTricksWon >= totalBid

    val isWinner: Boolean
        get() = score >= 41 && players.all { it.score > 0 }

    /* ================= ROUND ================= */

    fun resetRound(): Team =
        copy(players = players.map { it.resetRound() })
}
