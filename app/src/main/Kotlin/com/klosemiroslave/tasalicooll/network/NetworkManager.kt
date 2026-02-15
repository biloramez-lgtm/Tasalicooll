package com.klosemiroslave.tasalicooll.network

import com.klosemiroslave.tasalicooll.model.Player

class NetworkManager {

    val connectedPlayers = mutableListOf<Player>()

    fun connectPlayer(player: Player) {
        connectedPlayers.add(player)
    }

    fun isReady(): Boolean = connectedPlayers.size >= 2
}
