package com.klosemiroslave.tasalicooll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.klosemiroslave.tasalicooll.engine.GameEngine
import com.klosemiroslave.tasalicooll.model.Player
import com.klosemiroslave.tasalicooll.model.Team
import com.klosemiroslave.tasalicooll.ui.screens.GameScreen

class MainActivity : ComponentActivity() {

    lateinit var players: MutableList<Player>
    lateinit var teams: List<Team>
    lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // اللاعبين (AI يكمل العدد)
        players = mutableListOf(
            Player("Alice", teamId = 1),
            Player("Bob", teamId = 2),
            Player("AI Charlie", teamId = 1, isAI = true),
            Player("AI David", teamId = 2, isAI = true)
        )
        teams = listOf(
            Team(1, listOf(players[0], players[2])),
            Team(2, listOf(players[1], players[3]))
        )

        gameEngine = GameEngine(players, teams)

        setContent {
            GameScreen(players) {
                gameEngine.playRound()
            }
        }
    }
}
