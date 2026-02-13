package com.tarneeb.game.utils

object GameConstants {
    const val TOTAL_PLAYERS = 4
    const val CARDS_PER_PLAYER = 13
    const val MIN_BID = 2
    const val MAX_BID = 13
    const val WINNING_SCORE = 41
    const val MIN_TOTAL_BIDS_DEFAULT = 11
    const val MIN_TOTAL_BIDS_30_39 = 12
    const val MIN_TOTAL_BIDS_40_49 = 13
    const val MIN_TOTAL_BIDS_50_PLUS = 14

    const val MIN_BID_BELOW_30 = 2
    const val MIN_BID_30_39 = 3
    const val MIN_BID_40_49 = 4
    const val MIN_BID_50_PLUS = 5

    // Scoring tables
    val SCORING_TABLE_BELOW_30 = mapOf(
        2 to 2,
        3 to 3,
        4 to 4,
        5 to 10,
        6 to 12,
        7 to 14,
        8 to 16,
        9 to 27,
        10 to 40,
        11 to 40,
        12 to 40,
        13 to 40
    )

    val SCORING_TABLE_30_PLUS = mapOf(
        2 to 2,
        3 to 3,
        4 to 4,
        5 to 5,
        6 to 6,
        7 to 14,
        8 to 16,
        9 to 27,
        10 to 40,
        11 to 40,
        12 to 40,
        13 to 40
    )

    // Player positions
    const val SOUTH = 0
    const val WEST = 1
    const val NORTH = 2
    const val EAST = 3

    // Position names
    val POSITION_NAMES = mapOf(
        SOUTH to "South",
        WEST to "West",
        NORTH to "North",
        EAST to "East"
    )
}

object CardUtils {
    fun getCardImageUrl(suit: String, rank: String): String {
        return "https://deckofcardsapi.com/static/img/${rank}${suit}.png"
    }

    fun suitToEmoji(suitName: String): String = when (suitName.lowercase()) {
        "hearts" -> "♥"
        "diamonds" -> "♦"
        "clubs" -> "♣"
        "spades" -> "♠"
        else -> "♥"
    }
}

fun String.toTitleCase(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun Int.getOrdinalSuffix(): String = when {
    this % 100 in 11..13 -> "${this}th"
    this % 10 == 1 -> "${this}st"
    this % 10 == 2 -> "${this}nd"
    this % 10 == 3 -> "${this}rd"
    else -> "${this}th"
}

inline fun <T> List<T>.getElementAtOrNull(index: Int): T? {
    return if (index in indices) this[index] else null
}
