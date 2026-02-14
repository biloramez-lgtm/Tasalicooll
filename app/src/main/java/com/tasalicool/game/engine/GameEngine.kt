package com.tasalicool.game.engine

import com.tasalicool.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameEngine {

    private val cardRulesEngine = CardRulesEngine()
    private val scoringEngine = ScoringEngine()
    private val biddingEngine = BiddingEngine()

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /* ======================= INITIALIZATION ======================= */

    fun initializeGame(
        team1: Team,
        team2: Team,
        dealerIndex: Int = 0
    ): Game {
        val players = listOf(
            team1.player1,
            team1.player2,
            team2.player1,
            team2.player2
        )

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

    /** 🔥 NEW – Professional default initializer */
    fun initializeDefaultGame(
        team1Name: String,
        team2Name: String,
        dealerIndex: Int = 0
    ) {
        val p1 = Player(0, "$team1Name-1", isAI = false)
        val p2 = Player(1, "$team2Name-1", isAI = true)
        val p3 = Player(2, "$team1Name-2", isAI = false)
        val p4 = Player(3, "$team2Name-2", isAI = true)

        val team1 = Team(1, team1Name, p1, p3)
        val team2 = Team(2, team2Name, p2, p4)

        val game = initializeGame(team1, team2, dealerIndex)
        dealCards(game)
    }

    fun restartGame() {
        val game = _gameState.value ?: return
        game.resetForNewRound()
        dealCards(game)
    }

    /* ======================= DEALING ======================= */

    fun dealCards(game: Game) {
        try {
            val deck = Card.createGameDeck()
            var index = 0

            game.players.forEach { it.hand.clear() }

            repeat(4) { i ->
                repeat(13) {
                    game.players[i].addCard(deck[index++])
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

    /* ======================= BIDDING ======================= */

    fun placeBid(game: Game, playerIndex: Int, bid: Int): Boolean {
        return try {
            val player = game.players[playerIndex]
            val minimumBid = scoringEngine.getMinimumBid(
                maxOf(game.team1.score, game.team2.score)
            )

            if (!cardRulesEngine.validateBid(bid, player.hand.size, minimumBid)) {
                _error.value = "Invalid bid"
                return false
            }

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
                val minimumTotal = game.getMinimumTotalBids()

                if (totalBids < minimumTotal) {
                    game.players.forEach { it.bid = 0 }
                    dealCards(game)
                } else {
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

    /* ======================= PLAYING ======================= */

    fun playCard(game: Game, playerIndex: Int, card: Card): Boolean {
        return try {
            val player = game.players[playerIndex]

            var trick = game.tricks.lastOrNull()
            if (trick == null || trick.isComplete(4)) {
                trick = Trick()
                game.tricks.add(trick)
            }

            val played = trick.cards.values.toList()
            if (!cardRulesEngine.canPlayCard(card, player, trick, played)) {
                _error.value = "Invalid card"
                return false
            }

            if (!player.removeCard(card)) {
                _error.value = "Card not in hand"
                return false
            }

            trick.addCard(playerIndex, card)

            if (trick.isComplete(4)) {
                val winnerId =
                    cardRulesEngine.calculateTrickWinner(trick, Suit.HEARTS)

                trick.winnerId = winnerId
                game.getPlayerById(winnerId)?.tricksWon++

                if (game.tricks.size == 13) {
                    endRound(game)
                } else {
                    game.currentPlayerToPlayIndex = winnerId
                }
            } else {
                game.currentPlayerToPlayIndex = (playerIndex + 1) % 4
            }

            _gameState.value = game
            true

        } catch (e: Exception) {
            _error.value = "Error playing card: ${e.message}"
            false
        }
    }

    /* ======================= ROUND END ======================= */

    private fun endRound(game: Game) {
        try {
            val after30 = maxOf(game.team1.score, game.team2.score) >= 30

            fun apply(team: Team) {
                if (team.isBidMet()) {
                    team.player1.score += scoringEngine.getPointsForBid(team.player1.bid, after30)
                    team.player2.score += scoringEngine.getPointsForBid(team.player2.bid, after30)
                } else {
                    team.player1.score -= team.player1.bid
                    team.player2.score -= team.player2.bid
                }
            }

            apply(game.team1)
            apply(game.team2)

            when {
                game.team1.isWinner -> {
                    game.isGameOver = true
                    game.winningTeamId = 1
                    game.gamePhase = GamePhase.GAME_END
                }
                game.team2.isWinner -> {
                    game.isGameOver = true
                    game.winningTeamId = 2
                    game.gamePhase = GamePhase.GAME_END
                }
                else -> game.resetForNewRound()
            }

            _gameState.value = game

        } catch (e: Exception) {
            _error.value = "Error ending round: ${e.message}"
        }
    }

    /* ======================= HELPERS ======================= */

    fun getValidBids(playerIndex: Int): List<Int> {
        val game = _gameState.value ?: return emptyList()
        val minBid = scoringEngine.getMinimumBid(
            maxOf(game.team1.score, game.team2.score)
        )
        return biddingEngine.getValidBids(game.players[playerIndex], minBid)
    }

    fun getValidCards(playerIndex: Int): List<Card> {
        val game = _gameState.value ?: return emptyList()
        val trick = game.tricks.lastOrNull() ?: Trick()
        return cardRulesEngine.getValidPlayableCards(game.players[playerIndex], trick)
    }

    fun clearError() {
        _error.value = null
    }
}

/* ======================= ENGINES ======================= */

class CardRulesEngine {

    fun canPlayCard(card: Card, player: Player, trick: Trick, played: List<Card>): Boolean {
        if (played.isEmpty()) return true
        val suit = trick.trickSuit ?: return true
        return !player.canFollowSuit(suit) || card.suit == suit
    }

    fun calculateTrickWinner(trick: Trick, trump: Suit = Suit.HEARTS): Int {
        var winner = trick.playOrder.first()
        var winningCard = trick.cards[winner]!!

        for (id in trick.playOrder.drop(1)) {
            val card = trick.cards[id] ?: continue
            val better =
                (card.suit == trump && winningCard.suit != trump) ||
                (card.suit == winningCard.suit && card.rank.value > winningCard.rank.value)
            if (better) {
                winner = id
                winningCard = card
            }
        }
        return winner
    }

    fun validateBid(bid: Int, handSize: Int, min: Int) = bid in min..handSize

    fun getValidPlayableCards(player: Player, trick: Trick): List<Card> {
        val suit = trick.trickSuit ?: return player.hand
        return player.hand.filter { it.suit == suit }.ifEmpty { player.hand }
    }
}

class ScoringEngine {

    fun getPointsForBid(bid: Int, after30: Boolean): Int {
        val table = if (after30)
            mapOf(2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 14, 8 to 16, 9 to 27, 10 to 40, 11 to 40, 12 to 40, 13 to 40)
        else
            mapOf(2 to 2, 3 to 3, 4 to 4, 5 to 10, 6 to 12, 7 to 14, 8 to 16, 9 to 27, 10 to 40, 11 to 40, 12 to 40, 13 to 40)

        return table[bid] ?: 0
    }

    fun getMinimumBid(score: Int) = when {
        score >= 50 -> 5
        score >= 40 -> 4
        score >= 30 -> 3
        else -> 2
    }
}

class BiddingEngine {

    fun getValidBids(player: Player, min: Int): List<Int> =
        (min..player.hand.size).toList()

    fun suggestBid(player: Player, min: Int): Int {
        val high = player.hand.count { it.rank.value >= 10 }
        val maxSuit = player.hand.groupingBy { it.suit }.eachCount().values.maxOrNull() ?: 0
        return ((high / 2) + (maxSuit / 3)).coerceIn(min, player.hand.size)
    }
}
