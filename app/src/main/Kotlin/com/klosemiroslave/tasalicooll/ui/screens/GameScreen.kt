package com.klosemiroslave.tasalicooll.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klosemiroslave.tasalicooll.model.Player
import com.klosemiroslave.tasalicooll.ui.components.PlayerHandView

@Composable
fun GameScreen(players: List<Player>, onPlayRound: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        players.forEach { player ->
            Text(text = "${player.name} - Tricks: ${player.tricksWon} - Score: ${player.score}")
            PlayerHandView(player)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onPlayRound, modifier = Modifier.fillMaxWidth()) {
            Text("Play Round")
        }
    }
}
