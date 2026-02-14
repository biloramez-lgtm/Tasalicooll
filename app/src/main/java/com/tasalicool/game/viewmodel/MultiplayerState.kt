package com.tasalicool.game.viewmodel

data class MultiplayerState(
    val enabled: Boolean = false,
    val isHost: Boolean = false,
    val connectedPlayers: Int = 1,
    val roomCode: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE
)

enum class ConnectionStatus {
    IDLE,
    CREATING_ROOM,
    WAITING_FOR_PLAYERS,
    CONNECTED,
    ERROR
}
