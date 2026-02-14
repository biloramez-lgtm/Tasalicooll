package com.tasalicool.game.viewmodel

data class MultiplayerState(

    /* -------- General -------- */
    val enabled: Boolean = false,
    val isHost: Boolean = false,

    /* -------- Players -------- */
    val connectedPlayers: Int = 1,
    val maxPlayers: Int = 2,

    /* -------- Room -------- */
    val roomCode: String? = null,

    /* -------- Connection -------- */
    val status: ConnectionStatus = ConnectionStatus.IDLE,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,

    /* -------- Error -------- */
    val errorMessage: String? = null
)
