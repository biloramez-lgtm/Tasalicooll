package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Game
import com.tasalicool.game.model.GamePhase

/**
 * GameEvent
 *
 * أحداث تصدر من منطق اللعبة (Engine / Repository)
 * ولا تحتوي أي منطق UI
 */
sealed class GameEvent {

    /* ================= GAME FLOW ================= */

    data class GameCreated(
        val game: Game
    ) : GameEvent()

    data class GameUpdated(
        val game: Game
    ) : GameEvent()

    data class PhaseChanged(
        val phase: GamePhase
    ) : GameEvent()

    object GameEnded : GameEvent()


    /* ================= BIDDING ================= */

    data class BidRequested(
        val playerIndex: Int,
        val availableBids: List<Int>
    ) : GameEvent()

    data class BidPlaced(
        val playerIndex: Int,
        val bid: Int
    ) : GameEvent()


    /* ================= PLAYING ================= */

    data class TurnChanged(
        val playerIndex: Int
    ) : GameEvent()

    data class CardPlayed(
        val playerIndex: Int,
        val card: Card
    ) : GameEvent()

    data class TrickCompleted(
        val winnerPlayerIndex: Int
    ) : GameEvent()


    /* ================= ROUND / SCORE ================= */

    data class RoundCompleted(
        val roundNumber: Int
    ) : GameEvent()

    data class ScoreUpdated(
        val team1Score: Int,
        val team2Score: Int
    ) : GameEvent()


    /* ================= ERROR ================= */

    data class ErrorOccurred(
        val message: String,
        val recoverable: Boolean = true
    ) : GameEvent()


    /* ================= MULTIPLAYER ================= */

    object MultiplayerEnabled : GameEvent()

    data class PlayerConnected(
        val playerId: String
    ) : GameEvent()

    data class PlayerDisconnected(
        val playerId: String
    ) : GameEvent()

    data class RemoteStateReceived(
        val serializedState: String
    ) : GameEvent()

    object SyncStarted : GameEvent()
    object SyncCompleted : GameEvent()


    /* ================= UI ACTIONS (from ViewModel) ================= */

    object CreateRoom : GameEvent()
    
    data class JoinRoom(val roomCode: String) : GameEvent()
    
    object LeaveRoom : GameEvent()
    
    data class PlaceBid(val bid: Int) : GameEvent()
    
    data class PlayCard(val cardIndex: Int) : GameEvent()
    
    object ConcedGame : GameEvent()
    
    object RestartGame : GameEvent()
}
