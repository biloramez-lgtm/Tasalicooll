package com.tasalicool.game.network

/**
 * NetworkEvent - أحداث الشبكة
 */
sealed class NetworkEvent {

    /**
     * لاعب جديد اتصل
     */
    data class PlayerConnected(
        val player: NetworkPlayer
    ) : NetworkEvent()

    /**
     * لاعب انقطع
     */
    data class PlayerDisconnected(
        val playerId: String
    ) : NetworkEvent()

    /**
     * رسالة واردة من لاعب أو السيرفر
     */
    data class MessageReceived(
        val playerId: String,
        val command: NetworkCommand
    ) : NetworkEvent()

    /**
     * انقطع الاتصال مع السيرفر
     */
    object ServerDisconnected : NetworkEvent()

    /**
     * خطأ في الاتصال
     */
    data class ConnectionError(
        val message: String
    ) : NetworkEvent()
}

/**
 * NetworkPlayer - لاعب متصل بالشبكة
 */
data class NetworkPlayer(
    val id: String,
    val name: String,
    val address: String,
    val status: PlayerStatus
)

/**
 * PlayerStatus - حالة اللاعب داخل اللعبة
 */
enum class PlayerStatus {
    CONNECTED,     // متصل فقط
    BIDDING,       // في مرحلة البدية
    PLAYING,       // يلعب ورقة
    WAITING,       // ينتظر دوره
    DISCONNECTED   // انقطع
}
