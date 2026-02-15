package com.tasalicool.game.multiplayer

import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.network.ConnectionState
import com.tasalicool.game.network.MultiplayerManager
import com.tasalicool.game.network.NetworkCommand
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MultiplayerGameController(
    private val engine: GameEngine,
    private val multiplayerManager: MultiplayerManager
) {

    // 🔥 نشتغل على Main حتى ما يصير مشاكل UI
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // 🔒 حماية من race condition
    private val mutex = Mutex()

    init {
        observeCommands()
    }

    private fun observeCommands() {
        scope.launch {
            multiplayerManager.commands.collect { cmd ->
                mutex.withLock {

                    when (multiplayerManager.connectionState.value) {

                        ConnectionState.HOSTING ->
                            handleAsHost(cmd)

                        ConnectionState.CONNECTED ->
                            handleAsClient(cmd)

                        else -> Unit
                    }
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

                    // البحث في اليد عن بطاقة تطابق الـ rank و suit
                    val card = engine.gameState.value
                        ?.players?.get(playerIndex)
                        ?.hand?.firstOrNull { c ->
                            c.rank.displayName == cmd.cardRank &&
                            c.suit.getSymbol() == cmd.cardSuit
                        }

                    if (card != null) {
                        engine.playCard(playerIndex, card) // النوع صحيح الآن: Card
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

        if (cmd is NetworkCommand.SyncState) {
            engine.onNetworkCommand(cmd)
        }
    }

    // ================= UTIL =================

    private fun findPlayerIndex(playerId: String): Int {
        return engine.gameState.value
            ?.players
            ?.indexOfFirst { it.id.toString() == playerId }
            ?: -1
    }

    fun clear() {
        scope.cancel()
    }
}
