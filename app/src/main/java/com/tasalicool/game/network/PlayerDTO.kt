package com.tasalicool.game.network

import com.tasalicool.game.model.Player
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val id: Int,
    val name: String,
    val position: Int,
    val teamId: Int,
    val isCurrentTurn: Boolean,
    val isDealer: Boolean,
    val hand: List<CardDTO>,
    val bid: Int,
    val tricksWon: Int,
    val score: Int
) {
    companion object {
        fun fromPlayer(
            player: Player,
            position: Int,
            teamId: Int,
            isCurrentTurn: Boolean,
            isDealer: Boolean
        ): PlayerDTO {
            return PlayerDTO(
                id = player.id,
                name = player.name,
                position = position,
                teamId = teamId,
                isCurrentTurn = isCurrentTurn,
                isDealer = isDealer,
                hand = player.hand.map { CardDTO.fromCard(it) },
                bid = player.bid,
                tricksWon = player.tricksWon,
                score = player.score
            )
        }
    }
}
