package com.tasalicool.game.model

import java.util.UUID

data class Trick(
    val id: String = UUID.randomUUID().toString(),
    val cards: MutableMap<Int, Card> = mutableMapOf(),
    val playOrder: MutableList<Int> = mutableListOf(),
    var winnerId: Int = -1,
    var trickSuit: Suit? = null,
    var heartsBroken: Boolean = false,
    var trickNumber: Int = 0
) {
    companion object {
        const val COMPLETE_SIZE = 4
    }

    fun addCard(playerId: Int, card: Card) {
        cards[playerId] = card
        playOrder.add(playerId)

        // تحديد لون الخدعة من أول ورقة
        if (trickSuit == null) {
            trickSuit = card.suit
        }

        // كسر القلوب
        if (card.suit == Suit.HEARTS) {
            heartsBroken = true
        }
    }

    fun isComplete(playerCount: Int = COMPLETE_SIZE): Boolean {
        return cards.size == playerCount
    }

    fun reset() {
        cards.clear()
        playOrder.clear()
        winnerId = -1
        trickSuit = null
        heartsBroken = false
    }
}
