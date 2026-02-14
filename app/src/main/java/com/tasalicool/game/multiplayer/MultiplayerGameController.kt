package com.tasalicool.game.multiplayer

import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.network.MultiplayerManager
import com.tasalicool.game.network.NetworkCommand
import com.tasalicool.game.network.ConnectionState
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MultiplayerGameController(
    private val engine: GameEngine,
    private val multiplayerManager: MultiplayerManager
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        observeCommands()
    }

    private fun observeCommands() {
        scope.launch {
            multiplayerManager.commands.collect { cmd ->

                when (multiplayerManager.connectionState.value) {

                    ConnectionState.HOSTING -> handleAsHost(cmd)

                    ConnectionState.CONNECTED -> handleAsClient(cmd)

                    else -> Unit
                }
            }
        }
    }

    // ================= HOST =================

    private fun handleAsHost(cmd: NetworkCommand) {

        when (cmd) {

            is NetworkCommand.BidPlaced -> {
                val playerIndex = findPlayerIndex(cmd.playerId)
                if (playerIndex != -1) {
                    engine.placeBid(playerIndex, cmd.bidValue)
                    sendSync()
                }
            }

            is NetworkCommand.CardPlayed -> {
                val playerIndex = findPlayerIndex(cmd.playerId)
                if (playerIndex != -1) {
                    val card = engine.gameState.value
                        ?.players?.get(playerIndex)
                        ?.hand?.find {
                            it.rank.name == cmd.cardRank &&
                            it.suit.name == cmd.cardSuit
                        }

                    if (card != null) {
                        engine.playCard(playerIndex, card)
                        sendSync()
                    }
                }
            }

            else -> Unit
        }
    }

    private fun sendSync() {
        val state = engine.gameState.value ?: return
        val serialized = Json.encodeToString(state)

        multiplayerManager.broadcast(
            NetworkCommand.SyncState(
                gameId = state.id,
                serializedGameState = serialized
            )
        )
    }

    // ================= CLIENT =================

    private fun handleAsClient(cmd: NetworkCommand) {

        when (cmd) {
            is NetworkCommand.SyncState -> {
                engine.onNetworkCommand(cmd)
            }
            else -> Unit
        }
    }

    // ================= UTIL =================

    private fun findPlayerIndex(playerId: String): Int {
        return engine.gameState.value
            ?.players
            ?.indexOfFirst { it.id.toString() == playerId }
            ?: -1
    }
}
