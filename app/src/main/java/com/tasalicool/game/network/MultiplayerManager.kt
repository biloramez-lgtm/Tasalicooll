package com.tasalicool.game.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MultiplayerManager {

    companion object {
        const val DEFAULT_PORT = 5555
        const val CONNECTION_TIMEOUT = 30_000
    }

    // JSON configuration
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    // ==================== STATE ====================

    private val _connectionState =
        MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> =
        _connectionState.asStateFlow()

    private val _connectedPlayers =
        MutableStateFlow<List<NetworkPlayer>>(emptyList())
    val connectedPlayers: StateFlow<List<NetworkPlayer>> =
        _connectedPlayers.asStateFlow()

    private val _networkEvents =
        MutableStateFlow<NetworkEvent?>(null)
    val networkEvents: StateFlow<NetworkEvent?> =
        _networkEvents.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    // ==================== SERVER ====================

    private var serverSocket: ServerSocket? = null
    private val clientConnections = mutableMapOf<String, ClientConnection>()
    private var isServerRunning = false

    fun startServer(port: Int = DEFAULT_PORT): Boolean {
        return try {
            serverSocket = ServerSocket(port)
            isServerRunning = true
            _connectionState.value = ConnectionState.HOSTING

            thread(isDaemon = true, name = "ServerThread") {
                while (isServerRunning) {
                    try {
                        val socket = serverSocket?.accept() ?: break
                        handleNewClient(socket)
                    } catch (e: Exception) {
                        if (isServerRunning) {
                            _errorMessage.value = e.message
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            _errorMessage.value = e.message
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }

    private fun handleNewClient(socket: Socket) {
        val playerId = socket.remoteSocketAddress.toString()

        val connection = ClientConnection(
            socket = socket,
            json = json,
            onMessageReceived = { id, command ->
                handleClientMessage(id, command)
            },
            onDisconnected = { id ->
                handleClientDisconnected(id)
            }
        )

        clientConnections[playerId] = connection
        connection.start()
        updatePlayersList()

        _networkEvents.value =
            NetworkEvent.PlayerConnected(
                NetworkPlayer(
                    id = playerId,
                    name = "Player ${clientConnections.size}",
                    address = playerId,
                    status = PlayerStatus.CONNECTED
                )
            )
    }

    private fun handleClientMessage(
        playerId: String,
        command: NetworkCommand
    ) {
        _networkEvents.value =
            NetworkEvent.MessageReceived(playerId, command)

        clientConnections.forEach { (id, conn) ->
            if (id != playerId) {
                conn.send(command)
            }
        }
    }

    private fun handleClientDisconnected(playerId: String) {
        clientConnections.remove(playerId)
        updatePlayersList()
        _networkEvents.value =
            NetworkEvent.PlayerDisconnected(playerId)
    }

    fun broadcast(command: NetworkCommand) {
        clientConnections.values.forEach {
            it.send(command)
        }
    }

    // ==================== CLIENT ====================

    private var clientSocket: Socket? = null
    private var serverConnection: ServerConnection? = null

    fun connectToServer(
        host: String,
        port: Int = DEFAULT_PORT,
        playerId: String,
        playerName: String
    ): Boolean {
        return try {
            _connectionState.value = ConnectionState.CONNECTING

            clientSocket = Socket(host, port).apply {
                soTimeout = CONNECTION_TIMEOUT
            }

            serverConnection = ServerConnection(
                socket = clientSocket!!,
                json = json,
                onMessageReceived = {
                    _networkEvents.value =
                        NetworkEvent.MessageReceived("server", it)
                },
                onDisconnected = {
                    _connectionState.value =
                        ConnectionState.DISCONNECTED
                }
            )

            serverConnection?.start()
            _connectionState.value = ConnectionState.CONNECTED

            sendToServer(
                NetworkCommand.PlayerJoined(
                    playerId = playerId,
                    playerName = playerName
                )
            )
            true
        } catch (e: Exception) {
            _errorMessage.value = e.message
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }

    fun sendToServer(command: NetworkCommand) {
        serverConnection?.send(command)
    }

    // ==================== COMMON ====================

    private fun updatePlayersList() {
        _connectedPlayers.value =
            clientConnections.values.map {
                NetworkPlayer(
                    id = it.playerId,
                    name = it.playerName,
                    address = it.playerId,
                    status = PlayerStatus.CONNECTED
                )
            }
    }

    fun disconnect() {
        isServerRunning = false

        clientConnections.values.forEach { it.close() }
        clientConnections.clear()

        serverConnection?.close()
        clientSocket?.close()
        serverSocket?.close()

        _connectionState.value = ConnectionState.DISCONNECTED
        _connectedPlayers.value = emptyList()
    }
}

/* ==================== CONNECTION CLASSES ==================== */

private class ClientConnection(
    private val socket: Socket,
    private val json: Json,
    private val onMessageReceived: (String, NetworkCommand) -> Unit,
    private val onDisconnected: (String) -> Unit
) : Thread(true) {

    val playerId = socket.remoteSocketAddress.toString()
    var playerName: String = "Unknown"

    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

    override fun run() {
        try {
            while (true) {
                val line = reader.readLine() ?: break
                val command =
                    json.decodeFromString<NetworkCommand>(line)

                if (command is NetworkCommand.PlayerJoined) {
                    playerName = command.playerName
                }
                onMessageReceived(playerId, command)
            }
        } catch (_: Exception) {
        } finally {
            close()
            onDisconnected(playerId)
        }
    }

    fun send(command: NetworkCommand) {
        writer.apply {
            write(json.encodeToString(command))
            newLine()
            flush()
        }
    }

    fun close() {
        socket.close()
    }
}

private class ServerConnection(
    private val socket: Socket,
    private val json: Json,
    private val onMessageReceived: (NetworkCommand) -> Unit,
    private val onDisconnected: () -> Unit
) : Thread(true) {

    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

    override fun run() {
        try {
            while (true) {
                val line = reader.readLine() ?: break
                val command =
                    json.decodeFromString<NetworkCommand>(line)
                onMessageReceived(command)
            }
        } catch (_: Exception) {
        } finally {
            close()
            onDisconnected()
        }
    }

    fun send(command: NetworkCommand) {
        writer.apply {
            write(json.encodeToString(command))
            newLine()
            flush()
        }
    }

    fun close() {
        socket.close()
    }
}
