package com.klosemiroslave.tasalicooll.engine

import com.klosemiroslave.tasalicooll.model.Player

object BiddingEngine {
    fun getMinimumBid(player: Player): Int {
        return when {
            player.score >= 50 -> 5
            player.score >= 40 -> 4
            player.score >= 30 -> 3
            else -> 2
        }
    }

    fun validateTotalBids(players: List<Player>): Boolean {
        val totalBids = players.sumOf { it.bid }
        val minTotal = when {
            players.any { it.score >= 50 } -> 14
            players.any { it.score >= 40 } -> 13
            players.any { it.score >= 30 } -> 12
            else -> 0
        }
        return totalBids >= minTotal
    }
}
