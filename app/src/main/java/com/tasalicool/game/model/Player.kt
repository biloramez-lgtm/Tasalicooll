package com.tasalicool.game.model

data class Player(
    val id: Int,
    val name: String,
    val isAI: Boolean = false,
    val position: Int = 0,
    var hand: MutableList<Card> = mutableListOf(),
    var score: Int = 0,
    var bid: Int = 0,
    var tricksWon: Int = 0,

    // ==================== NEW ====================
    var onPlayCard: ((Card) -> Boolean)? = null
) {

    /* ================= BIDDING ================= */

    fun canBid(): Boolean =
        bid == 0 && hand.isNotEmpty()

    fun getMinimumBid(teamScore: Int): Int = when {
        teamScore >= 50 -> 5
        teamScore >= 40 -> 4
        teamScore >= 30 -> 3
        else -> 2
    }

    /* ================= PLAYING ================= */

    fun canFollowSuit(playedSuit: Suit): Boolean =
        hand.any { it.suit == playedSuit }

    fun removeCard(card: Card): Boolean =
        hand.remove(card)

    fun addCard(card: Card) {
        hand.add(card)
    }

    fun sortHand() {
        hand.sortWith(compareBy({ it.suit }, { it.rank.value }))
    }

    fun playCard(card: Card): Boolean {
        return onPlayCard?.invoke(card) ?: false
    }

    /* ================= ROUND ================= */

    fun resetRound(): Player =
        copy(
            hand = mutableListOf(),
            bid = 0,
            tricksWon = 0
        )
}
