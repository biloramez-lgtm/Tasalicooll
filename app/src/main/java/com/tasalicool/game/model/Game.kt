package com.tasalicool.game.model

import java.util.UUID

data class Trick(
    val id: String = UUID.randomUUID().toString(),
    val cards: MutableMap<Int, Card> = mutableMapOf(),
    var winnerId: Int = -1,
    val playOrder: MutableList<Int> = mutableListOf(),
    var trickSuit: Suit? = null
) {
    fun addCard(playerId: Int, card: Card) {
        cards[playerId] = card
        playOrder.add(playerId)
        if (trickSuit == null) trickSuit = card.suit
    }

    fun isComplete(playerCount: Int): Boolean = cards.size == playerCount

    fun getWinnerId(trumpSuit: Suit = Suit.HEARTS): Int {
        if (cards.isEmpty()) return -1
        var winningCard = cards[playOrder.first()] ?: return -1
        var winnerId = playOrder.first()

        for (playerId in playOrder.drop(1)) {
            val currentCard = cards[playerId] ?: continue
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
}

enum class GamePhase { DEALING, BIDDING, PLAYING, ROUND_END, GAME_END }
enum class BiddingPhase { WAITING, PLAYER1_BIDDING, PLAYER2_BIDDING, PLAYER3_BIDDING, PLAYER4_BIDDING, COMPLETE }

data class Game(
    val id: String = UUID.randomUUID().toString(),
    val team1: Team,
    val team2: Team,
    val players: List<Player>,
    var currentRound: Int = 1,
    var currentTrick: Int = 1,
    var dealerIndex: Int = 0,
    var currentPlayerToPlayIndex: Int = 1,
    var gamePhase: GamePhase = GamePhase.DEALING,
    var biddingPhase: BiddingPhase = BiddingPhase.WAITING,
    val tricks: MutableList<Trick> = mutableListOf(),
    var isGameOver: Boolean = false,
    var winningTeamId: Int = -1
) {
    fun getCurrentPlayer(): Player = players[currentPlayerToPlayIndex]
    fun getNextPlayerIndex(): Int = (currentPlayerToPlayIndex + 1) % 4
    fun getDealerPlayer(): Player = players[dealerIndex]
    fun getRightOfDealerIndex(): Int = (dealerIndex + 1) % 4
    fun allPlayersHaveBid(): Boolean = players.all { it.bid > 0 }

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
}

data class RoundResult(
    val roundNumber: Int,
    val team1Score: Int,
    val team2Score: Int,
    val winner: Int
)
