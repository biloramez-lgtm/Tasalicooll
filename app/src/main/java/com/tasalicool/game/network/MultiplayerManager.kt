package com.tasalicool.game.network

import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
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

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    /* ==================== STATE ==================== */

    private val _connectionState =
        MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _players =
        MutableStateFlow<Map<String, NetworkPlayer>>(emptyMap())
    val players = _players.asStateFlow()

    private val _events =
        MutableSharedFlow<NetworkEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    private val _commands =
        MutableSharedFlow<NetworkCommand>(extraBufferCapacity = 64)
    val commands = _commands.asSharedFlow()

    /* ==================== SERVER ==================== */

    private var serverSocket: ServerSocket? = null
    private val clients = mutableMapOf<String, ClientConnection>()
    private var serverRunning = false

    fun startServer(port: Int = DEFAULT_PORT): Boolean {
        return try {
            serverSocket = ServerSocket(port)
            serverRunning = true
            _connectionState.value = ConnectionState.HOSTING

            thread(name = "ServerThread", isDaemon = true) {
                while (serverRunning) {
                    val socket = serverSocket?.accept() ?: break
                    addClient(socket)
                }
            }
            true
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }

    private fun addClient(socket: Socket) {
        val id = socket.remoteSocketAddress.toString()

        val connection = ClientConnection(
            socket,
            json,
            onMessage = { pid, cmd -> onClientMessage(pid, cmd) },
            onDisconnect = { pid -> removeClient(pid) }
        )

        clients[id] = connection
        connection.start()
    }

    private fun onClientMessage(playerId: String, command: NetworkCommand) {
        if (command is NetworkCommand.PlayerJoined) {
            val player = NetworkPlayer(
                id = playerId,
                name = command.playerName,
                address = playerId,
                status = PlayerStatus.CONNECTED
            )
            _players.value = _players.value + (playerId to player)
            _events.tryEmit(NetworkEvent.PlayerConnected(player))
        }

        _commands.tryEmit(command)

        clients.forEach { (id, conn) ->
            if (id != playerId) conn.send(command)
        }
    }

    private fun removeClient(playerId: String) {
        clients.remove(playerId)
        _players.value = _players.value - playerId
        _events.tryEmit(NetworkEvent.PlayerDisconnected(playerId))
    }

    fun broadcast(command: NetworkCommand) {
        clients.values.forEach { it.send(command) }
    }

    /* ==================== CLIENT ==================== */

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
                clientSocket!!,
                json,
                onMessage = { _commands.tryEmit(it) },
                onDisconnect = {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            )

            serverConnection!!.start()
            _connectionState.value = ConnectionState.CONNECTED

            sendToServer(
                NetworkCommand.PlayerJoined(playerId, playerName)
            )
            true
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }

    fun sendToServer(command: NetworkCommand) {
        serverConnection?.send(command)
    }

    /* ==================== AI SUPPORT ==================== */

    fun sendFromAI(command: NetworkCommand) {
        _commands.tryEmit(command)
        broadcast(command)
    }

    /* ==================== CLEAN ==================== */

    fun disconnect() {
        serverRunning = false
        clients.values.forEach { it.close() }
        clients.clear()

        serverConnection?.close()
        clientSocket?.close()
        serverSocket?.close()

        _players.value = emptyMap()
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}

/* ==================== CONNECTIONS ==================== */

private class ClientConnection(
    private val socket: Socket,
    private val json: Json,
    private val onMessage: (String, NetworkCommand) -> Unit,
    private val onDisconnect: (String) -> Unit
) : Thread(true) {

    private val id = socket.remoteSocketAddress.toString()
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

    override fun run() {
        try {
            while (true) {
                val line = reader.readLine() ?: break
                val cmd = json.decodeFromString<NetworkCommand>(line)
                onMessage(id, cmd)
            }
        } finally {
            close()
            onDisconnect(id)
        }
    }

    fun send(cmd: NetworkCommand) {
        writer.apply {
            write(json.encodeToString(cmd))
            newLine()
            flush()
        }
    }

    fun close() = socket.close()
}

private class ServerConnection(
    private val socket: Socket,
    private val json: Json,
    private val onMessage: (NetworkCommand) -> Unit,
    private val onDisconnect: () -> Unit
) : Thread(true) {

    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

    override fun run() {
        try {
            while (true) {
                val line = reader.readLine() ?: break
                onMessage(json.decodeFromString(line))
            }
        } finally {
            close()
            onDisconnect()
        }
    }

    fun send(cmd: NetworkCommand) {
        writer.apply {
            write(json.encodeToString(cmd))
            newLine()
            flush()
        }
    }

    fun close() = socket.close()
}
