package com.klosemiroslave.tasalicooll.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.klosemiroslave.tasalicooll.model.Card

@Composable
fun CardView(card: Card) {
    Box(
        modifier = Modifier
            .size(60.dp, 90.dp)
            .background(Color.White)
    ) {
        Text("${card.rank.name}\n${card.suit.name}")
    }
}
