package com.tasalicool.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasalicool.game.model.Card
import com.tasalicool.game.repository.GameRepository
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    val gameState = repository.gameState
    val errorState = repository.error

    fun startGame(team1Name: String, team2Name: String) {
        viewModelScope.launch {
            repository.startGame(team1Name, team2Name)
        }
    }

    fun placeBid(playerIndex: Int, bid: Int) {
        viewModelScope.launch {
            repository.placeBid(playerIndex, bid)
        }
    }

    fun playCard(playerIndex: Int, card: Card) {
        viewModelScope.launch {
            repository.playCard(playerIndex, card)
        }
    }

    fun restartGame() {
        repository.restartGame()
    }

    fun clearError() {
        repository.clearError()
    }
}
