package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Card

sealed class GameAction {

    /* ===== GAME FLOW ===== */
    data class StartGame(
        val team1: String,
        val team2: String
    ) : GameAction()

    object RestartGame : GameAction()

    /* ===== BIDDING ===== */
    data class PlaceBid(
        val playerIndex: Int,
        val bid: Int
    ) : GameAction()

    /* ===== PLAYING ===== */
    data class PlayCard(
        val playerIndex: Int,
        val card: Card
    ) : GameAction()

    /* ===== ERROR ===== */
    object ClearError : GameAction()

    /* ===== MULTIPLAYER (جاهز) ===== */
    object CreateRoom : GameAction()
    object JoinRoom : GameAction()
    object LeaveRoom : GameAction()
}
