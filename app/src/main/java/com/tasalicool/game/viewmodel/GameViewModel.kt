package com.tasalicool.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasalicool.game.engine.GameEngine
import com.tasalicool.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine()

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState

    fun initializeGame(player1Name: String, player2Name: String, ai1Name: String, ai2Name: String) {
        viewModelScope.launch {
            val player1 = Player(id = 0, name = player1Name, isAI = false)
            val player3 = Player(id = 2, name = player2Name, isAI = false)
            val player2 = Player(id = 1, name = ai1Name, isAI = true)
            val player4 = Player(id = 3, name = ai2Name, isAI = true)

            val team1 = Team(id = 1, name = "Team 1", player1 = player1, player2 = player3)
            val team2 = Team(id = 2, name = "Team 2", player1 = player2, player2 = player4)

            val game = gameEngine.initializeGame(team1, team2, 0)
            gameEngine.dealCards(game)
            _gameState.emit(game)
        }
    }

    fun placeBid(playerIndex: Int, bid: Int) {
        viewModelScope.launch {
            val game = _gameState.value ?: return@launch
            gameEngine.placeBid(game, playerIndex, bid)
            _gameState.emit(game)
        }
    }

    fun playCard(playerIndex: Int, card: Card) {
        viewModelScope.launch {
            val game = _gameState.value ?: return@launch
            gameEngine.playCard(game, playerIndex, card)
            _gameState.emit(game)
        }
    }
}
