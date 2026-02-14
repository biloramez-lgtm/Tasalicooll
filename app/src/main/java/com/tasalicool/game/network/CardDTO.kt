package com.tasalicool.game.network

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * CardDTO
 * Network Safe Data Transfer Object
 * يحتوي بيانات فقط — بدون منطق لعب
 */
@Serializable
data class CardDTO(

    val rank: String,     // 2-10, J, Q, K, A
    val suit: String,     // H, D, C, S
    val rankValue: Int    // 2 - 14

) : JavaSerializable {

    // ================= VALIDATION =================

    fun isValid(): Boolean {
        val validRanks = listOf(
            "2","3","4","5","6","7","8","9","10",
            "J","Q","K","A"
        )

        val validSuits = listOf("H","D","C","S")

        return rank in validRanks &&
               suit in validSuits &&
               rankValue in 2..14
    }

    override fun toString(): String {
        return "$rank$suit"
    }

    // ================= FACTORY =================

    companion object {

        fun fromCard(card: com.tasalicool.game.model.Card): CardDTO {
            return CardDTO(
                rank = card.rank.displayName,
                suit = normalizeSuit(card.suit.getSymbol()),
                rankValue = card.rank.value
            )
        }

        fun toCard(dto: CardDTO): com.tasalicool.game.model.Card {
            val suit = com.tasalicool.game.model.Suit.fromString(dto.suit)
            val rank = com.tasalicool.game.model.Rank.fromString(dto.rank)
            return com.tasalicool.game.model.Card(
                suit = suit,
                rank = rank
            )
        }

        fun empty(): CardDTO {
            return CardDTO(
                rank = "2",
                suit = "H",
                rankValue = 2
            )
        }

        private fun normalizeSuit(symbol: String): String {
            return when (symbol) {
                "♥","H" -> "H"
                "♦","D" -> "D"
                "♣","C" -> "C"
                "♠","S" -> "S"
                else -> "H"
            }
        }
    }
}
