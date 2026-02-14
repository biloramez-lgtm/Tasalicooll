package com.tasalicool.game.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasalicool.game.model.GameState
import com.tasalicool.game.viewmodel.GameViewModel

@Composable
fun BiddingScreen(
    viewModel: GameViewModel,
    game: GameState
) {
    var bidValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Bidding Phase",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Current Player: ${game.currentPlayer}",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = bidValue,
            onValueChange = { bidValue = it },
            label = { Text("Enter your bid") },
            singleLine = true
        )

        Button(
            enabled = bidValue.toIntOrNull() != null,
            onClick = {
                viewModel.placeBid(bidValue.toInt())
                bidValue = ""
            }
        ) {
            Text("Submit Bid")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Bids so far:",
            style = MaterialTheme.typography.titleMedium
        )

        game.bids.forEach { (player, bid) ->
            Text("Player $player → $bid")
        }
    }
}
