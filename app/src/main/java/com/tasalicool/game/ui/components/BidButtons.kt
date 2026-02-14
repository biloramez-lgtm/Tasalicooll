package com.tasalicool.game.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * BidButtons - أزرار البدية
 */
@Composable
fun BidButton(bid: Int, isEnabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.size(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color(0xFFFFC107) else Color(0xFF666666),
            disabledContainerColor = Color(0xFF555555)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            bid.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
