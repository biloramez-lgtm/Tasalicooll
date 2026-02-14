package com.tasalicool.game.model

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
        fun fromDisplayName(name: String): Rank {
            return values().find { it.displayName == name } ?: ACE
        }

        fun fromString(value: String): Rank {
            return when (value.uppercase()) {
                "2" -> TWO
                "3" -> THREE
                "4" -> FOUR
                "5" -> FIVE
                "6" -> SIX
                "7" -> SEVEN
                "8" -> EIGHT
                "9" -> NINE
                "10", "T" -> TEN
                "J", "JACK" -> JACK
                "Q", "QUEEN" -> QUEEN
                "K", "KING" -> KING
                "A", "ACE" -> ACE
                else -> ACE
            }
        }

        fun fromValue(value: Int): Rank {
            return values().find { it.value == value } ?: ACE
        }
    }

    fun getPoints(): Int = when (this) {
        ACE -> 4
        KING -> 3
        QUEEN -> 2
        JACK -> 1
        TEN -> 10
        else -> 0
    }

    fun isFaceCard(): Boolean = this == JACK || this == QUEEN || this == KING || this == ACE

    fun isNumberCard(): Boolean = !isFaceCard()

    fun isHighCard(): Boolean = this.value >= 10

    fun isLowCard(): Boolean = this.value <= 5

    fun getSymbol(): String = when (this) {
        TEN -> "10"
        JACK -> "J"
        QUEEN -> "Q"
        KING -> "K"
        ACE -> "A"
        else -> this.displayName
    }

    fun getFullName(): String = when (this) {
        TWO -> "Two"
        THREE -> "Three"
        FOUR -> "Four"
        FIVE -> "Five"
        SIX -> "Six"
        SEVEN -> "Seven"
        EIGHT -> "Eight"
        NINE -> "Nine"
        TEN -> "Ten"
        JACK -> "Jack"
        QUEEN -> "Queen"
        KING -> "King"
        ACE -> "Ace"
    }
}
