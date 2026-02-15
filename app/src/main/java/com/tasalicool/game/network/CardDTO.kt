package com.tasalicool.game.network

import com.tasalicool.game.model.Card
import kotlinx.serialization.Serializable

@Serializable
data class CardDTO(
    val suit: String,
    val rank: String
) {
    companion object {
        fun fromCard(card: Card): CardDTO {
            return CardDTO(
                suit = card.suit.name,
                rank = card.rank.name
            )
        }
    }
}
