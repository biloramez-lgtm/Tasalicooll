package com.tasalicool.game.network

import com.tasalicool.game.model.Trick
import kotlinx.serialization.Serializable

@Serializable
data class TrickDTO(
    val cards: Map<Int, CardDTO>,
    val winningPlayerIndex: Int
) {
    companion object {
        fun fromTrick(trick: Trick): TrickDTO {
            return TrickDTO(
                cards = trick.cards.mapValues { CardDTO.fromCard(it.value) },
                winningPlayerIndex = trick.winningPlayerIndex
            )
        }
    }
}
