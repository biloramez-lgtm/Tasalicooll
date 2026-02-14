package com.tasalicool.game.network

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * PlayerDTO - Data Transfer Object للاعب
 * مخصص لنقل بيانات اللاعب عبر الشبكة فقط
 * بدون منطق runtime أو clock-dependent logic
 */
@Serializable
data class PlayerDTO(

    // ==================== IDENTIFICATION ====================
    val id: Int,                         // معرف اللاعب داخل اللعبة
    val name: String,                    // اسم اللاعب
    val sessionId: String = "",          // معرف الجلسة الشبكية

    // ==================== TEAM INFO ====================
    val teamId: Int,                     // 1 أو 2
    val position: Int,                   // 0 - 3

    // ==================== SCORES ====================
    val score: Int = 0,
    val roundScore: Int = 0,
    val previousScore: Int = 0,

    // ==================== GAME STATE ====================
    val bid: Int = 0,                    // 0 - 13
    val tricksWon: Int = 0,
    val tricksNeeded: Int = 0,

    // ==================== STATUS ====================
    val status: String = "WAITING",      // WAITING, BIDDING, PLAYING, DISCONNECTED, AI
    val isCurrentPlayer: Boolean = false,
    val isDealer: Boolean = false,
    val isConnected: Boolean = true,
    val isHost: Boolean = false,
    val isReady: Boolean = false,

    // ==================== HAND ====================
    val handSize: Int = 0,
    val hand: List<CardDTO> = emptyList(),

    // ==================== TIMESTAMPS ====================
    val joinTime: Long = 0,
    val lastActivityTime: Long = 0,
    val lastBidTime: Long = 0,
    val lastCardPlayTime: Long = 0

) : JavaSerializable {

    // ==================== VALIDATION ====================

    fun isValid(): Boolean {
        return id >= 0 &&
                name.isNotBlank() &&
                teamId in 1..2 &&
                position in 0..3 &&
                score >= 0 &&
                bid in 0..13 &&
                tricksWon in 0..13 &&
                handSize in 0..13 &&
                status.isNotBlank()
    }

    // ==================== STATUS HELPERS ====================

    fun isBidding(): Boolean = status == "BIDDING"

    fun isPlaying(): Boolean = status == "PLAYING"

    fun isWaiting(): Boolean = status == "WAITING"

    fun isDisconnected(): Boolean = status == "DISCONNECTED"

    fun metBid(): Boolean = bid > 0 && tricksWon >= bid

    fun failedBid(): Boolean = bid > 0 && tricksWon < bid

    fun hasBidded(): Boolean = bid > 0

    // ==================== COMPUTED HELPERS ====================

    fun getRemainingTricks(): Int =
        (bid - tricksWon).coerceAtLeast(0)

    fun getPerformancePercentage(): Float =
        if (bid == 0) 0f else (tricksWon.toFloat() / bid) * 100f

    fun getPointsAfterBid(): Int =
        if (metBid()) score else score - bid

    // ==================== DISPLAY ====================

    override fun toString(): String {
        return "$name (ID: $id) - Score: $score - Team: $teamId"
    }

    fun getStatusBadge(): String {
        return when (status) {
            "CONNECTED" -> "🟢"
            "BIDDING" -> "🟡"
            "PLAYING" -> "🔵"
            "WAITING" -> "⚪"
            "DISCONNECTED" -> "🔴"
            "AI" -> "🤖"
            else -> "❓"
        }
    }

    fun getPerformanceBadge(): String {
        return when {
            bid == 0 -> "⏳"
            metBid() -> "✅"
            failedBid() -> "❌"
            else -> "⚠️"
        }
    }

    // ==================== COMPARISON ====================

    fun isBetterThan(other: PlayerDTO): Boolean {
        return when {
            this.score > other.score -> true
            this.score == other.score && this.tricksWon > other.tricksWon -> true
            else -> false
        }
    }

    fun isSame(other: PlayerDTO): Boolean {
        return this.id == other.id
    }

    // ==================== FACTORY ====================

    companion object {

        fun empty(): PlayerDTO {
            return PlayerDTO(
                id = -1,
                name = "Unknown",
                teamId = 1,
                position = 0
            )
        }
    }
}
