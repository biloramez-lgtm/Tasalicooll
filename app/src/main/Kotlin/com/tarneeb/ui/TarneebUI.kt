package com.tarneeb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =================== ألوان افتراضية لتجنب TarneebColors ===================
object DefaultColors {
    val Background = Color(0xFFF5F5F5)
    val Primary = Color(0xFF6200EE)
    val Surface = Color.White
    val TextPrimary = Color.Black
    val TextSecondary = Color.DarkGray
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFF44336)
    val CardRed = Color(0xFFF44336)
    val CardBlack = Color(0xFF212121)
    val White = Color.White
}

// =================== COMPONENTS ===================
@Composable
fun HomeScreen(
    onSinglePlayerClick: () -> Unit,
    onMultiplayerClick: () -> Unit,
    onNetworkClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎴 لعبة Tarneeb",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = DefaultColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "اختر طريقة اللعب",
            fontSize = 20.sp,
            color = DefaultColors.TextSecondary,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        MenuButton("🤖", "لعبة فردية", "ضد الكمبيوتر", onSinglePlayerClick)
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton("👥", "لعبة محلية", "على نفس الجهاز", onMultiplayerClick)
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton("🌐", "لعبة أونلاين", "عبر الإنترنت", onNetworkClick)
    }
}

@Composable
fun MenuButton(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DefaultColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 40.sp, modifier = Modifier.padding(end = 16.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DefaultColors.TextPrimary)
                Text(subtitle, fontSize = 14.sp, color = DefaultColors.TextSecondary)
            }
        }
    }
}

@Composable
fun SinglePlayerSetupScreen(onStart: (String) -> Unit, onBack: () -> Unit) {
    var playerName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = DefaultColors.Primary) }
        }

        Text("لعبة فردية", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DefaultColors.Primary, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("اسمك") },
            modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DefaultColors.TextPrimary,
                unfocusedTextColor = DefaultColors.TextSecondary
            )
        )

        Button(
            onClick = { if (playerName.isNotEmpty()) onStart(playerName) },
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp),
            enabled = playerName.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = DefaultColors.Primary, disabledContainerColor = DefaultColors.TextSecondary)
        ) { Text("ابدأ اللعبة", fontSize = 18.sp) }
    }
}

@Composable
fun CenterText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 24.sp, color = DefaultColors.TextPrimary)
    }
}

// =================== MULTIPLAYER SETUP ===================
@Composable
fun MultiplayerSetupScreen(onStart: (List<String>) -> Unit, onBack: () -> Unit) {
    var playerCount by remember { mutableStateOf(2) }
    var playerNames by remember { mutableStateOf(List(4) { "" }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultColors.Background)
            .padding(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = DefaultColors.Primary) }
        }

        Text("لعبة محلية", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DefaultColors.Primary, modifier = Modifier.padding(bottom = 24.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("عدد اللاعبين", fontSize = 18.sp, color = DefaultColors.TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    (2..4).forEach { count ->
                        FilterChip(
                            selected = playerCount == count,
                            onClick = { playerCount = count },
                            label = { Text("$count لاعبين") }
                        )
                    }
                }
            }
            items(playerCount) { index ->
                OutlinedTextField(
                    value = playerNames[index],
                    onValueChange = { playerNames = playerNames.toMutableList().apply { this[index] = it } },
                    label = { Text("لاعب ${index + 1}") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DefaultColors.TextPrimary,
                        unfocusedTextColor = DefaultColors.TextSecondary
                    )
                )
            }
        }

        Button(
            onClick = { val valid = playerNames.take(playerCount); if (valid.size == playerCount) onStart(valid) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = playerNames.take(playerCount).all { it.isNotEmpty() },
            colors = ButtonDefaults.buttonColors(containerColor = DefaultColors.Primary)
        ) { Text("ابدأ اللعبة", fontSize = 18.sp) }
    }
}

// =================== NETWORK SETUP ===================
@Composable
fun NetworkSetupScreen(onJoinGame: (String, String) -> Unit, onCreateGame: (String) -> Unit, onBack: () -> Unit) {
    var playerName by remember { mutableStateOf("") }
    var gameCode by remember { mutableStateOf("") }
    var showJoinForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultColors.Background)
            .padding(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = DefaultColors.Primary) }
        }

        Text("لعبة أونلاين", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DefaultColors.Primary, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("اسمك") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DefaultColors.TextPrimary,
                unfocusedTextColor = DefaultColors.TextSecondary
            )
        )

        if (!showJoinForm) {
            Button(
                onClick = { if (playerName.isNotEmpty()) onCreateGame(playerName) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = playerName.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DefaultColors.Success)
            ) { Text("إنشاء لعبة جديدة", fontSize = 18.sp) }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showJoinForm = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DefaultColors.Primary)
            ) { Text("الانضمام لعبة موجودة", fontSize = 18.sp) }
        } else {
            OutlinedTextField(
                value = gameCode,
                onValueChange = { gameCode = it },
                label = { Text("كود اللعبة") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DefaultColors.TextPrimary,
                    unfocusedTextColor = DefaultColors.TextSecondary
                )
            )

            Button(
                onClick = { if (playerName.isNotEmpty() && gameCode.isNotEmpty()) onJoinGame(gameCode, playerName) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = playerName.isNotEmpty() && gameCode.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DefaultColors.Primary)
            ) { Text("انضم", fontSize = 18.sp) }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showJoinForm = false },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DefaultColors.Surface)
            ) { Text("رجوع", fontSize = 18.sp) }
        }
    }
}

// =================== GAME SCREEN ===================
@Composable
fun GameScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultColors.Background)
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = DefaultColors.Primary) }
        }

        CenterText("هنا ستظهر اللعبة (GameScreen)")
    }
}

// =================== CARD VIEW & CARD BUTTON ===================
enum class Suit(val symbol: String) {
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣"),
    SPADES("♠")
}

data class Card(val rank: String, val suit: Suit)

@Composable
fun CardView(card: Card) {
    Card(
        modifier = Modifier.width(60.dp).height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) DefaultColors.CardRed else DefaultColors.CardBlack
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(card.rank, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DefaultColors.White)
            Text(card.suit.symbol, fontSize = 14.sp, color = DefaultColors.White)
        }
    }
}

@Composable
fun CardButton(card: Card, isValid: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isValid,
        modifier = Modifier.width(60.dp).height(90.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) DefaultColors.CardRed else DefaultColors.CardBlack,
            disabledContainerColor = Color.Gray
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(card.rank, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DefaultColors.White)
            Text(card.suit.symbol, fontSize = 14.sp, color = DefaultColors.White)
        }
    }
}

// =================== DIFFICULTY BUTTON ===================
@Composable
fun DifficultyButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DefaultColors.Primary else DefaultColors.Surface
        )
    ) {
        Text(label, color = if (isSelected) DefaultColors.White else DefaultColors.TextPrimary)
    }
}

// =================== UTILITY COMPONENTS ===================
@Composable
fun CenterBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = DefaultColors.Error)
    ) {
        Text(text = message, color = DefaultColors.White, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
    }
}

// =================== PREVIEW ===================
@Composable
fun PreviewHome() {
    HomeScreen(
        onSinglePlayerClick = {},
        onMultiplayerClick = {},
        onNetworkClick = {}
    )
}
