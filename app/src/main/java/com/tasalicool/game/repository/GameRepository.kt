package com.tasalicool.game.repository

import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.model.Card
import kotlinx.coroutines.flow.StateFlow

class GameRepository(
    private val engine: GameEngine = GameEngine()
) {

    val gameState: StateFlow<com.tasalicool.game.model.Game?> = engine.gameState
    val error: StateFlow<String?> = engine.error

    fun startGame(team1Name: String, team2Name: String) {
        engine.initializeDefaultGame(
            team1Name = team1Name,
            team2Name = team2Name
        )
    }

    fun restartGame() {
        engine.restartGame()
    }

    fun placeBid(playerIndex: Int, bid: Int) {
        val game = engine.gameState.value ?: return
        engine.placeBid(game, playerIndex, bid)
    }

    fun playCard(playerIndex: Int, card: Card) {
        val game = engine.gameState.value ?: return
        engine.playCard(game, playerIndex, card)
    }

    fun getValidBids(playerIndex: Int): List<Int> =
        engine.getValidBids(playerIndex)

    fun getValidCards(playerIndex: Int): List<Card> =
        engine.getValidCards(playerIndex)

    fun clearError() {
        engine.clearError()
    }
}
