package com.tarneeb.game.engine

import com.tarneeb.game.model.Card
import com.tarneeb.game.model.Game
import com.tarneeb.game.model.Player
import com.tarneeb.game.model.Suit
import kotlin.random.Random

class AIPlayer(val difficulty: Difficulty = Difficulty.MEDIUM) {

    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    fun selectBid(player: Player, game: Game, biddingEngine: BiddingEngine, scoringEngine: ScoringEngine): Int {
        return when (difficulty) {
            Difficulty.EASY -> easyBid(player, game, biddingEngine, scoringEngine)
            Difficulty.MEDIUM -> mediumBid(player, game, biddingEngine, scoringEngine)
            Difficulty.HARD -> hardBid(player, game, biddingEngine, scoringEngine)
        }
    }

    fun selectCard(player: Player, game: Game, cardRulesEngine: CardRulesEngine): Card {
        return when (difficulty) {
            Difficulty.EASY -> easyCard(player, game, cardRulesEngine)
            Difficulty.MEDIUM -> mediumCard(player, game, cardRulesEngine)
            Difficulty.HARD -> hardCard(player, game, cardRulesEngine)
        }
    }

    private fun easyBid(player: Player, game: Game, biddingEngine: BiddingEngine, scoringEngine: ScoringEngine): Int {
        val minimumBid = scoringEngine.getMinimumBid(maxOf(game.team1.score, game.team2.score))
        return biddingEngine.suggestBid(player, minimumBid)
    }

    private fun mediumBid(player: Player, game: Game, biddingEngine: BiddingEngine, scoringEngine: ScoringEngine): Int {
        return biddingEngine.getAIBid(player, game, scoringEngine)
    }

    private fun hardBid(player: Player, game: Game, biddingEngine: BiddingEngine, scoringEngine: ScoringEngine): Int {
        val minimumBid = scoringEngine.getMinimumBid(maxOf(game.team1.score, game.team2.score))
        val highCards = player.hand.count { it.rank.value >= 10 }
        val suitDistribution = player.hand.groupingBy { it.suit }.eachCount()
        val averageSuitCards = suitDistribution.values.average().toInt()

        // More sophisticated bidding based on hand strength
        val handStrength = (highCards * 1.5 + averageSuitCards * 0.5).toInt()
        return (minimumBid + handStrength).coerceIn(minimumBid, player.hand.size)
    }

    private fun easyCard(player: Player, game: Game, cardRulesEngine: CardRulesEngine): Card {
        val validCards = cardRulesEngine.getValidPlayableCards(player, game.tricks.lastOrNull() ?: return player.hand.first())
        return validCards.random()
    }

    private fun mediumCard(player: Player, game: Game, cardRulesEngine: CardRulesEngine): Card {
        val validCards = cardRulesEngine.getValidPlayableCards(player, game.tricks.lastOrNull() ?: return player.hand.first())

        // Try to win trick if partner bid, otherwise play low card
        val trick = game.tricks.lastOrNull() ?: return validCards.first()
        val team = game.getTeamByPlayerId(player.id)

        return if (trick.cards.size > 0) {
            val highestCard = validCards.maxByOrNull { it.rank.value }
            if (highestCard?.rank?.value ?: 0 > 10) {
                highestCard ?: validCards.first()
            } else {
                validCards.minByOrNull { it.rank.value } ?: validCards.first()
            }
        } else {
            validCards.first()
        }
    }

    private fun hardCard(player: Player, game: Game, cardRulesEngine: CardRulesEngine): Card {
        val validCards = cardRulesEngine.getValidPlayableCards(player, game.tricks.lastOrNull() ?: return player.hand.first())
        val trick = game.tricks.lastOrNull() ?: return validCards.first()
        val team = game.getTeamByPlayerId(player.id) ?: return validCards.first()

        // Strategic play based on game state
        if (trick.cards.isEmpty()) {
            // Lead with a strong suit
            return player.hand
                .groupingBy { it.suit }
                .eachCount()
                .maxByOrNull { it.value }
                ?.let { maxSuit ->
                    player.hand.filter { it.suit == maxSuit.key }
                        .minByOrNull { it.rank.value } ?: validCards.first()
                } ?: validCards.first()
        }

        // Try to win if partner bid well
        val partnerId = if (team == game.team1) team.player1.id else team.player2.id
        val partnerBid = if (team == game.team1) team.player1.bid + team.player2.bid else team.player1.bid + team.player2.bid

        return if (partnerBid >= 8) {
            // Aggressive play to help partner
            validCards.maxByOrNull { it.rank.value } ?: validCards.first()
        } else {
            // Conservative play
            validCards.minByOrNull { it.rank.value } ?: validCards.first()
        }
    }
}
