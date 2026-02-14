package com.tasalicool.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onSinglePlayerClick: () -> Unit,
    onMultiplayerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Title
            Text(
                text = "TASALICOOL 400",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Single Player
            MenuButton(
                text = "Single Player",
                onClick = onSinglePlayerClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Multiplayer
            MenuButton(
                text = "Multiplayer",
                onClick = onMultiplayerClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Settings
            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
