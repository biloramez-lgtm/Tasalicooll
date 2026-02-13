package com.tarneeb.game.engine

import com.tarneeb.game.model.Game
import com.tarneeb.game.model.Player
import com.tarneeb.game.utils.GameConstants
import kotlin.random.Random

class BiddingEngine {

    fun getValidBids(player: Player, minimumBid: Int): List<Int> {
        val maxBid = player.hand.size
        return (minimumBid..maxBid).toList()
    }

    fun suggestBid(player: Player, minimumBid: Int): Int {
        if (player.hand.isEmpty()) return minimumBid

        // Count high cards (A, K, Q, J, 10)
        val highCardCount = player.hand.count { it.rank.value >= 10 }

        // Count cards of the same suit (potential for tricks)
        val suitCounts = player.hand.groupingBy { it.suit }.eachCount()
        val maxSuitCount = suitCounts.values.maxOrNull() ?: 0

        // Simple heuristic: estimate tricks
        var estimatedTricks = (highCardCount / 2) + (maxSuitCount / 3)
        estimatedTricks = estimatedTricks.coerceIn(minimumBid, player.hand.size)

        return estimatedTricks
    }

    fun getAIBid(player: Player, game: Game, scoringEngine: ScoringEngine): Int {
        val minimumBid = scoringEngine.getMinimumBid(
            maxOf(game.team1.score, game.team2.score)
        )
        val validBids = getValidBids(player, minimumBid)

        // Get team for this player
        val team = game.getTeamByPlayerId(player.id)
        val partnerBid = if (team == game.team1) {
            game.team1.player1.bid + game.team1.player2.bid
        } else {
            game.team2.player1.bid + game.team2.player2.bid
        }

        val suggestedBid = suggestBid(player, minimumBid)

        // Adjust based on current bidding
        return if (partnerBid > 0) {
            // Partner has already bid, try to complement
            val totalNeeded = 10
            val ownBid = (totalNeeded - partnerBid).coerceIn(minimumBid, player.hand.size)
            ownBid.coerceIn(validBids.minOrNull() ?: minimumBid, validBids.maxOrNull() ?: 13)
        } else {
            suggestedBid.coerceIn(validBids.minOrNull() ?: minimumBid, validBids.maxOrNull() ?: 13)
        }
    }

    fun isValidBid(bid: Int, hand: List<*>, minimumBid: Int): Boolean {
        return bid in minimumBid..hand.size
    }

    fun areAllBidsValid(game: Game): Boolean {
        return game.players.all { it.bid > 0 }
    }

    fun getTotalBids(game: Game): Int {
        return game.players.sumOf { it.bid }
    }
}
