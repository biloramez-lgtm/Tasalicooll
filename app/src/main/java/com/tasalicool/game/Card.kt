package com.tarneeb.game.model

enum class Suit {
    HEARTS,
    DIAMONDS,
    CLUBS,
    SPADES;

    companion object {
        fun fromSymbol(symbol: String): Suit = when (symbol) {
            "♥" -> HEARTS
            "♦" -> DIAMONDS
            "♣" -> CLUBS
            "♠" -> SPADES
            else -> HEARTS
        }
    }

    fun getSymbol(): String = when (this) {
        HEARTS -> "♥"
        DIAMONDS -> "♦"
        CLUBS -> "♣"
        SPADES -> "♠"
    }

    fun getDisplayName(): String = when (this) {
        HEARTS -> "Hearts"
        DIAMONDS -> "Diamonds"
        CLUBS -> "Clubs"
        SPADES -> "Spades"
    }
}

enum class Rank(val displayName: String, val value: Int) {
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 11),
    QUEEN("Q", 12),
    KING("K", 13),
    ACE("A", 14);

    companion object {
        fun fromDisplayName(name: String): Rank = values().find { it.displayName == name } ?: ACE
    }
}

data class Card(
    val suit: Suit,
    val rank: Rank
) {
    override fun toString(): String = "${rank.displayName}${suit.getSymbol()}"

    fun getImageResId(): Int {
        // This would typically be a drawable resource ID
        return 0
    }

    companion object {
        fun createDeck(): List<Card> {
            return buildList {
                for (suit in Suit.values()) {
                    for (rank in Rank.values()) {
                        if (rank != Rank.TWO) continue // Excluding 2s for standard deck
                        add(Card(suit, rank))
                    }
                }
            }
        }

        fun createFullDeck(): List<Card> {
            return buildList {
                for (suit in Suit.values()) {
                    for (rank in Rank.values()) {
                        add(Card(suit, rank))
                    }
                }
            }
        }
    }
}
