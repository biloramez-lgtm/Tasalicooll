package com.tasalicool.game.model

import java.util.UUID

/**
 * Trick - Data holder only
 * No rules, no calculations
 */
data class Trick(
    val id: String = UUID.randomUUID().toString(),
    val cards: MutableMap<Int, Card> = mutableMapOf(),
    val playOrder: MutableList<Int> = mutableListOf(),

    var trickSuit: Suit? = null,
    var winnerId: Int = -1,
    var trickNumber: Int = 0
) {

    companion object {
        const val COMPLETE_SIZE = 4
    }

    /**
     * Add card to trick
     * Sets trickSuit on first card only
     */
    fun addCard(playerId: Int, card: Card) {
        if (cards.containsKey(playerId)) return

        cards[playerId] = card
        playOrder.add(playerId)

        if (trickSuit == null) {
            trickSuit = card.suit
        }
    }

    fun isComplete(playerCount: Int = COMPLETE_SIZE): Boolean {
        return cards.size == playerCount
    }

    fun isEmpty(): Boolean = cards.isEmpty()

    fun isFull(): Boolean = cards.size == COMPLETE_SIZE

    fun getCardByPlayerId(playerId: Int): Card? =
        cards[playerId]

    fun getPlayedCardsCount(): Int =
        cards.size

    fun getAllCards(): List<Card> =
        cards.values.toList()

    fun getCardsByPlayOrder(): List<Pair<Int, Card>> =
        playOrder.mapNotNull { id ->
            cards[id]?.let { id to it }
        }

    fun reset() {
        cards.clear()
        playOrder.clear()
        trickSuit = null
        winnerId = -1
    }

    override fun toString(): String {
        return "Trick(number=$trickNumber, cards=${cards.size}, suit=$trickSuit, winner=$winnerId)"
    }
}
