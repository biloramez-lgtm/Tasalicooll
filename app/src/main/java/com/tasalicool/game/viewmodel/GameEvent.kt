package com.tasalicool.game.viewmodel

import com.tasalicool.game.model.Card

sealed class GameEvent {

    object StartGame : GameEvent()
    object RestartGame : GameEvent()

    data class PlaceBid(val value: Int) : GameEvent()

    data class PlayCard(val card: Card) : GameEvent()

}
