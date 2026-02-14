package com.tasalicool.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * BottomMainBar - شريط اللاعب في الأسفل
 */
@Composable
fun BottomMainBar(
    onSettings: () -> Unit,
    onChat: () -> Unit,
    onGift: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color(0xFF0d0d0d)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSettings) {
                Text("⚙️", fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = onChat) {
                Text("💬", fontSize = 24.sp)
            }
            
            IconButton(onClick = onGift) {
                Text("🎁", fontSize = 24.sp)
            }
        }
    }
}
