package com.tasalicool.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasalicool.game.model.GameState
import com.tasalicool.game.viewmodel.GameViewModel

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    game: GameState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Playing Phase",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Current Player: ${game.currentPlayer}",
            style = MaterialTheme.typography.titleMedium
        )

        Divider()

        Text(
            text = "Your Cards",
            style = MaterialTheme.typography.titleMedium
        )

        game.currentPlayerCards.forEach { card ->
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.playCard(card)
                }
            ) {
                Text(card.displayName)
            }
        }
    }
}
