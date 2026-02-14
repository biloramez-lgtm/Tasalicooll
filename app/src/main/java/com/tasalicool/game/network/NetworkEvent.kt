package com.tasalicool.game.network

/**
 * NetworkEvent - أحداث الشبكة
 */
sealed class NetworkEvent {
    data class PlayerConnected(val player: NetworkPlayer) : NetworkEvent()
    data class PlayerDisconnected(val playerId: String) : NetworkEvent()
    data class MessageReceived(val playerId: String, val command: NetworkCommand) : NetworkEvent()
    object ServerDisconnected : NetworkEvent()
    data class ConnectionError(val message: String) : NetworkEvent()
}

/**
 * لاعب متصل بالشبكة
 */
data class NetworkPlayer(
    val id: String,
    val name: String,
    val address: String,
    val status: PlayerStatus
)

/**
 * حالة اللاعب
 */
enum class PlayerStatus {
    CONNECTED,
    BIDDING,
    PLAYING,
    WAITING,
    DISCONNECTED
}

/**
 * حالة الاتصال
 */
enum class ConnectionState {
    DISCONNECTED,      // غير متصل
    CONNECTING,        // جاري الاتصال
    CONNECTED,         // متصل كعميل
    HOSTING,           // استضافة كسيرفر
    ERROR              // خطأ في الاتصال
}
