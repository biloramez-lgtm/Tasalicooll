package com.tarneeb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

class TarneebMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // أبسط كود ممكن
        setContent {
            // ثيم بسيط
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // نص بسيط
                        Text(
                            text = "🎮 Tarneeb Game",
                            fontSize = 32.sp,
                            color = Color(0xFF1976D2)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // زر بسيط
                        Button(
                            onClick = { }
                        ) {
                            Text("بدء اللعبة")
                        }
                    }
                }
            }
        }
    }
}
