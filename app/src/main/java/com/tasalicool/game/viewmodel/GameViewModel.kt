package com.tasalicool.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasalicool.game.engine.ComprehensiveGameEngine
import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Game
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val engine: ComprehensiveGameEngine
) : ViewModel() {

    val gameState: StateFlow<Game?> = engine.gameState
    val errorState = engine.error

    fun startGame(
        team1Name: String,
        team2Name: String
    ) {
        viewModelScope.launch {
            engine.initializeDefaultGame(
                team1Name = team1Name,
                team2Name = team2Name
            )
        }
    }

    fun placeBid(playerIndex: Int, bid: Int) {
        viewModelScope.launch {
            engine.gameState.value?.let {
                engine.placeBid(it, playerIndex, bid)
            }
        }
    }

    fun playCard(playerIndex: Int, card: Card) {
        viewModelScope.launch {
            engine.gameState.value?.let {
                engine.playCard(it, playerIndex, card)
            }
        }
    }

    fun clearError() {
        engine.clearError()
    }
}
