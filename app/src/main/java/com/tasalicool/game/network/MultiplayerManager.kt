package com.tasalicool.game.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

// =====================
// Data Models
// =====================

data class GameMessage(
    val type: String, // "BID", "CARD", "START", "JOIN", "LEAVE", "SYNC"
    val playerId: Int,
    val playerName: String,
    val gameId: String,
    val data: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class GameState(
    val gameId: String,
    val players: List<PlayerData>,
    val currentPhase: String,
    val team1Score: Int,
    val team2Score: Int,
    val roundNumber: Int
)

data class PlayerData(
    val id: Int,
    val name: String,
    val score: Int,
    val bid: Int,
    val tricksWon: Int
)

// =====================
// Multiplayer Manager
// =====================

class MultiplayerManager {

    private val _connectionState =
        MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _receivedMessages =
        MutableStateFlow<GameMessage?>(null)
    val receivedMessages: StateFlow<GameMessage?> = _receivedMessages

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var outputStream: PrintWriter? = null
    private var inputStream: BufferedReader? = null

    private val connectedPlayers = mutableListOf<Socket>()

    // =====================
    // Host (Server)
    // =====================

    fun startServer(port: Int = 5555) {
        thread {
            try {
                serverSocket = ServerSocket(port)
                _connectionState.value = ConnectionState.HOSTING

                while (true) {
                    val socket = serverSocket?.accept() ?: break
                    connectedPlayers.add(socket)
                    thread { handleClient(socket) }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                e.printStackTrace()
            }
        }
    }

    // =====================
    // Client (Join)
    // =====================

    fun joinGame(hostAddress: String, port: Int = 5555) {
        thread {
            try {
                clientSocket = Socket(hostAddress, port)
                outputStream = PrintWriter(
                    OutputStreamWriter(clientSocket!!.getOutputStream()),
                    true
                )
                inputStream = BufferedReader(
                    InputStreamReader(clientSocket!!.getInputStream())
                )

                _connectionState.value = ConnectionState.CONNECTED

                while (true) {
                    val message = inputStream?.readLine() ?: break
                    handleMessage(message)
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                e.printStackTrace()
            }
        }
    }

    // =====================
    // Messaging
    // =====================

    fun sendMessage(message: GameMessage) {
        thread {
            try {
                val serialized = messageToString(message)

                if (clientSocket != null) {
                    outputStream?.println(serialized)
                } else {
                    connectedPlayers.forEach { socket ->
                        try {
                            val writer = PrintWriter(
                                OutputStreamWriter(socket.getOutputStream()),
                                true
                            )
                            writer.println(serialized)
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(
                InputStreamReader(socket.getInputStream())
            )
            while (true) {
                val line = reader.readLine() ?: break
                handleMessage(line)

                connectedPlayers
                    .filter { it != socket }
                    .forEach { other ->
                        try {
                            val writer = PrintWriter(
                                OutputStreamWriter(other.getOutputStream()),
                                true
                            )
                            writer.println(line)
                        } catch (_: Exception) {}
                    }
            }
        } catch (_: Exception) {}
    }

    private fun handleMessage(message: String) {
        val gameMessage = stringToMessage(message)
        _receivedMessages.value = gameMessage
    }

    // =====================
    // Serialization (simple)
    // =====================

    private fun messageToString(message: GameMessage): String {
        return "${message.type}|${message.playerId}|${message.playerName}|${message.gameId}|${message.data}|${message.timestamp}"
    }

    private fun stringToMessage(str: String): GameMessage {
        val parts = str.split("|")
        return GameMessage(
            type = parts.getOrNull(0) ?: "UNKNOWN",
            playerId = parts.getOrNull(1)?.toIntOrNull() ?: -1,
            playerName = parts.getOrNull(2) ?: "",
            gameId = parts.getOrNull(3) ?: "",
            data = parts.getOrNull(4) ?: "",
            timestamp = parts.getOrNull(5)?.toLongOrNull() ?: System.currentTimeMillis()
        )
    }

    // =====================
    // Utils
    // =====================

    fun disconnect() {
        try {
            clientSocket?.close()
            outputStream?.close()
            inputStream?.close()
            connectedPlayers.forEach { it.close() }
            serverSocket?.close()
            _connectionState.value = ConnectionState.DISCONNECTED
        } catch (_: Exception) {}
    }

    fun isConnected(): Boolean {
        return _connectionState.value == ConnectionState.CONNECTED ||
               _connectionState.value == ConnectionState.HOSTING
    }
}

// =====================
// Connection State
// =====================

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    HOSTING,
    ERROR
}

// =====================
// WiFi Direct (placeholder)
// =====================

class WiFiDirectManager {

    fun initializeWiFiDirect() {
        // future implementation
    }

    fun startDiscovery() {
        // future implementation
    }

    fun connectToDevice(deviceAddress: String) {
        // future implementation
    }
}
