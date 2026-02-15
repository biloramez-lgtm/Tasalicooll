package com.tarneeb.network

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import java.net.*
import kotlin.random.Random
import com.tarneeb.engine.*

/**
 * NetworkManager - مدير الشبكة الكامل
 * 
 * يدعم:
 * ✅ إنشاء لعبة أونلاين
 * ✅ الانضمام لعبة موجودة
 * ✅ مزامنة حالة اللعبة
 * ✅ إرسال استقبال الحركات
 * ✅ معالجة الاتصالات
 */
class NetworkManager {
    
    // ========================================================================
    // STATE MANAGEMENT
    // ========================================================================
    
    private val _networkState = MutableStateFlow<NetworkGameState?>(null)
    val networkState: StateFlow<NetworkGameState?> = _networkState.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _playerAction = MutableStateFlow<NetworkAction?>(null)
    val playerAction: StateFlow<NetworkAction?> = _playerAction.asStateFlow()
    
    // ========================================================================
    // NETWORK DATA
    // ========================================================================
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var isServer = false
    private var isConnected = false
    private var playerName = ""
    private var gameCode = ""
    
    private val playerConnections = mutableMapOf<String, Socket>()
    private val gameState = mutableMapOf<String, String>()
    
    companion object {
        const val SERVER_PORT = 5555
        const val TAG = "TarneebNetwork"
    }
    
    // ========================================================================
    // GAME CREATION
    // ========================================================================
    
    /**
     * إنشاء لعبة جديدة (كـ Server)
     */
    fun createGame(name: String) {
        try {
            playerName = name
            gameCode = generateGameCode()
            isServer = true
            
            // إنشاء server
            serverSocket = ServerSocket(SERVER_PORT)
            
            _networkState.value = NetworkGameState(
                status = "الانتظار",
                gameCode = gameCode,
                players = listOf(name),
                isHost = true
            )
            
            Log.d(TAG, "اللعبة الجديدة: $gameCode")
            
            // بدء استقبال الاتصالات
            Thread { acceptConnections() }.start()
            
        } catch (e: Exception) {
            _error.value = "خطأ في إنشاء اللعبة: ${e.message}"
            Log.e(TAG, "Error creating game", e)
        }
    }
    
    /**
     * الانضمام إلى لعبة موجودة (كـ Client)
     */
    fun joinGame(code: String, name: String) {
        try {
            playerName = name
            gameCode = code
            isServer = false
            
            // الاتصال بـ Server
            val serverAddress = discoverServer(code)
            if (serverAddress != null) {
                clientSocket = Socket(serverAddress, SERVER_PORT)
                isConnected = true
                
                // إرسال بيانات اللاعب
                sendPlayerInfo(name)
                
                _networkState.value = NetworkGameState(
                    status = "متصل",
                    gameCode = code,
                    players = listOf(name),
                    isHost = false
                )
                
                // بدء استقبال الرسائل
                Thread { receiveMessages() }.start()
                
                Log.d(TAG, "انضمام للعبة: $code")
            } else {
                _error.value = "لم تتمكن من العثور على اللعبة"
            }
            
        } catch (e: Exception) {
            _error.value = "خطأ في الانضمام: ${e.message}"
            Log.e(TAG, "Error joining game", e)
        }
    }
    
    /**
     * اكتشاف Server (محاكاة - في الإنتاج استخدم UDP broadcast)
     */
    private fun discoverServer(gameCode: String): InetAddress? {
        return try {
            InetAddress.getByName("localhost")
        } catch (e: Exception) {
            null
        }
    }
    
    // ========================================================================
    // CONNECTION MANAGEMENT
    // ========================================================================
    
    /**
     * قبول الاتصالات (Server)
     */
    private fun acceptConnections() {
        try {
            while (isServer && serverSocket != null) {
                val socket = serverSocket?.accept()
                if (socket != null) {
                    val playerName = receivePlayerInfo(socket)
                    if (playerName != null) {
                        playerConnections[playerName] = socket
                        
                        // تحديث قائمة اللاعبين
                        updatePlayersList()
                        
                        // بدء استقبال الرسائل من هذا اللاعب
                        Thread { receivePlayerMessages(playerName, socket) }.start()
                        
                        Log.d(TAG, "لاعب جديد: $playerName")
                    }
                }
            }
        } catch (e: SocketException) {
            Log.d(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting connections", e)
        }
    }
    
    /**
     * إرسال بيانات اللاعب
     */
    private fun sendPlayerInfo(name: String) {
        try {
            val writer = OutputStreamWriter(clientSocket?.getOutputStream())
            writer.write("PLAYER_INFO:$name\n")
            writer.flush()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending player info", e)
        }
    }
    
    /**
     * استقبال بيانات اللاعب
     */
    private fun receivePlayerInfo(socket: Socket): String? {
        return try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val message = reader.readLine()
            if (message?.startsWith("PLAYER_INFO:") == true) {
                message.substringAfter("PLAYER_INFO:")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving player info", e)
            null
        }
    }
    
    // ========================================================================
    // MESSAGE HANDLING
    // ========================================================================
    
    /**
     * استقبال الرسائل (Client)
     */
    private fun receiveMessages() {
        try {
            val reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
            while (isConnected) {
                val message = reader.readLine()
                if (message != null) {
                    handleMessage(message)
                }
            }
        } catch (e: SocketException) {
            Log.d(TAG, "Client disconnected")
        } catch (e: Exception) {
            _error.value = "خطأ في الاتصال: ${e.message}"
            Log.e(TAG, "Error receiving messages", e)
        }
    }
    
    /**
     * استقبال الرسائل من لاعب معين (Server)
     */
    private fun receivePlayerMessages(playerName: String, socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (isServer) {
                val message = reader.readLine()
                if (message != null) {
                    handlePlayerMessage(playerName, message)
                }
            }
        } catch (e: SocketException) {
            Log.d(TAG, "Player disconnected: $playerName")
            playerConnections.remove(playerName)
            updatePlayersList()
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving from player", e)
        }
    }
    
    /**
     * معالجة الرسالة (Client)
     */
    private fun handleMessage(message: String) {
        when {
            message.startsWith("GAME_STATE:") -> {
                val state = message.substringAfter("GAME_STATE:")
                gameState["current"] = state
                
                // تحديث NetworkGameState
                _networkState.value = _networkState.value?.copy(
                    status = "جاري اللعب",
                    gameState = state
                )
            }
            
            message.startsWith("PLAYER_ACTION:") -> {
                val action = message.substringAfter("PLAYER_ACTION:")
                _playerAction.value = parseAction(action)
            }
            
            message.startsWith("ERROR:") -> {
                val errorMsg = message.substringAfter("ERROR:")
                _error.value = errorMsg
            }
        }
    }
    
    /**
     * معالجة رسالة اللاعب (Server)
     */
    private fun handlePlayerMessage(playerName: String, message: String) {
        when {
            message.startsWith("PLAYER_ACTION:") -> {
                val action = message.substringAfter("PLAYER_ACTION:")
                
                // إعادة إرسال للاعبين الآخرين
                broadcastMessage("PLAYER_ACTION:$playerName:$action")
            }
            
            message.startsWith("GAME_STATE_UPDATE:") -> {
                val state = message.substringAfter("GAME_STATE_UPDATE:")
                gameState["current"] = state
                
                // إعادة إرسال للجميع
                broadcastMessage("GAME_STATE:$state")
            }
        }
    }
    
    // ========================================================================
    // GAME ACTIONS
    // ========================================================================
    
    /**
     * إرسال حركة لاعب
     */
    fun sendPlayerAction(action: NetworkAction) {
        try {
            val message = buildActionMessage(action)
            
            if (isServer) {
                // كـ Server، إعادة إرسال للجميع
                broadcastMessage("PLAYER_ACTION:$message")
            } else {
                // كـ Client، إرسال للـ Server
                val writer = OutputStreamWriter(clientSocket?.getOutputStream())
                writer.write("PLAYER_ACTION:$message\n")
                writer.flush()
            }
            
            _playerAction.value = action
            Log.d(TAG, "Action sent: $message")
            
        } catch (e: Exception) {
            _error.value = "خطأ في إرسال الحركة: ${e.message}"
            Log.e(TAG, "Error sending action", e)
        }
    }
    
    /**
     * إرسال حالة اللعبة
     */
    fun sendGameState(gameState: TarneebGame) {
        try {
            val stateMessage = encodeGameState(gameState)
            
            if (isServer) {
                broadcastMessage("GAME_STATE:$stateMessage")
            } else {
                val writer = OutputStreamWriter(clientSocket?.getOutputStream())
                writer.write("GAME_STATE_UPDATE:$stateMessage\n")
                writer.flush()
            }
            
            Log.d(TAG, "Game state sent")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending game state", e)
        }
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    /**
     * إرسال رسالة للجميع (Server)
     */
    private fun broadcastMessage(message: String) {
        playerConnections.values.forEach { socket ->
            try {
                val writer = OutputStreamWriter(socket.getOutputStream())
                writer.write("$message\n")
                writer.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Error broadcasting message", e)
            }
        }
    }
    
    /**
     * تحديث قائمة اللاعبين
     */
    private fun updatePlayersList() {
        val players = mutableListOf(playerName)
        players.addAll(playerConnections.keys)
        
        _networkState.value = _networkState.value?.copy(
            players = players
        )
    }
    
    /**
     * بناء رسالة الحركة
     */
    private fun buildActionMessage(action: NetworkAction): String {
        return when (action) {
            is NetworkAction.PlaceBid -> "BID:${action.playerId}:${action.bid}"
            is NetworkAction.PlayCard -> "CARD:${action.playerId}:${action.cardSuit}:${action.cardRank}"
        }
    }
    
    /**
     * تحليل رسالة الحركة
     */
    private fun parseAction(message: String): NetworkAction? {
        return when {
            message.startsWith("BID:") -> {
                val parts = message.substringAfter("BID:").split(":")
                NetworkAction.PlaceBid(parts[0].toInt(), parts[1].toInt())
            }
            message.startsWith("CARD:") -> {
                val parts = message.substringAfter("CARD:").split(":")
                NetworkAction.PlayCard(parts[0].toInt(), parts[1], parts[2])
            }
            else -> null
        }
    }
    
    /**
     * ترميز حالة اللعبة
     */
    private fun encodeGameState(game: TarneebGame): String {
        return "${game.gamePhase.name}:${game.team1.totalScore}:${game.team2.totalScore}:${game.currentRound}"
    }
    
    /**
     * فك ترميز حالة اللعبة
     */
    private fun decodeGameState(message: String): Map<String, String> {
        val parts = message.split(":")
        return mapOf(
            "phase" to (parts.getOrNull(0) ?: ""),
            "team1Score" to (parts.getOrNull(1) ?: "0"),
            "team2Score" to (parts.getOrNull(2) ?: "0"),
            "round" to (parts.getOrNull(3) ?: "1")
        )
    }
    
    /**
     * توليد كود اللعبة
     */
    private fun generateGameCode(): String {
        return List(6) {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            chars.random()
        }.joinToString("")
    }
    
    /**
     * قطع الاتصال
     */
    fun disconnect() {
        try {
            isConnected = false
            isServer = false
            
            clientSocket?.close()
            serverSocket?.close()
            playerConnections.values.forEach { it.close() }
            playerConnections.clear()
            
            _networkState.value = null
            _error.value = null
            
            Log.d(TAG, "Disconnected")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

/**
 * NetworkGameState - حالة اللعبة الشبكية
 */
data class NetworkGameState(
    val status: String = "",  // الانتظار، متصل، جاري اللعب
    val gameCode: String = "",
    val players: List<String> = emptyList(),
    val isHost: Boolean = false,
    val gameState: String? = null
)

/**
 * NetworkAction - حركات اللعبة الشبكية
 */
sealed class NetworkAction {
    data class PlaceBid(val playerId: Int, val bid: Int) : NetworkAction()
    data class PlayCard(val playerId: Int, val cardSuit: String, val cardRank: String) : NetworkAction()
}

/**
 * NetworkMessage - رسالة الشبكة
 */
data class NetworkMessage(
    val type: String,  // PLAYER_INFO, GAME_STATE, PLAYER_ACTION, ERROR
    val content: String
) {
    override fun toString(): String = "$type:$content"
    
    companion object {
        fun parse(message: String): NetworkMessage? {
            val colonIndex = message.indexOf(":")
            return if (colonIndex >= 0) {
                NetworkMessage(
                    type = message.substring(0, colonIndex),
                    content = message.substring(colonIndex + 1)
                )
            } else {
                null
            }
        }
    }
}
