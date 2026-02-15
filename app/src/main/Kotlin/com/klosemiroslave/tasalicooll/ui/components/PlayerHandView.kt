package com.klosemiroslave.tasalicooll.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import com.klosemiroslave.tasalicooll.model.Player

@Composable
fun PlayerHandView(player: Player) {
    Row(modifier = androidx.compose.ui.Modifier.horizontalScroll(rememberScrollState())) {
        player.hand.forEach { card ->
            CardView(card)
        }
    }
}
