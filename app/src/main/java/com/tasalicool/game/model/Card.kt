package com.tasalicool.game.model

import kotlinx.serialization.Serializable

@Serializable
enum class Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES;

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

    fun getColor(): String = when (this) {
        HEARTS, DIAMONDS -> "RED"
        CLUBS, SPADES -> "BLACK"
    }
}

@Serializable
enum class Rank(val displayName: String, val value: Int) {
    TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5),
    SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9),
    TEN("10", 10), JACK("J", 11), QUEEN("Q", 12),
    KING("K", 13), ACE("A", 14);
}

@Serializable
data class Card(val suit: Suit, val rank: Rank) {
    override fun toString(): String = "${rank.displayName}${suit.getSymbol()}"
    
    fun isTrump(): Boolean = suit == Suit.HEARTS
    fun isHighCard(): Boolean = rank.value >= 10

    companion object {
        fun createGameDeck(): List<Card> {
            return buildList {
                for (suit in Suit.values()) {
                    for (rank in Rank.values()) {
                        add(Card(suit, rank))
                    }
                }
            }.shuffled()
        }
    }
}
