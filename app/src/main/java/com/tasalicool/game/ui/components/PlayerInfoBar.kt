package com.tasalicool.game.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PlayerInfoBar - شريط معلومات اللاعب
 */
@Composable
fun PlayerInfoBar(
    name: String,
    score: String,
    tricksWon: String,
    isCurrent: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isCurrent) Color(0xFFFFB800) else Color(0xFF555555)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("👤", fontSize = 24.sp)
            }
        }
        
        Text(
            name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
        
        Text(
            "$score/$tricksWon",
            color = Color(0xFFFFB800),
            fontSize = 11.sp
        )
    }
}
