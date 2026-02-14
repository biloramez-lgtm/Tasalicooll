package com.tasalicool.game.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * NetworkCommand
 *
 * Final Production Version
 * Local WiFi / Hotspot Multiplayer
 * Host authoritative architecture
 */

@Serializable
sealed class NetworkCommand {

    // ==================== CONNECTION ====================

    @Serializable
    @SerialName("PlayerJoined")
    data class PlayerJoined(
        val playerId: String,
        val playerName: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    @Serializable
    @SerialName("PlayerLeft")
    data class PlayerLeft(
        val playerId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    @Serializable
    @SerialName("Ping")
    data class Ping(
        val pingId: String = "ping_${System.currentTimeMillis()}",
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    @Serializable
    @SerialName("Pong")
    data class Pong(
        val pingId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    // ==================== GAME FLOW ====================

    @Serializable
    @SerialName("GameStarted")
    data class GameStarted(
        val gameId: String,
        val dealerIndex: Int,
        val roundNumber: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    @Serializable
    @SerialName("TurnChanged")
    data class TurnChanged(
        val gameId: String,
        val currentPlayerId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    @Serializable
    @SerialName("SyncState")
    data class SyncState(
        val gameId: String,
        val serializedGameState: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    // ==================== BIDDING ====================

    @Serializable
    @SerialName("BidPlaced")
    data class BidPlaced(
        val gameId: String,
        val playerId: String,
        val bidValue: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    // ==================== PLAY ====================

    @Serializable
    @SerialName("CardPlayed")
    data class CardPlayed(
        val gameId: String,
        val playerId: String,
        val cardRank: String,
        val cardSuit: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    @Serializable
    @SerialName("TrickCompleted")
    data class TrickCompleted(
        val gameId: String,
        val trickNumber: Int,
        val winnerPlayerId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    // ==================== ROUND / GAME END ====================

    @Serializable
    @SerialName("RoundCompleted")
    data class RoundCompleted(
        val gameId: String,
        val roundNumber: Int,
        val team1Score: Int,
        val team2Score: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    @Serializable
    @SerialName("GameEnded")
    data class GameEnded(
        val gameId: String,
        val winningTeamId: Int,
        val finalScoreTeam1: Int,
        val finalScoreTeam2: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()

    // ==================== CHAT ====================

    @Serializable
    @SerialName("ChatMessage")
    data class ChatMessage(
        val playerId: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand()
}
