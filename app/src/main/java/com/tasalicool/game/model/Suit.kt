package com.tasalicool.game.model

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

        fun fromString(value: String): Suit = when (value.uppercase()) {
            "HEARTS", "H", "♥" -> HEARTS
            "DIAMONDS", "D", "♦" -> DIAMONDS
            "CLUBS", "C", "♣" -> CLUBS
            "SPADES", "S", "♠" -> SPADES
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

    fun getColor(): String = when (this) {
        HEARTS, DIAMONDS -> "RED"
        CLUBS, SPADES -> "BLACK"
    }

    fun isRed(): Boolean = this == HEARTS || this == DIAMONDS

    fun isBlack(): Boolean = this == CLUBS || this == SPADES

    fun getShortName(): String = when (this) {
        HEARTS -> "H"
        DIAMONDS -> "D"
        CLUBS -> "C"
        SPADES -> "S"
    }
}
