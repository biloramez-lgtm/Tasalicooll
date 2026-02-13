package com.tasalicool.game.network

import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

@Serializable
data class GameMessage(
    val type: String, // "BID", "CARD", "START", "JOIN", "LEAVE", "SYNC"
    val playerId: Int,
    val playerName: String,
    val gameId: String,
    val data: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class GameState(
    val gameId: String,
    val players: List<PlayerData>,
    val currentPhase: String,
    val team1Score: Int,
    val team2Score: Int,
    val roundNumber: Int
)

@Serializable
data class PlayerData(
    val id: Int,
    val name: String,
    val score: Int,
    val bid: Int,
    val tricksWon: Int
)

class MultiplayerManager {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _receivedMessages = MutableStateFlow<GameMessage?>(null)
    val receivedMessages: StateFlow<GameMessage?> = _receivedMessages

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var outputStream: PrintWriter? = null
    private var inputStream: BufferedReader? = null

    private val connectedPlayers = mutableListOf<Socket>()

    // Host mode (server)
    fun startServer(port: Int = 5555) {
        thread {
            try {
                serverSocket = ServerSocket(port)
                _connectionState.value = ConnectionState.HOSTING
                
                while (true) {
                    val socket = serverSocket?.accept() ?: break
                    connectedPlayers.add(socket)
                    
                    thread {
                        handleClient(socket)
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                e.printStackTrace()
            }
        }
    }

    // Client mode (join)
    fun joinGame(hostAddress: String, port: Int = 5555) {
        thread {
            try {
                clientSocket = Socket(hostAddress, port)
                outputStream = PrintWriter(
                    OutputStreamWriter(clientSocket?.getOutputStream()),
                    true
                )
                inputStream = BufferedReader(
                    InputStreamReader(clientSocket?.getInputStream())
                )
                _connectionState.value = ConnectionState.CONNECTED
                
                while (true) {
                    val message = inputStream?.readLine() ?: break
                    // Parse and handle message
                    handleMessage(message)
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(message: GameMessage) {
        thread {
            try {
                if (clientSocket != null) {
                    // Send as client
                    outputStream?.println(messageToJson(message))
                } else {
                    // Send to all connected clients (as server)
                    connectedPlayers.forEach { socket ->
                        try {
                            val writer = PrintWriter(
                                OutputStreamWriter(socket.getOutputStream()),
                                true
                            )
                            writer.println(messageToJson(message))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (true) {
                val line = reader.readLine() ?: break
                handleMessage(line)
                
                // Broadcast to other players
                connectedPlayers.filter { it != socket }.forEach { otherSocket ->
                    try {
                        val writer = PrintWriter(
                            OutputStreamWriter(otherSocket.getOutputStream()),
                            true
                        )
                        writer.println(line)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleMessage(message: String) {
        try {
            val gameMessage = jsonToMessage(message)
            _receivedMessages.value = gameMessage
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun messageToJson(message: GameMessage): String {
        return "${message.type}|${message.playerId}|${message.playerName}|${message.gameId}|${message.data}"
    }

    private fun jsonToMessage(json: String): GameMessage {
        val parts = json.split("|")
        return GameMessage(
            type = parts.getOrNull(0) ?: "UNKNOWN",
            playerId = parts.getOrNull(1)?.toIntOrNull() ?: -1,
            playerName = parts.getOrNull(2) ?: "",
            gameId = parts.getOrNull(3) ?: "",
            data = parts.getOrNull(4) ?: ""
        )
    }

    fun disconnect() {
        try {
            clientSocket?.close()
            outputStream?.close()
            inputStream?.close()
            connectedPlayers.forEach { it.close() }
            serverSocket?.close()
            _connectionState.value = ConnectionState.DISCONNECTED
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean {
        return _connectionState.value == ConnectionState.CONNECTED ||
               _connectionState.value == ConnectionState.HOSTING
    }
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    HOSTING,
    ERROR
}

class WiFiDirectManager {
    // WiFi Direct implementation for advanced multiplayer
    // Can be extended for peer-to-peer connections without internet
    
    fun initializeWiFiDirect() {
        // Initialize WiFi Direct
    }

    fun startDiscovery() {
        // Discover nearby devices
    }

    fun connectToDevice(deviceAddress: String) {
        // Connect to specific device
    }
}
