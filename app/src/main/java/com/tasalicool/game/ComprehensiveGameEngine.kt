package com.tasalicool.game.engine

import com.tasalicool.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class ComprehensiveGameEngine {
    private val cardRulesEngine = CardRulesEngine()
    private val scoringEngine = ScoringEngine()
    private val biddingEngine = BiddingEngine()

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Initialize new game
    fun initializeGame(
        team1: Team,
        team2: Team,
        dealerIndex: Int = 0
    ): Game {
        val players = listOf(team1.player1, team1.player2, team2.player1, team2.player2)
        val game = Game(
            team1 = team1,
            team2 = team2,
            players = players,
            dealerIndex = dealerIndex,
            currentPlayerToPlayIndex = (dealerIndex + 1) % 4
        )
        _gameState.value = game
        return game
    }

    // Deal cards to all players
    fun dealCards(game: Game) {
        try {
            val deck = Card.createGameDeck()
            var cardIndex = 0

            // Clear existing hands
            game.players.forEach { it.hand.clear() }

            // Deal 13 cards to each player
            for (i in 0 until 4) {
                for (j in 0 until 13) {
                    game.players[i].addCard(deck[cardIndex++])
                }
                game.players[i].sortHand()
            }

            game.gamePhase = GamePhase.BIDDING
            game.biddingPhase = BiddingPhase.PLAYER1_BIDDING
            _gameState.value = game
        } catch (e: Exception) {
            _error.value = "Error dealing cards: ${e.message}"
        }
    }

    // Process bid
    fun placeBid(game: Game, playerIndex: Int, bid: Int): Boolean {
        return try {
            val player = game.players[playerIndex]
            val minimumBid = scoringEngine.getMinimumBid(
                maxOf(game.team1.score, game.team2.score)
            )

            // Validate bid
            if (!cardRulesEngine.validateBid(bid, player.hand.size, minimumBid)) {
                _error.value = "Invalid bid. Must be between $minimumBid and ${player.hand.size}"
                return false
            }

            player.bid = bid

            // Move to next bidding phase
            game.biddingPhase = when (game.biddingPhase) {
                BiddingPhase.PLAYER1_BIDDING -> BiddingPhase.PLAYER2_BIDDING
                BiddingPhase.PLAYER2_BIDDING -> BiddingPhase.PLAYER3_BIDDING
                BiddingPhase.PLAYER3_BIDDING -> BiddingPhase.PLAYER4_BIDDING
                BiddingPhase.PLAYER4_BIDDING -> BiddingPhase.COMPLETE
                else -> BiddingPhase.WAITING
            }

            if (game.biddingPhase == BiddingPhase.COMPLETE) {
                val totalBids = game.players.sumOf { it.bid }
                val minimumTotalBids = game.getMinimumTotalBids()

                if (totalBids < minimumTotalBids) {
                    // Reshuffle
                    game.players.forEach { it.bid = 0 }
                    dealCards(game)
                } else {
                    // Start playing phase
                    game.gamePhase = GamePhase.PLAYING
                    game.currentPlayerToPlayIndex = game.getRightOfDealerIndex()
                }
            }

            _gameState.value = game
            true
        } catch (e: Exception) {
            _error.value = "Error placing bid: ${e.message}"
            false
        }
    }

    // Play a card
    fun playCard(game: Game, playerIndex: Int, card: Card): Boolean {
        return try {
            val player = game.players[playerIndex]

            // Get or create current trick
            var currentTrick = game.tricks.lastOrNull()
            if (currentTrick == null || currentTrick.isComplete(4)) {
                currentTrick = Trick()
                game.tricks.add(currentTrick)
            }

            // Validate card play
            val playedCards = currentTrick.cards.values.toList()
            if (!cardRulesEngine.canPlayCard(card, player, currentTrick, playedCards)) {
                _error.value = "Invalid card play. Must follow suit if possible."
                return false
            }

            // Remove card from hand and add to trick
            if (!player.removeCard(card)) {
                _error.value = "Card not found in hand."
                return false
            }
            currentTrick.addCard(playerIndex, card)

            // Check if trick is complete
            if (currentTrick.isComplete(4)) {
                val winnerId = cardRulesEngine.calculateTrickWinner(currentTrick, Suit.HEARTS)
                currentTrick.winnerId = winnerId
                
                // Increment tricks won
                game.getPlayerById(winnerId)?.tricksWon++

                // Check if all tricks are played
                if (game.tricks.size == 13) {
                    endRound(game)
                } else {
                    // Next player is trick winner
                    game.currentPlayerToPlayIndex = winnerId
                }
            } else {
                // Next player
                game.currentPlayerToPlayIndex = (playerIndex + 1) % 4
            }

            _gameState.value = game
            true
        } catch (e: Exception) {
            _error.value = "Error playing card: ${e.message}"
            false
        }
    }

    // End round and calculate scores
    private fun endRound(game: Game) {
        try {
            val isAfter30 = maxOf(game.team1.score, game.team2.score) >= 30

            // Update team 1 scores
            if (game.team1.isBidMet()) {
                game.team1.player1.score += scoringEngine.getPointsForBid(game.team1.player1.bid, isAfter30)
                game.team1.player2.score += scoringEngine.getPointsForBid(game.team1.player2.bid, isAfter30)
            } else {
                game.team1.player1.score -= game.team1.player1.bid
                game.team1.player2.score -= game.team1.player2.bid
            }

            // Update team 2 scores
            if (game.team2.isBidMet()) {
                game.team2.player1.score += scoringEngine.getPointsForBid(game.team2.player1.bid, isAfter30)
                game.team2.player2.score += scoringEngine.getPointsForBid(game.team2.player2.bid, isAfter30)
            } else {
                game.team2.player1.score -= game.team2.player1.bid
                game.team2.player2.score -= game.team2.player2.bid
            }

            // Check for winner
            if (game.team1.isWinner) {
                game.isGameOver = true
                game.winningTeamId = 1
                game.gamePhase = GamePhase.GAME_END
            } else if (game.team2.isWinner) {
                game.isGameOver = true
                game.winningTeamId = 2
                game.gamePhase = GamePhase.GAME_END
            } else {
                // Next round
                game.resetForNewRound()
            }

            _gameState.value = game
        } catch (e: Exception) {
            _error.value = "Error ending round: ${e.message}"
        }
    }

    // Get valid bids for a player
    fun getValidBids(playerIndex: Int): List<Int> {
        val game = _gameState.value ?: return emptyList()
        val player = game.players[playerIndex]
        val minimumBid = scoringEngine.getMinimumBid(
            maxOf(game.team1.score, game.team2.score)
        )
        return biddingEngine.getValidBids(player, minimumBid)
    }

    // Get valid cards a player can play
    fun getValidCards(playerIndex: Int): List<Card> {
        val game = _gameState.value ?: return emptyList()
        val player = game.players[playerIndex]
        val currentTrick = game.tricks.lastOrNull() ?: Trick()
        return cardRulesEngine.getValidPlayableCards(player, currentTrick)
    }

    // Clear error
    fun clearError() {
        _error.value = null
    }
}

class CardRulesEngine {
    fun canPlayCard(
        card: Card,
        player: Player,
        trick: Trick,
        playedInTrick: List<Card>
    ): Boolean {
        if (playedInTrick.isEmpty()) return true

        val trickSuit = trick.trickSuit ?: return true

        if (player.canFollowSuit(trickSuit)) {
            return card.suit == trickSuit
        }

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
            } else if (currentCard.suit == trumpSuit && winningCard.suit == trumpSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            } else if (winningCard.suit != trumpSuit && currentCard.suit == trick.trickSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            } else if (currentCard.suit == winningCard.suit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
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
        return if (cardsOfTrickSuit.isNotEmpty()) {
            cardsOfTrickSuit
        } else {
            player.hand.toList()
        }
    }
}

class ScoringEngine {
    fun getPointsForBid(bid: Int, isAfter30Points: Boolean): Int {
        val table = if (isAfter30Points) {
            mapOf(
                2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6,
                7 to 14, 8 to 16, 9 to 27, 10 to 40, 11 to 40, 12 to 40, 13 to 40
            )
        } else {
            mapOf(
                2 to 2, 3 to 3, 4 to 4, 5 to 10, 6 to 12,
                7 to 14, 8 to 16, 9 to 27, 10 to 40, 11 to 40, 12 to 40, 13 to 40
            )
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

    fun suggestBid(player: Player, minimumBid: Int): Int {
        val highCardCount = player.hand.count { it.rank.value >= 10 }
        val suitCounts = player.hand.groupingBy { it.suit }.eachCount()
        val maxSuitCount = suitCounts.values.maxOrNull() ?: 0

        var estimatedTricks = (highCardCount / 2) + (maxSuitCount / 3)
        estimatedTricks = estimatedTricks.coerceIn(minimumBid, player.hand.size)

        return estimatedTricks
    }
}
