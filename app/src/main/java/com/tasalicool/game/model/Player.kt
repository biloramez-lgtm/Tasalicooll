package com.tasalicool.game.model

data class Player(
    val id: Int,
    val name: String,
    val isAI: Boolean = false,
    var hand: MutableList<Card> = mutableListOf(),
    var score: Int = 0,
    var bid: Int = 0,
    var tricksWon: Int = 0,
    var position: Int = 0
) {
    fun canBid(minimumBid: Int): Boolean = bid == 0 && hand.isNotEmpty()

    fun resetRound() {
        hand.clear()
        bid = 0
        tricksWon = 0
    }

    fun getMinimumBid(teamScore: Int): Int = when {
        teamScore >= 50 -> 5
        teamScore >= 40 -> 4
        teamScore >= 30 -> 3
        else -> 2
    }

    fun canFollowSuit(playedSuit: Suit): Boolean {
        return hand.any { it.suit == playedSuit }
    }

    fun removeCard(card: Card): Boolean {
        return hand.remove(card)
    }

    fun addCard(card: Card) {
        hand.add(card)
    }

    fun sortHand() {
        hand.sortWith(compareBy({ it.suit }, { it.rank.value }))
    }
}

data class Team(
    val id: Int,
    val name: String,
    val player1: Player,
    val player2: Player
) {
    val score: Int get() = player1.score + player2.score
    val isWinner: Boolean get() = score >= 41 && player1.score > 0 && player2.score > 0

    fun resetRound() {
        player1.resetRound()
        player2.resetRound()
    }

    fun getTotalBid(): Int = player1.bid + player2.bid
    fun getTotalTricksWon(): Int = player1.tricksWon + player2.tricksWon
    fun isBidMet(): Boolean = getTotalTricksWon() >= getTotalBid()
}
