package com.tasalicool.game.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * MultiplayerManager - مدير الملعب متعدد اللاعبين
 * 
 * المسؤوليات:
 * ✅ Server Mode - استضافة اللعبة (Host)
 * ✅ Client Mode - الانضمام للعبة (Join)
 * ✅ Connection Management - إدارة الاتصالات
 * ✅ Message Broadcasting - بث الرسائل
 * ✅ Network State - حالة الشبكة
 */
class MultiplayerManager {
    
    companion object {
        const val DEFAULT_PORT = 5555
        const val CONNECTION_TIMEOUT = 30000  // 30 seconds
        const val MESSAGE_SEPARATOR = "|"
    }
    
    // ==================== STATE ====================
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _connectedPlayers = MutableStateFlow<List<NetworkPlayer>>(emptyList())
    val connectedPlayers: StateFlow<List<NetworkPlayer>> = _connectedPlayers.asStateFlow()
    
    private val _networkEvents = MutableStateFlow<NetworkEvent?>(null)
    val networkEvents: StateFlow<NetworkEvent?> = _networkEvents.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // ==================== SERVER MODE ====================
    
    private var serverSocket: ServerSocket? = null
    private val clientConnections = mutableMapOf<String, ClientConnection>()
    private var isServerRunning = false
    
    /**
     * بدء السيرفر (Host mode)
     * 
     * يستقبل اتصالات اللاعبين الآخرين
     */
    fun startServer(port: Int = DEFAULT_PORT): Boolean {
        return try {
            serverSocket = ServerSocket(port)
            isServerRunning = true
            _connectionState.value = ConnectionState.HOSTING
            
            // قبول الاتصالات في thread منفصل
            thread(isDaemon = true, name = "ServerThread") {
                while (isServerRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        handleNewClient(clientSocket)
                    } catch (e: Exception) {
                        if (isServerRunning) {
                            _errorMessage.value = "Error accepting client: ${e.message}"
                        }
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start server: ${e.message}"
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }
    
    /**
     * التعامل مع اتصال عميل جديد
     */
    private fun handleNewClient(socket: Socket) {
        try {
            val clientConnection = ClientConnection(
                socket = socket,
                onMessageReceived = { playerId, message ->
                    handleClientMessage(playerId, message)
                },
                onDisconnected = { playerId ->
                    handleClientDisconnected(playerId)
                }
            )
            
            val playerId = socket.remoteSocketAddress.toString()
            clientConnections[playerId] = clientConnection
            clientConnection.start()
            
            // إضافة اللاعب للقائمة
            val player = NetworkPlayer(
                id = playerId,
                name = "Player ${clientConnections.size}",
                address = socket.remoteSocketAddress.toString(),
                status = PlayerStatus.CONNECTED
            )
            
            updatePlayersList()
            _networkEvents.value = NetworkEvent.PlayerConnected(player)
            
        } catch (e: Exception) {
            _errorMessage.value = "Error handling new client: ${e.message}"
        }
    }
    
    /**
     * بث رسالة لجميع العملاء
     */
    fun broadcastMessage(command: NetworkCommand) {
        clientConnections.forEach { (_, connection) ->
            connection.sendMessage(command)
        }
    }
    
    /**
     * إرسال رسالة لعميل محدد
     */
    fun sendToPlayer(playerId: String, command: NetworkCommand) {
        clientConnections[playerId]?.sendMessage(command)
    }
    
    /**
     * التعامل مع رسالة من عميل
     */
    private fun handleClientMessage(playerId: String, command: NetworkCommand) {
        _networkEvents.value = NetworkEvent.MessageReceived(playerId, command)
        
        // بث الرسالة للعملاء الآخرين
        clientConnections.forEach { (id, connection) ->
            if (id != playerId) {
                connection.sendMessage(command)
            }
        }
    }
    
    /**
     * التعامل مع قطع اتصال عميل
     */
    private fun handleClientDisconnected(playerId: String) {
        clientConnections.remove(playerId)
        updatePlayersList()
        _networkEvents.value = NetworkEvent.PlayerDisconnected(playerId)
    }
    
    // ==================== CLIENT MODE ====================
    
    private var clientSocket: Socket? = null
    private var clientConnection: ServerConnection? = null
    
    /**
     * الاتصال بالسيرفر (Join mode)
     * 
     * @param hostAddress عنوان المضيف (IP)
     * @param port المنفذ
     * @param playerName اسم اللاعب
     */
    fun connectToServer(
        hostAddress: String,
        port: Int = DEFAULT_PORT,
        playerName: String
    ): Boolean {
        return try {
            _connectionState.value = ConnectionState.CONNECTING
            
            clientSocket = Socket(hostAddress, port)
            clientSocket?.soTimeout = CONNECTION_TIMEOUT
            
            clientConnection = ServerConnection(
                socket = clientSocket!!,
                onMessageReceived = { command ->
                    _networkEvents.value = NetworkEvent.MessageReceived("server", command)
                },
                onDisconnected = {
                    handleServerDisconnected()
                }
            )
            
            clientConnection?.start()
            
            _connectionState.value = ConnectionState.CONNECTED
            
            // إرسال معلومات اللاعب
            sendToServer(
                NetworkCommand.PlayerJoined(
                    playerName = playerName,
                    timestamp = System.currentTimeMillis()
                )
            )
            
            true
        } catch (e: Exception) {
            _errorMessage.value = "Failed to connect: ${e.message}"
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }
    
    /**
     * إرسال رسالة للسيرفر
     */
    fun sendToServer(command: NetworkCommand) {
        clientConnection?.sendMessage(command)
    }
    
    /**
     * التعامل مع قطع اتصال السيرفر
     */
    private fun handleServerDisconnected() {
        _connectionState.value = ConnectionState.DISCONNECTED
        _networkEvents.value = NetworkEvent.ServerDisconnected
    }
    
    // ==================== CONNECTION MANAGEMENT ====================
    
    /**
     * تحديث قائمة اللاعبين
     */
    private fun updatePlayersList() {
        val players = clientConnections.values.map { connection ->
            NetworkPlayer(
                id = connection.getPlayerId(),
                name = connection.getPlayerName(),
                address = connection.getAddress(),
                status = PlayerStatus.CONNECTED
            )
        }
        _connectedPlayers.value = players
    }
    
    /**
     * قطع الاتصال
     */
    fun disconnect() {
        try {
            isServerRunning = false
            
            // إغلاق جميع اتصالات العملاء
            clientConnections.forEach { (_, connection) ->
                connection.close()
            }
            clientConnections.clear()
            
            // إغلاق السيرفر
            serverSocket?.close()
            serverSocket = null
            
            // إغلاق الاتصال بالسيرفر
            clientConnection?.close()
            clientSocket?.close()
            clientSocket = null
            
            _connectionState.value = ConnectionState.DISCONNECTED
            _connectedPlayers.value = emptyList()
        } catch (e: Exception) {
            _errorMessage.value = "Error disconnecting: ${e.message}"
        }
    }
    
    /**
     * الحصول على حالة الاتصال
     */
    fun getConnectionStatus(): ConnectionState = _connectionState.value
    
    /**
     * الحصول على عدد اللاعبين المتصلين
     */
    fun getConnectedPlayerCount(): Int = clientConnections.size
}

/**
 * Client Connection Thread
 */
private class ClientConnection(
    private val socket: Socket,
    private val onMessageReceived: (String, NetworkCommand) -> Unit,
    private val onDisconnected: (String) -> Unit
) : Thread(isDaemon = true) {
    
    private val playerId = socket.remoteSocketAddress.toString()
    private var playerName = "Unknown"
    private val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private var isRunning = true
    
    override fun run() {
        try {
            while (isRunning) {
                val message = reader.readLine() ?: break
                val command = NetworkCommand.fromString(message)
                
                if (command is NetworkCommand.PlayerJoined) {
                    playerName = command.playerName
                }
                
                onMessageReceived(playerId, command)
            }
        } catch (e: Exception) {
            if (isRunning) {
                // Connection error
            }
        } finally {
            close()
            onDisconnected(playerId)
        }
    }
    
    fun sendMessage(command: NetworkCommand) {
        try {
            writer.println(command.toString())
        } catch (e: Exception) {
            // Write error
        }
    }
    
    fun close() {
        isRunning = false
        try {
            socket.close()
        } catch (e: Exception) {
            // Already closed
        }
    }
    
    fun getPlayerId(): String = playerId
    fun getPlayerName(): String = playerName
    fun getAddress(): String = socket.remoteSocketAddress.toString()
}

/**
 * Server Connection Thread
 */
private class ServerConnection(
    private val socket: Socket,
    private val onMessageReceived: (NetworkCommand) -> Unit,
    private val onDisconnected: () -> Unit
) : Thread(isDaemon = true) {
    
    private val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private var isRunning = true
    
    override fun run() {
        try {
            while (isRunning) {
                val message = reader.readLine() ?: break
                val command = NetworkCommand.fromString(message)
                onMessageReceived(command)
            }
        } catch (e: Exception) {
            if (isRunning) {
                // Connection error
            }
        } finally {
            close()
            onDisconnected()
        }
    }
    
    fun sendMessage(command: NetworkCommand) {
        try {
            writer.println(command.toString())
        } catch (e: Exception) {
            // Write error
        }
    }
    
    fun close() {
        isRunning = false
        try {
            socket.close()
        } catch (e: Exception) {
            // Already closed
        }
    }
    
    fun getPlayerId(): String = socket.remoteSocketAddress.toString()
    fun getPlayerName(): String = "Server"
    fun getAddress(): String = socket.remoteSocketAddress.toString()
}
