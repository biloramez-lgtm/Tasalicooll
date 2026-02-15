package com.klosemiroslave.tasalicooll.engine

import com.klosemiroslave.tasalicooll.model.Player

object ScoringEngine {
    private val scoreTableBelow30 = mapOf(
        2 to 2, 3 to 3, 4 to 4, 5 to 10, 6 to 12,
        7 to 14, 8 to 16, 9 to 27, 10 to 40, 11 to 40,
        12 to 40, 13 to 40
    )
    private val scoreTableAbove30 = mapOf(
        2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6,
        7 to 7, 8 to 8, 9 to 9, 10 to 10, 11 to 11,
        12 to 12, 13 to 13
    )

    fun calculateScore(player: Player) {
        val table = if (player.score >= 30) scoreTableAbove30 else scoreTableBelow30
        if (player.tricksWon >= player.bid) {
            player.score += table[player.bid] ?: player.bid
        } else {
            player.score -= player.bid
        }
    }
}
