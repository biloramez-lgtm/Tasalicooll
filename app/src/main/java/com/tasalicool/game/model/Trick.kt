package com.tasalicool.game.model

import java.util.UUID

data class Trick(
    val id: String = UUID.randomUUID().toString(),
    val cards: MutableMap<Int, Card> = mutableMapOf(),
    var winnerId: Int = -1,
    val playOrder: MutableList<Int> = mutableListOf(),
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
        
        if (trickSuit == null && card.suit != null) {
            trickSuit = card.suit
        }

        // Check if hearts are broken
        if (card.suit == Suit.HEARTS && !heartsBroken) {
            heartsBroken = true
        }
    }

    fun isComplete(playerCount: Int = COMPLETE_SIZE): Boolean {
        return cards.size == playerCount
    }

    fun isEmpty(): Boolean {
        return cards.isEmpty()
    }

    fun isFull(): Boolean {
        return cards.size == COMPLETE_SIZE
    }

    fun getHighestCard(trumpSuit: Suit = Suit.HEARTS): Card? {
        if (cards.isEmpty()) return null

        // First check for trump (hearts)
        val trumpCards = cards.values.filter { it.suit == trumpSuit }
        if (trumpCards.isNotEmpty()) {
            return trumpCards.maxByOrNull { it.rank.value }
        }

        // Then check for cards matching the trick suit
        val trickSuitCards = cards.values.filter { it.suit == trickSuit }
        if (trickSuitCards.isNotEmpty()) {
            return trickSuitCards.maxByOrNull { it.rank.value }
        }

        // Return first card played if no other cards match
        return cards.values.firstOrNull()
    }

    fun getWinnerId(trumpSuit: Suit = Suit.HEARTS): Int {
        if (cards.isEmpty()) return -1

        val highestCard = getHighestCard(trumpSuit) ?: return -1
        
        return cards.entries.find { it.value == highestCard }?.key ?: -1
    }

    fun calculateWinner(trumpSuit: Suit = Suit.HEARTS): Int {
        if (cards.isEmpty()) return -1

        var winningCard = cards[playOrder.first()] ?: return -1
        var winnerId = playOrder.first()

        for (playerId in playOrder.drop(1)) {
            val currentCard = cards[playerId] ?: continue

            // Trump suit wins
            if (currentCard.suit == trumpSuit && winningCard.suit != trumpSuit) {
                winningCard = currentCard
                winnerId = playerId
            }
            // Same trump suit - higher rank wins
            else if (currentCard.suit == trumpSuit && winningCard.suit == trumpSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
            // Led suit wins over non-led, non-trump
            else if (winningCard.suit != trumpSuit && currentCard.suit == trickSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
            // Same suit as winning card - higher rank wins
            else if (currentCard.suit == winningCard.suit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
        }

        winnerId = winnerId
        return winnerId
    }

    fun getCardByPlayerId(playerId: Int): Card? {
        return cards[playerId]
    }

    fun getPlayedCardsCount(): Int {
        return cards.size
    }

    fun getRemainingPlayers(totalPlayers: Int): Int {
        return totalPlayers - cards.size
    }

    fun getAllCards(): List<Card> {
        return cards.values.toList()
    }

    fun getCardsByPlayOrder(): List<Pair<Int, Card>> {
        return playOrder.mapNotNull { playerId ->
            cards[playerId]?.let { card -> playerId to card }
        }
    }

    fun hasHeart(): Boolean {
        return cards.values.any { it.suit == Suit.HEARTS }
    }

    fun hasTrickSuit(): Boolean {
        return trickSuit != null && cards.values.any { it.suit == trickSuit }
    }

    fun getTrickSuitCards(): List<Card> {
        return if (trickSuit != null) {
            cards.values.filter { it.suit == trickSuit }
        } else {
            emptyList()
        }
    }

    fun getTrumpCards(trumpSuit: Suit = Suit.HEARTS): List<Card> {
        return cards.values.filter { it.suit == trumpSuit }
    }

    fun reset() {
        cards.clear()
        playOrder.clear()
        winnerId = -1
        trickSuit = null
        heartsBroken = false
    }

    fun copy(): Trick {
        return Trick(
            id = this.id,
            cards = this.cards.toMutableMap(),
            winnerId = this.winnerId,
            playOrder = this.playOrder.toMutableList(),
            trickSuit = this.trickSuit,
            heartsBroken = this.heartsBroken,
            trickNumber = this.trickNumber
        )
    }

    override fun toString(): String {
        return "Trick(number=$trickNumber, cards=${cards.size}, suit=$trickSuit, winner=$winnerId)"
    }
