package com.tasalicool.game.network

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * NetworkCommand - أوامر الشبكة
 * 
 * جميع رسائل الشبكة توارث هذا الـ sealed class
 */
sealed class NetworkCommand {
    abstract fun toString(): String
    
    // ==================== CONNECTION ====================
    
    /**
     * لاعب انضم للعبة
     */
    @Serializable
    data class PlayerJoined(
        val playerName: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "PLAYER_JOINED|$playerName|$timestamp"
    }
    
    /**
     * لاعب غادر اللعبة
     */
    @Serializable
    data class PlayerLeft(
        val playerId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "PLAYER_LEFT|$playerId|$timestamp"
    }
    
    /**
     * رسالة ping (للتحقق من الاتصال)
     */
    @Serializable
    data class Ping(
        val id: String = "ping_${System.currentTimeMillis()}"
    ) : NetworkCommand() {
        override fun toString(): String = "PING|$id"
    }
    
    /**
     * رسالة pong (الرد على ping)
     */
    @Serializable
    data class Pong(
        val id: String
    ) : NetworkCommand() {
        override fun toString(): String = "PONG|$id"
    }
    
    // ==================== GAME STATE ====================
    
    /**
     * بدء لعبة جديدة
     */
    @Serializable
    data class GameStarted(
        val gameId: String,
        val round: Int,
        val dealerIndex: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "GAME_STARTED|$gameId|$round|$dealerIndex|$timestamp"
    }
    
    /**
     * وضع بدية
     */
    @Serializable
    data class BidPlaced(
        val playerId: String,
        val bid: Int,
        val gameId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "BID_PLACED|$playerId|$bid|$gameId|$timestamp"
    }
    
    /**
     * لعب ورقة
     */
    @Serializable
    data class CardPlayed(
        val playerId: String,
        val cardRank: String,
        val cardSuit: String,
        val gameId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "CARD_PLAYED|$playerId|$cardRank|$cardSuit|$gameId|$timestamp"
    }
    
    /**
     * اكتملت خدعة
     */
    @Serializable
    data class TrickComplete(
        val winnerId: String,
        val trickNumber: Int,
        val gameId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "TRICK_COMPLETE|$winnerId|$trickNumber|$gameId|$timestamp"
    }
    
    /**
     * اكتملت جولة
     */
    @Serializable
    data class RoundComplete(
        val roundNumber: Int,
        val team1Score: Int,
        val team2Score: Int,
        val gameId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "ROUND_COMPLETE|$roundNumber|$team1Score|$team2Score|$gameId|$timestamp"
    }
    
    /**
     * انتهت اللعبة
     */
    @Serializable
    data class GameEnded(
        val winningTeamId: Int,
        val finalScore1: Int,
        val finalScore2: Int,
        val gameId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "GAME_ENDED|$winningTeamId|$finalScore1|$finalScore2|$gameId|$timestamp"
    }
    
    // ==================== CHAT ====================
    
    /**
     * رسالة دردشة
     */
    @Serializable
    data class ChatMessage(
        val playerId: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : NetworkCommand() {
        override fun toString(): String = "CHAT|$playerId|$message|$timestamp"
    }
    
    // ==================== UTILS ====================
    
    companion object {
        fun fromString(message: String): NetworkCommand {
            return try {
                val parts = message.split("|", limit = 5)
                when (parts.getOrNull(0)) {
                    "PLAYER_JOINED" -> PlayerJoined(parts[1], parts.getOrNull(2)?.toLongOrNull() ?: System.currentTimeMillis())
                    "PLAYER_LEFT" -> PlayerLeft(parts[1], parts.getOrNull(2)?.toLongOrNull() ?: System.currentTimeMillis())
                    "PING" -> Ping(parts.getOrNull(1) ?: "ping")
                    "PONG" -> Pong(parts[1])
                    "GAME_STARTED" -> GameStarted(parts[1], parts[2].toInt(), parts[3].toInt(), parts.getOrNull(4)?.toLongOrNull() ?: System.currentTimeMillis())
                    "BID_PLACED" -> BidPlaced(parts[1], parts[2].toInt(), parts[3], parts.getOrNull(4)?.toLongOrNull() ?: System.currentTimeMillis())
                    "CARD_PLAYED" -> CardPlayed(parts[1], parts[2], parts[3], parts[4], parts.getOrNull(5)?.toLongOrNull() ?: System.currentTimeMillis())
                    "TRICK_COMPLETE" -> TrickComplete(parts[1], parts[2].toInt(), parts[3], parts.getOrNull(4)?.toLongOrNull() ?: System.currentTimeMillis())
                    "ROUND_COMPLETE" -> RoundComplete(parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), parts[4], parts.getOrNull(5)?.toLongOrNull() ?: System.currentTimeMillis())
                    "GAME_ENDED" -> GameEnded(parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), parts[4], parts.getOrNull(5)?.toLongOrNull() ?: System.currentTimeMillis())
                    "CHAT" -> ChatMessage(parts[1], parts.getOrNull(2) ?: "", parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis())
                    else -> PlayerJoined("Unknown")
                }
            } catch (e: Exception) {
                PlayerJoined("Error parsing: ${e.message}")
            }
        }
    }
}
