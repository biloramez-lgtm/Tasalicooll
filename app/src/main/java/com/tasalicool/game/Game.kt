package com.tarneeb.game.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class Trick(
    val id: String = UUID.randomUUID().toString(),
    val cards: MutableMap<Int, Card> = mutableMapOf(), // playerId -> Card
    var winnerId: Int = -1,
    val playOrder: MutableList<Int> = mutableListOf(),
    var trickSuit: Suit? = null,
    var heartsBroken: Boolean = false
) {
    fun addCard(playerId: Int, card: Card) {
        cards[playerId] = card
        playOrder.add(playerId)
        if (trickSuit == null) {
            trickSuit = card.suit
        }
    }

    fun isComplete(playerCount: Int): Boolean = cards.size == playerCount

    fun getHighestCard(trumpSuit: Suit = Suit.HEARTS): Card? {
        if (cards.isEmpty()) return null

        // Check if there are hearts
        val hearts = cards.values.filter { it.suit == trumpSuit }
        if (hearts.isNotEmpty()) {
            return hearts.maxByOrNull { it.rank.value }
        }

        // Check for cards matching the trick suit
        val trickSuitCards = cards.values.filter { it.suit == trickSuit }
        if (trickSuitCards.isNotEmpty()) {
            return trickSuitCards.maxByOrNull { it.rank.value }
        }

        return null
    }

    fun getWinnerId(trumpSuit: Suit = Suit.HEARTS): Int {
        val highestCard = getHighestCard(trumpSuit) ?: return -1
        return cards.entries.find { it.value == highestCard }?.key ?: -1
    }
}

data class GameState(
    val currentRound: Int = 1,
    val currentTrick: Int = 1,
    val currentPlayerIndex: Int = 0,
    val dealerIndex: Int = 0,
    val gamePhase: GamePhase = GamePhase.DEALING,
    val currentTrick: Trick? = null,
    val completedTricks: List<Trick> = emptyList(),
    val biddingPhase: BiddingPhase = BiddingPhase.WAITING
)

enum class GamePhase {
    DEALING,
    BIDDING,
    PLAYING,
    ROUND_END,
    GAME_END
}

enum class BiddingPhase {
    WAITING,
    PLAYER1_BIDDING,
    PLAYER2_BIDDING,
    PLAYER3_BIDDING,
    PLAYER4_BIDDING,
    COMPLETE
}

data class Game(
    val id: String = UUID.randomUUID().toString(),
    val team1: Team,
    val team2: Team,
    val players: List<Player>,
    var currentRound: Int = 1,
    var currentTrick: Int = 1,
    var dealerIndex: Int = 0,
    var currentPlayerToPlayIndex: Int = 1, // Right of dealer starts
    var gamePhase: GamePhase = GamePhase.DEALING,
    var biddingPhase: BiddingPhase = BiddingPhase.WAITING,
    val tricks: MutableList<Trick> = mutableListOf(),
    val roundHistory: MutableList<RoundResult> = mutableListOf(),
    var isGameOver: Boolean = false,
    var winningTeamId: Int = -1
) {
    fun getPlayers(): List<Player> = players

    fun getPlayerByPosition(position: Int): Player = players[position]

    fun getPlayerById(id: Int): Player? = players.find { it.id == id }

    fun getTeamByPlayerId(playerId: Int): Team? {
        return if (team1.player1.id == playerId || team1.player2.id == playerId) team1
        else if (team2.player1.id == playerId || team2.player2.id == playerId) team2
        else null
    }

    fun getCurrentPlayer(): Player = players[currentPlayerToPlayIndex]

    fun getNextPlayerIndex(): Int = (currentPlayerToPlayIndex + 1) % 4

    fun getDealerPlayer(): Player = players[dealerIndex]

    fun getRightOfDealerIndex(): Int = (dealerIndex + 1) % 4

    fun getMinimumTotalBids(): Int {
        val maxScore = maxOf(team1.score, team2.score)
        return when {
            maxScore >= 50 -> 14
            maxScore >= 40 -> 13
            maxScore >= 30 -> 12
            else -> 11
        }
    }

    fun resetForNewRound() {
        currentTrick = 1
        tricks.clear()
        dealerIndex = (dealerIndex + 1) % 4
        currentPlayerToPlayIndex = getRightOfDealerIndex()
        currentRound++
        team1.resetRound()
        team2.resetRound()
        gamePhase = GamePhase.DEALING
        biddingPhase = BiddingPhase.WAITING
    }

    fun allPlayersHaveBid(): Boolean {
        return players.all { it.bid > 0 }
    }
}

data class RoundResult(
    val roundNumber: Int,
    val team1Score: Int,
    val team2Score: Int,
    val team1Bid: Int,
    val team2Bid: Int,
    val team1TricksWon: Int,
    val team2TricksWon: Int,
    val winner: Int // teamId
)
