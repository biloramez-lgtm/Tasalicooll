package com.tarneeb.game.model

data class Player(
    val id: Int,
    val name: String,
    val isAI: Boolean = false,
    var hand: MutableList<Card> = mutableListOf(),
    var score: Int = 0,
    var bid: Int = 0,
    var tricksWon: Int = 0,
    var position: Int = 0 // 0=South, 1=West, 2=North, 3=East
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

    fun getMaximumBid(): Int = hand.size

    fun canFollowSuit(playedSuit: Suit): Boolean {
        return hand.any { it.suit == playedSuit }
    }

    fun canPlayCard(card: Card, playedSuit: Suit?, trickSuit: Suit?): Boolean {
        if (playedSuit == null) return true // First card in trick

        // Must follow suit if possible
        if (canFollowSuit(playedSuit)) {
            return card.suit == playedSuit
        }

        // Can play any card if can't follow suit
        return true
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
    val score: Int
        get() = player1.score + player2.score

    val isWinner: Boolean
        get() = score >= 41 && player1.score > 0 && player2.score > 0

    fun resetRound() {
        player1.resetRound()
        player2.resetRound()
    }

    fun getTotalBid(): Int = player1.bid + player2.bid

    fun getTotalTricksWon(): Int = player1.tricksWon + player2.tricksWon

    fun isBidMet(): Boolean {
        return getTotalTricksWon() >= getTotalBid()
    }

    fun getScoresToAdd(isAfter30: Boolean): Pair<Int, Int> {
        return if (isBidMet()) {
            Pair(getPointsForBid(player1.bid, isAfter30),
                 getPointsForBid(player2.bid, isAfter30))
        } else {
            Pair(-player1.bid, -player2.bid)
        }
    }

    private fun getPointsForBid(bid: Int, isAfter30: Boolean): Int {
        return when {
            bid < 2 || bid > 13 -> 0
            bid <= 4 -> bid
            !isAfter30 -> when (bid) {
                5 -> 10
                6 -> 12
                7 -> 14
                8 -> 16
                9 -> 27
                10, 11, 12, 13 -> 40
                else -> 0
            }
            else -> when (bid) {
                5 -> 5
                6 -> 6
                7 -> 14
                8 -> 16
                9 -> 27
                10, 11, 12, 13 -> 40
                else -> 0
            }
        }
    }
}
