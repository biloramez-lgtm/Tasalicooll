package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Card

sealed class GameAction {

    /* -------- Lifecycle -------- */
    data class StartGame(
        val team1Name: String,
        val team2Name: String
    ) : GameAction()

    object RestartGame : GameAction()

    /* -------- Bidding -------- */
    data class PlaceBid(
        val playerIndex: Int,
        val bid: Int
    ) : GameAction()

    /* -------- Playing -------- */
    data class PlayCard(
        val playerIndex: Int,
        val card: Card
    ) : GameAction()

    /* -------- UI -------- */
    object Retry : GameAction()
    object ClearError : GameAction()

    /* -------- Multiplayer -------- */
    data class ReceiveRemoteState(val data: String) : GameAction()
    object SyncWithPeer : GameAction()
}
