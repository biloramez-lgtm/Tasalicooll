package com.tasalicool.game.network

import java.io.Serializable

/**
 * NetworkPlayer - لاعب متصل بالشبكة
 */
data class NetworkPlayer(
    val id: String,                     // معرف اللاعب
    val name: String,                   // اسم اللاعب
    val address: String,                // IP
    val status: PlayerStatus = PlayerStatus.CONNECTED,
    val joinTime: Long = System.currentTimeMillis(),
    val lastActivityTime: Long = System.currentTimeMillis(),
    val isHost: Boolean = false,

    // Game data
    val currentScore: Int = 0,
    val roundScore: Int = 0,
    val bid: Int = 0,
    val tricksWon: Int = 0,
    val isReady: Boolean = false

) : Serializable {

    // ==================== STATE CHECKS ====================

    fun isConnected(): Boolean =
        status != PlayerStatus.DISCONNECTED

    fun isActive(): Boolean =
        System.currentTimeMillis() - lastActivityTime < 30_000

    fun hasBidded(): Boolean =
        bid > 0

    fun metBid(): Boolean =
        tricksWon >= bid

    fun isReadyToPlay(): Boolean =
        isConnected() && isActive() && isReady

    // ==================== DISPLAY ====================

    fun getStatusBadge(): String = status.getEmoji()

    fun getTricksInfo(): String =
        if (bid > 0) "$tricksWon/$bid" else "-"

    fun getPerformance(): String =
        when {
            bid == 0 -> "Not Bid"
            tricksWon >= bid -> "✓ Met"
            else -> "✗ Failed"
        }

    // ==================== TIME ====================

    fun connectionDurationMs(): Long =
        System.currentTimeMillis() - joinTime

    fun isInTimeout(): Boolean =
        System.currentTimeMillis() - lastActivityTime > 60_000

    // ==================== IMMUTABLE UPDATES ====================

    fun updateStatus(newStatus: PlayerStatus): NetworkPlayer =
        copy(status = newStatus, lastActivityTime = System.currentTimeMillis())

    fun updateBid(newBid: Int): NetworkPlayer =
        copy(bid = newBid, lastActivityTime = System.currentTimeMillis())

    fun updateTricks(tricks: Int): NetworkPlayer =
        copy(tricksWon = tricks, lastActivityTime = System.currentTimeMillis())

    fun updateScore(score: Int): NetworkPlayer =
        copy(currentScore = score, lastActivityTime = System.currentTimeMillis())

    fun setReady(ready: Boolean): NetworkPlayer =
        copy(isReady = ready, lastActivityTime = System.currentTimeMillis())

    fun resetForNewRound(): NetworkPlayer =
        copy(
            bid = 0,
            tricksWon = 0,
            roundScore = 0,
            status = PlayerStatus.WAITING
        )

    // ==================== VALIDATION ====================

    fun isValid(): Boolean =
        id.isNotBlank() &&
        name.isNotBlank() &&
        address.isNotBlank() &&
        bid in 0..13 &&
        tricksWon in 0..13

    override fun toString(): String =
        "$name ($currentScore pts) - ${status.name}"
}

/**
 * PlayerStatus - حالة اللاعب
 */
enum class PlayerStatus {
    CONNECTED,
    BIDDING,
    PLAYING,
    WAITING,
    DISCONNECTED;

    fun isActive(): Boolean =
        this != DISCONNECTED

    fun getEmoji(): String =
        when (this) {
            CONNECTED -> "🟢"
            BIDDING -> "🟡"
            PLAYING -> "🔵"
            WAITING -> "⚪"
            DISCONNECTED -> "🔴"
        }
}

/**
 * موقع اللاعب على الطاولة
 */
enum class TablePosition {
    HOST,
    TOP,
    LEFT,
    RIGHT,
    BOTTOM,
    GUEST
}

/**
 * Extensions
 */
fun NetworkPlayer.getTablePosition(): TablePosition =
    if (isHost) TablePosition.HOST else TablePosition.GUEST

fun NetworkPlayer.getStarRating(): String =
    when {
        tricksWon == 0 -> "☆☆☆☆☆"
        tricksWon < bid / 2 -> "★☆☆☆☆"
        tricksWon < bid -> "★★☆☆☆"
        tricksWon == bid -> "★★★☆☆"
        tricksWon == bid + 1 -> "★★★★☆"
        else -> "★★★★★"
    }
