package com.klosemiroslave.tasalicooll.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klosemiroslave.tasalicooll.model.Player

@Composable
fun ScoreScreen(players: List<Player>) {
    Column(modifier = Modifier.padding(16.dp)) {
        players.forEach { player ->
            Text(text = "${player.name} - Score: ${player.score}")
        }
    }
}
