package com.tasalicool.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasalicool.game.model.Game

/**
 * GameTable - طاولة اللعبة
 */
@Composable
fun GameTable(game: Game) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {

        // Dealer Button
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF8B0000), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🎴", fontSize = 40.sp)
        }

        // Current Trick Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy((-20).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            game.getCurrentTrick()?.cards?.forEach { (_, card) ->
                Text(
                    text = card.toString(),
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}
