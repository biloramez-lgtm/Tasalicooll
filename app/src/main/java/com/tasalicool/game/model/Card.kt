package com.tasalicool.game.model

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val suit: Suit,
    val rank: Rank
) {
    companion object {
        fun createFullDeck(): List<Card> {
            return buildList {
                for (suit in Suit.values()) {
                    for (rank in Rank.values()) {
                        add(Card(suit, rank))
                    }
                }
            }
        }

        fun createGameDeck(): List<Card> {
            return createFullDeck().shuffled()
        }

        fun createDeckBysuit(suit: Suit): List<Card> {
            return createFullDeck().filter { it.suit == suit }
        }

        fun createDeckByRank(rank: Rank): List<Card> {
            return createFullDeck().filter { it.rank == rank }
        }

        fun validateCard(card: Card?): Boolean {
            return card != null && Suit.values().contains(card.suit) && Rank.values().contains(card.rank)
        }

        fun fromString(cardString: String): Card? {
            return try {
                val parts = cardString.split(" ")
                if (parts.size == 2) {
                    val rank = Rank.fromString(parts[0])
                    val suit = Suit.fromString(parts[1])
                    Card(suit, rank)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun toString(): String = "${rank.displayName}${suit.getSymbol()}"

    fun toFullString(): String = "${rank.getFullName()} of ${suit.getDisplayName()}"

    fun toShortString(): String = "${rank.displayName}${suit.getShortName()}"

    fun getImageResId(): Int = 0

    fun isTrump(): Boolean = suit == Suit.HEARTS

    fun isHighCard(): Boolean = rank.value >= 10

    fun isLowCard(): Boolean = rank.value <= 5

    fun isFaceCard(): Boolean = rank.isFaceCard()

    fun isNumberCard(): Boolean = rank.isNumberCard()

    fun canBeat(otherCard: Card, trickSuit: Suit?, trumpSuit: Suit = Suit.HEARTS): Boolean {
        if (this.suit == trumpSuit && otherCard.suit != trumpSuit) return true
        if (this.suit == trumpSuit && otherCard.suit == trumpSuit) return this.rank.value > otherCard.rank.value
        if (this.suit == trickSuit && otherCard.suit != trickSuit && otherCard.suit != trumpSuit) return true
        if (this.suit == otherCard.suit) return this.rank.value > otherCard.rank.value
        return false
    }

    fun isBetter(otherCard: Card, trickSuit: Suit?, trumpSuit: Suit = Suit.HEARTS): Boolean =
        canBeat(otherCard, trickSuit, trumpSuit)

    fun compareTo(otherCard: Card, trickSuit: Suit?, trumpSuit: Suit = Suit.HEARTS): Int =
        when {
            this.canBeat(otherCard, trickSuit, trumpSuit) -> 1
            otherCard.canBeat(this, trickSuit, trumpSuit) -> -1
            else -> 0
        }

    fun getStrength(): Int {
        var strength = rank.value * 10
        if (isTrump()) strength += 1000
        if (isFaceCard()) strength += 50
        if (isHighCard()) strength += 30
        return strength
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Card) return false
        return suit == other.suit && rank == other.rank
    }

    override fun hashCode(): Int {
        var result = suit.hashCode()
        result = 31 * result + rank.hashCode()
        return result
    }

    fun copy(): Card = Card(suit, rank)

    fun getColor(): String = suit.getColor()

    fun isRed(): Boolean = suit.isRed()

    fun isBlack(): Boolean = suit.isBlack()

    fun getPoints(): Int = rank.getPoints()

    fun matches(suit: Suit?): Boolean = suit == null || this.suit == suit

    fun matchesSuit(otherCard: Card): Boolean = this.suit == otherCard.suit

    fun matchesRank(otherCard: Card): Boolean = this.rank == otherCard.rank

    fun matchesBoth(otherCard: Card): Boolean = matchesSuit(otherCard) && matchesRank(otherCard)
}
