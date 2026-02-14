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

    /* ================= GAME FLOW ================= */

    fun startGame(team1Name: String, team2Name: String) {
        viewModelScope.launch {
            repository.startGame(team1Name, team2Name)
        }
    }

    fun restartGame() {
        repository.restartGame()
    }

    /* ================= BIDDING ================= */

    fun placeBid(playerIndex: Int, bid: Int) {
        viewModelScope.launch {
            repository.placeBid(playerIndex, bid)
        }
    }

    /* ================= PLAYING ================= */

    fun playCard(playerIndex: Int, card: Card) {
        viewModelScope.launch {
            repository.playCard(playerIndex, card)
        }
    }

    /* ================= ERROR ================= */

    fun clearError() {
        repository.clearError()
    }
}
