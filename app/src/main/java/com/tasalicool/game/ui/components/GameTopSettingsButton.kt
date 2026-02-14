package com.tasalicool.game.ui.components

import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

/**
 * GameTopSettingsButton - زر الإعدادات في الأعلى
 */
@Composable
fun GameTopSettingsButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Text(
            text = "⚙️",
            fontSize = 20.sp
        )
    }
}
