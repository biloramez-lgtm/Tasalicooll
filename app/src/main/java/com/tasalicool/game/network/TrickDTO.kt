package com.tasalicool.game.network

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * TrickDTO
 * Network Safe Data Transfer Object
 * يحتوي بيانات فقط بدون منطق لعب
 */
@Serializable
data class TrickDTO(

    val trickNumber: Int,
    val cards: Map<Int, CardDTO> = emptyMap(),   // playerId -> card
    val playOrder: List<Int> = emptyList(),
    val winnerId: Int = -1,
    val trickSuit: String? = null,
    val heartsBroken: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()

) : JavaSerializable {

    // ================= VALIDATION =================

    fun isValid(): Boolean {
        return trickNumber in 1..13 &&
               cards.size <= 4 &&
               playOrder.size == cards.size &&
               playOrder.all { it in 0..3 } &&
               cards.all { (_, card) -> card.isValid() } &&
               (winnerId == -1 || winnerId in 0..3)
    }

    fun isComplete(): Boolean {
        return cards.size == 4
    }

    fun isFinished(): Boolean {
        return isComplete() && winnerId >= 0
    }

    fun getCard(playerId: Int): CardDTO? {
        return cards[playerId]
    }

    override fun toString(): String {
        return "Trick#$trickNumber cards=${cards.size}/4 winner=$winnerId"
    }

    // ================= FACTORY =================

    companion object {

        fun fromTrick(trick: com.tasalicool.game.model.Trick): TrickDTO {
            return TrickDTO(
                trickNumber = trick.trickNumber,
                cards = trick.cards.mapValues { CardDTO.fromCard(it.value) },
                playOrder = trick.playOrder.toList(),
                winnerId = trick.winnerId,
                trickSuit = trick.trickSuit?.getSymbol(),
                heartsBroken = trick.heartsBroken,
                timestamp = System.currentTimeMillis()
            )
        }

        fun toTrick(dto: TrickDTO): com.tasalicool.game.model.Trick {
            val trick = com.tasalicool.game.model.Trick(dto.trickNumber)

            dto.cards.forEach { (playerId, cardDto) ->
                trick.addCard(playerId, CardDTO.toCard(cardDto))
            }

            trick.winnerId = dto.winnerId
            trick.heartsBroken = dto.heartsBroken

            return trick
        }

        fun empty(trickNumber: Int): TrickDTO {
            return TrickDTO(trickNumber = trickNumber)
        }
    }
}
