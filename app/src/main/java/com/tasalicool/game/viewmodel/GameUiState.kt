package com.tasalicool.game.viewmodel

/**
 * UiError - Error representation in UI layer
 */
sealed class UiError {
    data class GameError(val message: String) : UiError()
    data class NetworkError(val message: String) : UiError()
    data class ValidationError(val message: String) : UiError()
    data class TimeoutError(val message: String = "Request timeout") : UiError()
    object Unknown : UiError()
    
    fun getMessage(): String = when (this) {
        is GameError -> message
        is NetworkError -> message
        is ValidationError -> message
        is TimeoutError -> message
        is Unknown -> "Unknown error occurred"
    }
}

/**
 * GameUiState - Main UI state for game screens
 */
data class GameUiState(
    val game: Any? = null, // Game object
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val isGameActive: Boolean = false,
    val canPerformAction: Boolean = false,
    val currentPlayerIndex: Int = 0,
    val currentTrick: Int = 0
)



/**
 * NetworkEvent - Events from network layer
 */
sealed class NetworkEvent {
    data class ReceiveRemoteState(val state: Any) : NetworkEvent()
    object Retry : NetworkEvent()
    object SyncWithPeer : NetworkEvent()
    data class ConnectionLost(val reason: String) : NetworkEvent()
}
