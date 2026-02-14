package com.tasalicool.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasalicool.game.model.GameState

@Composable
fun GameOverScreen(
    game: GameState,
    onPlayAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Game Over",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Winner: ${game.winningTeam}",
            style = MaterialTheme.typography.titleLarge
        )

        Divider()

        Text("Final Scores:")

        game.scores.forEach { (team, score) ->
            Text("$team → $score")
        }

        Button(
            modifier = Modifier.padding(top = 20.dp),
            onClick = onPlayAgain
        ) {
            Text("Play Again")
        }
    }
}
