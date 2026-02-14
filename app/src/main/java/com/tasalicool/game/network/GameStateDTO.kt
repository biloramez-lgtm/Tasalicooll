package com.tasalicool.game.network.dto

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * GameStateDTO
 * Network Snapshot Only
 * Data Transfer Object - No Game Logic
 */
@Serializable
data class GameStateDTO(

    // ===== GAME IDENTIFICATION =====
    val gameId: String,
    val gameVersion: Int = 1,

    // ===== ROUND STATE =====
    val currentRound: Int,
    val currentTrick: Int,
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

    // ===== BASIC VALIDATION ONLY =====

    fun isValid(): Boolean {
        return gameId.isNotEmpty() &&
                currentRound >= 0 &&
                currentTrick in 0..13 &&
                dealerIndex in 0..3 &&
                currentPlayerIndex in 0..3 &&
                players.size == 4 &&
                team1Score >= 0 &&
                team2Score >= 0
    }

    override fun toString(): String {
        return "GameStateDTO(gameId=$gameId, round=$currentRound, trick=$currentTrick)"
    }

    companion object {

        fun fromGame(game: com.tasalicool.game.model.Game): GameStateDTO {
            return GameStateDTO(
                gameId = game.id,
                currentRound = game.currentRound,
                currentTrick = game.currentTrick,
                gamePhase = game.gamePhase.name,
                biddingPhase = game.biddingPhase.name,
                dealerIndex = game.dealerIndex,
                currentPlayerIndex = game.currentPlayerIndex,
                players = game.players.mapIndexed { index, player ->
                    PlayerDTO.fromPlayer(
                        player = player,
                        position = index,
                        teamId = if (index in 0..1) 1 else 2,
                        isCurrentTurn = game.currentPlayerIndex == index,
                        isDealer = game.dealerIndex == index
                    )
                },
                team1Score = game.team1.score,
                team2Score = game.team2.score,
                team1RoundScore = game.team1.player1.score + game.team1.player2.score,
                team2RoundScore = game.team2.player1.score + game.team2.player2.score,
                currentTrickCards = game.getCurrentTrick()?.cards
                    ?.mapValues { CardDTO.fromCard(it.value) } ?: emptyMap(),
                completedTricks = game.tricks.size,
                tricks = game.tricks.map { TrickDTO.fromTrick(it) },
                bids = game.players.associate { it.id to it.bid },
                isGameOver = game.isGameOver,
                winningTeamId = game.winningTeamId,
                lastUpdateTime = System.currentTimeMillis()
            )
        }

        fun empty(): GameStateDTO {
            return GameStateDTO(
                gameId = "",
                currentRound = 0,
                currentTrick = 0,
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
}
