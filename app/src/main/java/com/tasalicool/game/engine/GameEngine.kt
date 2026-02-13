package com.tasalicool.game.engine

import com.tasalicool.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameEngine {
    private val cardRulesEngine = CardRulesEngine()
    private val scoringEngine = ScoringEngine()
    private val biddingEngine = BiddingEngine()

    fun initializeGame(team1: Team, team2: Team, dealerIndex: Int = 0): Game {
        val players = listOf(team1.player1, team1.player2, team2.player1, team2.player2)
        return Game(
            team1 = team1,
            team2 = team2,
            players = players,
            dealerIndex = dealerIndex,
            currentPlayerToPlayIndex = (dealerIndex + 1) % 4
        )
    }

    fun dealCards(game: Game) {
        val deck = Card.createGameDeck()
        var cardIndex = 0

        game.players.forEach { it.hand.clear() }

        for (i in 0 until 4) {
            for (j in 0 until 13) {
                game.players[i].addCard(deck[cardIndex++])
            }
            game.players[i].sortHand()
        }

        game.gamePhase = GamePhase.BIDDING
        game.biddingPhase = BiddingPhase.PLAYER1_BIDDING
    }

    fun placeBid(game: Game, playerIndex: Int, bid: Int): Boolean {
        val player = game.players[playerIndex]
        val minimumBid = scoringEngine.getMinimumBid(maxOf(game.team1.score, game.team2.score))

        if (!cardRulesEngine.validateBid(bid, player.hand.size, minimumBid)) return false

        player.bid = bid

        game.biddingPhase = when (game.biddingPhase) {
            BiddingPhase.PLAYER1_BIDDING -> BiddingPhase.PLAYER2_BIDDING
            BiddingPhase.PLAYER2_BIDDING -> BiddingPhase.PLAYER3_BIDDING
            BiddingPhase.PLAYER3_BIDDING -> BiddingPhase.PLAYER4_BIDDING
            BiddingPhase.PLAYER4_BIDDING -> BiddingPhase.COMPLETE
            else -> BiddingPhase.WAITING
        }

        if (game.biddingPhase == BiddingPhase.COMPLETE) {
            val totalBids = game.players.sumOf { it.bid }
            val minimumTotalBids = when {
                game.team1.score + game.team2.score >= 50 -> 14
                game.team1.score + game.team2.score >= 40 -> 13
                game.team1.score + game.team2.score >= 30 -> 12
                else -> 11
            }

            if (totalBids < minimumTotalBids) {
                game.players.forEach { it.bid = 0 }
                dealCards(game)
            } else {
                game.gamePhase = GamePhase.PLAYING
                game.currentPlayerToPlayIndex = game.getRightOfDealerIndex()
            }
        }

        return true
    }

    fun playCard(game: Game, playerIndex: Int, card: Card): Boolean {
        val player = game.players[playerIndex]
        var currentTrick = game.tricks.lastOrNull()

        if (currentTrick == null || currentTrick.isComplete(4)) {
            currentTrick = Trick()
            game.tricks.add(currentTrick)
        }

        val playedCards = currentTrick.cards.values.toList()
        if (!cardRulesEngine.canPlayCard(card, player, currentTrick, playedCards)) return false

        if (!player.removeCard(card)) return false
        currentTrick.addCard(playerIndex, card)

        if (currentTrick.isComplete(4)) {
            val winnerId = cardRulesEngine.calculateTrickWinner(currentTrick, Suit.HEARTS)
            currentTrick.winnerId = winnerId
            game.getPlayerById(winnerId)?.tricksWon++

            if (game.tricks.size == 13) {
                endRound(game)
            } else {
                game.currentPlayerToPlayIndex = winnerId
            }
        } else {
            game.currentPlayerToPlayIndex = (playerIndex + 1) % 4
        }

        return true
    }

    private fun endRound(game: Game) {
        val isAfter30 = maxOf(game.team1.score, game.team2.score) >= 30

        if (game.team1.isBidMet()) {
            game.team1.player1.score += scoringEngine.getPointsForBid(game.team1.player1.bid, isAfter30)
            game.team1.player2.score += scoringEngine.getPointsForBid(game.team1.player2.bid, isAfter30)
        } else {
            game.team1.player1.score -= game.team1.player1.bid
            game.team1.player2.score -= game.team1.player2.bid
        }

        if (game.team2.isBidMet()) {
            game.team2.player1.score += scoringEngine.getPointsForBid(game.team2.player1.bid, isAfter30)
            game.team2.player2.score += scoringEngine.getPointsForBid(game.team2.player2.bid, isAfter30)
        } else {
            game.team2.player1.score -= game.team2.player1.bid
            game.team2.player2.score -= game.team2.player2.bid
        }

        if (game.team1.isWinner) {
            game.isGameOver = true
            game.winningTeamId = 1
            game.gamePhase = GamePhase.GAME_END
        } else if (game.team2.isWinner) {
            game.isGameOver = true
            game.winningTeamId = 2
            game.gamePhase = GamePhase.GAME_END
        } else {
            game.resetForNewRound()
        }
    }

    fun getValidBids(playerIndex: Int, game: Game): List<Int> {
        val player = game.players[playerIndex]
        val minimumBid = scoringEngine.getMinimumBid(maxOf(game.team1.score, game.team2.score))
        return (minimumBid..player.hand.size).toList()
    }

    fun getValidCards(playerIndex: Int, game: Game): List<Card> {
        val player = game.players[playerIndex]
        val currentTrick = game.tricks.lastOrNull() ?: Trick()
        return cardRulesEngine.getValidPlayableCards(player, currentTrick)
    }
}

class CardRulesEngine {
    fun canPlayCard(card: Card, player: Player, trick: Trick, playedInTrick: List<Card>): Boolean {
        if (playedInTrick.isEmpty()) return true
        val trickSuit = trick.trickSuit ?: return true
        if (player.canFollowSuit(trickSuit)) return card.suit == trickSuit
        return true
    }

    fun calculateTrickWinner(trick: Trick, trumpSuit: Suit = Suit.HEARTS): Int {
        if (trick.cards.isEmpty()) return -1
        var winningCard = trick.cards[trick.playOrder.first()] ?: return -1
        var winnerId = trick.playOrder.first()

        for (playerId in trick.playOrder.drop(1)) {
            val currentCard = trick.cards[playerId] ?: continue
            if (currentCard.suit == trumpSuit && winningCard.suit != trumpSuit) {
                winningCard = currentCard
                winnerId = playerId
            } else if (currentCard.suit == winningCard.suit && currentCard.rank.value > winningCard.rank.value) {
                winningCard = currentCard
                winnerId = playerId
            }
        }
        return winnerId
    }

    fun validateBid(bid: Int, handSize: Int, minimumBid: Int): Boolean {
        return bid in minimumBid..handSize
    }

    fun getValidPlayableCards(player: Player, trick: Trick): List<Card> {
        if (trick.cards.isEmpty()) return player.hand.toList()
        val trickSuit = trick.trickSuit ?: return player.hand.toList()
        val cardsOfTrickSuit = player.hand.filter { it.suit == trickSuit }
        return if (cardsOfTrickSuit.isNotEmpty()) cardsOfTrickSuit else player.hand.toList()
    }
}

class ScoringEngine {
    fun getPointsForBid(bid: Int, isAfter30Points: Boolean): Int {
        val table = if (isAfter30Points) {
            mapOf(2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 14, 8 to 16, 9 to 27, 10 to 40, 11 to 40, 12 to 40, 13 to 40)
        } else {
            mapOf(2 to 2, 3 to 3, 4 to 4, 5 to 10, 6 to 12, 7 to 14, 8 to 16, 9 to 27, 10 to 40, 11 to 40, 12 to 40, 13 to 40)
        }
        return table[bid] ?: 0
    }

    fun getMinimumBid(teamScore: Int): Int = when {
        teamScore >= 50 -> 5
        teamScore >= 40 -> 4
        teamScore >= 30 -> 3
        else -> 2
    }
}

class BiddingEngine {
    fun getValidBids(player: Player, minimumBid: Int): List<Int> {
        val maxBid = player.hand.size
        return (minimumBid..maxBid).toList()
    }
}
