package com.tasalicool.game.repository

import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.model.Card
import kotlinx.coroutines.flow.StateFlow

class GameRepository(
    private val engine: GameEngine = GameEngine()
) {

    val gameState = engine.gameState
    val error = engine.error

    fun startGame(team1: String, team2: String) {
        engine.initializeDefaultGame(team1, team2)
    }

    fun restartGame() {
        engine.restartGame()
    }

    fun placeBid(playerIndex: Int, bid: Int) {
        engine.gameState.value?.let {
            engine.placeBid(it, playerIndex, bid)
        }
    }

    fun playCard(playerIndex: Int, card: Card) {
        engine.gameState.value?.let {
            engine.playCard(it, playerIndex, card)
        }
    }

    fun getValidBids(playerIndex: Int): List<Int> =
        engine.getValidBids(playerIndex)

    fun getValidCards(playerIndex: Int): List<Card> =
        engine.getValidCards(playerIndex)

    fun clearError() = engine.clearError()
}
