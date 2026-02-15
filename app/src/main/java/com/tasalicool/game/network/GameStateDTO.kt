package com.tasalicool.game.network

import com.tasalicool.game.model.*
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class GameStateDTO(

    // ===== GAME IDENTIFICATION =====
    val gameId: String,
    val gameVersion: Int = 1,

    // ===== ROUND STATE =====
    val round: Int,
    val currentTrickNumber: Int,
    val gamePhase: String,
    val biddingPhase: String,

    // ===== TURN INFO =====
    val dealerIndex: Int,
    val currentPlayerIndex: Int,

    // ===== PLAYERS =====
    val players: List<PlayerDTO>,

    // ===== SCORES =====
    val team1Score: Int,
    val team2Score: Int,
    val team1RoundScore: Int = 0,
    val team2RoundScore: Int = 0,

    // ===== TRICKS =====
    val currentTrickCards: Map<Int, CardDTO> = emptyMap(),
    val completedTricks: Int = 0,
    val tricks: List<TrickDTO> = emptyList(),

    // ===== BIDS =====
    val bids: Map<Int, Int> = emptyMap(),

    // ===== STATUS =====
    val isGameOver: Boolean = false,
    val winningTeamId: Int = -1,
    val isPaused: Boolean = false,

    // ===== TIMESTAMPS =====
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val roundStartTime: Long = System.currentTimeMillis()

) : JavaSerializable {

    companion object {

        fun fromGame(game: Game): GameStateDTO {
            val currentTrick =
                game.tricks.lastOrNull() ?: game.getOrCreateCurrentTrick()

            return GameStateDTO(
                gameId = game.id,
                round = game.round,
                currentTrickNumber = game.currentTrickNumber,
                gamePhase = game.gamePhase.name,
                biddingPhase = game.biddingPhase.name,
                dealerIndex = game.dealerIndex,
                currentPlayerIndex = game.currentPlayerIndex,

                players = game.players.mapIndexed { index, player ->
                    PlayerDTO.fromPlayer(
                        player = player,
                        position = index,
                        teamId = if (index <= 1) game.team1.id else game.team2.id,
                        isCurrentTurn = game.currentPlayerIndex == index,
                        isDealer = game.dealerIndex == index
                    )
                },

                team1Score = game.team1.score,
                team2Score = game.team2.score,
                team1RoundScore = game.team1.players.sumOf { it.score },
                team2RoundScore = game.team2.players.sumOf { it.score },

                currentTrickCards =
                    currentTrick.cards.mapValues { CardDTO.fromCard(it.value) },

                completedTricks = game.tricks.size,
                tricks = game.tricks.map { TrickDTO.fromTrick(it) },

                bids = game.players.associate { it.id to it.bid },

                isGameOver = game.isGameOver,
                winningTeamId = game.winningTeamId
            )
        }

        fun empty() = GameStateDTO(
            gameId = "",
            round = 0,
            currentTrickNumber = 0,
            gamePhase = "DEALING",
            biddingPhase = "WAITING",
            dealerIndex = 0,
            currentPlayerIndex = 0,
            players = emptyList(),
            team1Score = 0,
            team2Score = 0
        )
    }
}
