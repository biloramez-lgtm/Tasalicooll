package com.tasalicool.game.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

data class GameMessage(
    val type: String,
    val playerId: Int,
    val playerName: String,
    val gameId: String,
    val data: String = ""
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
            }
        }
    }

    fun joinGame(hostAddress: String, port: Int = 5555) {
        thread {
            try {
                clientSocket = Socket(hostAddress, port)
                outputStream = PrintWriter(OutputStreamWriter(clientSocket?.getOutputStream()), true)
                inputStream = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
                _connectionState.value = ConnectionState.CONNECTED

                while (true) {
                    val message = inputStream?.readLine() ?: break
                    handleMessage(message)
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }

    fun sendMessage(message: GameMessage) {
        thread {
            try {
                outputStream?.println(messageToString(message))
                connectedPlayers.forEach { socket ->
                    try {
                        val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
                        writer.println(messageToString(message))
                    } catch (e: Exception) {}
                }
            } catch (e: Exception) {}
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (true) {
                val line = reader.readLine() ?: break
                handleMessage(line)
                connectedPlayers.filter { it != socket }.forEach { otherSocket ->
                    try {
                        val writer = PrintWriter(OutputStreamWriter(otherSocket.getOutputStream()), true)
                        writer.println(line)
                    } catch (e: Exception) {}
                }
            }
        } catch (e: Exception) {}
    }

    private fun handleMessage(message: String) {
        val gameMessage = stringToMessage(message)
        _receivedMessages.value = gameMessage
    }

    private fun messageToString(message: GameMessage): String {
        return "${message.type}|${message.playerId}|${message.playerName}|${message.gameId}|${message.data}"
    }

    private fun stringToMessage(str: String): GameMessage {
        val parts = str.split("|")
        return GameMessage(
            type = parts.getOrNull(0) ?: "",
            playerId = parts.getOrNull(1)?.toIntOrNull() ?: -1,
            playerName = parts.getOrNull(2) ?: "",
            gameId = parts.getOrNull(3) ?: "",
            data = parts.getOrNull(4) ?: ""
        )
    }

    fun disconnect() {
        clientSocket?.close()
        outputStream?.close()
        inputStream?.close()
        connectedPlayers.forEach { it.close() }
        serverSocket?.close()
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, HOSTING, ERROR }
