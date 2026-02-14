package com.tasalicool.game.viewmodel

enum class ConnectionStatus {

    IDLE,

    /* Host */
    CREATING_ROOM,
    WAITING_FOR_PLAYERS,

    /* Client */
    JOINING_ROOM,

    /* Active */
    CONNECTED,
    SYNCING,

    /* Failure */
    ERROR,
    DISCONNECTED
}
