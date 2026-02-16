package com.tarneeb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarneeb.engine.*
import com.tarneeb.network.NetworkManager
import com.tarneeb.network.NetworkGameState
import com.tarneeb.app.*

// ============================================================================
// HOME SCREEN
// ============================================================================

@Composable
fun HomeScreen(
    onSinglePlayerClick: () -> Unit,
    onMultiplayerClick: () -> Unit,
    onNetworkClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TarneebColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎴 لعبة Tarneeb",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "اختر طريقة اللعب",
            fontSize = 20.sp,
            color = TarneebColors.TextSecondary,
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
        colors = CardDefaults.cardColors(containerColor = TarneebColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 40.sp, modifier = Modifier.padding(end = 16.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TarneebColors.TextPrimary)
                Text(subtitle, fontSize = 14.sp, color = TarneebColors.TextSecondary)
            }
        }
    }
}

// ============================================================================
// SINGLE PLAYER SETUP
// ============================================================================

@Composable
fun SinglePlayerSetupScreen(onStart: (String, AIDifficulty) -> Unit, onBack: () -> Unit) {
    var playerName by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TarneebColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TarneebColors.Primary) }
        }

        Text("لعبة فردية", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TarneebColors.Primary, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("اسمك") },
            modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TarneebColors.TextPrimary,
                unfocusedTextColor = TarneebColors.TextSecondary
            )
        )

        Text("مستوى الصعوبة", fontSize = 18.sp, color = TarneebColors.TextPrimary, modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 12.dp))

        Row(modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 32.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            AIDifficulty.values().forEach { diff ->
                DifficultyButton(diff, diff == selectedDifficulty) { selectedDifficulty = diff }
            }
        }

        Button(
            onClick = { if (playerName.isNotEmpty()) onStart(playerName, selectedDifficulty) },
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp),
            enabled = playerName.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = TarneebColors.Primary, disabledContainerColor = TarneebColors.TextSecondary)
        ) { Text("ابدأ اللعبة", fontSize = 18.sp) }
    }
}

@Composable
fun DifficultyButton(difficulty: AIDifficulty, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) TarneebColors.Primary else TarneebColors.Surface)
    ) {
        Text(
            text = when (difficulty) {
                AIDifficulty.EASY -> "سهل"
                AIDifficulty.MEDIUM -> "متوسط"
                AIDifficulty.HARD -> "صعب"
            }
        )
    }
}

// ============================================================================
// MULTIPLAYER SETUP
// ============================================================================

@Composable
fun MultiplayerSetupScreen(onStart: (List<String>, AIDifficulty) -> Unit, onBack: () -> Unit) {
    var playerCount by remember { mutableStateOf(2) }
    var playerNames by remember { mutableStateOf(List(4) { "" }) }
    var selectedDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }

    Column(modifier = Modifier.fillMaxSize().background(TarneebColors.Background).padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TarneebColors.Primary) }
        }

        Text("لعبة محلية", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TarneebColors.Primary, modifier = Modifier.padding(bottom = 24.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("عدد اللاعبين", fontSize = 18.sp, color = TarneebColors.TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    (2..4).forEach { count ->
                        FilterChip(selected = playerCount == count, onClick = { playerCount = count }, label = { Text("$count لاعبين") })
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
                        focusedTextColor = TarneebColors.TextPrimary,
                        unfocusedTextColor = TarneebColors.TextSecondary
                    )
                )
            }
            item {
                Text("مستوى الصعوبة (للـ AI)", fontSize = 18.sp, color = TarneebColors.TextPrimary, modifier = Modifier.padding(top = 16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    AIDifficulty.values().forEach { diff ->
                        DifficultyButton(diff, diff == selectedDifficulty) { selectedDifficulty = diff }
                    }
                }
            }
        }

        Button(
            onClick = { val valid = playerNames.take(playerCount).filter { it.isNotEmpty() }; if (valid.size == playerCount) onStart(valid, selectedDifficulty) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = playerNames.take(playerCount).all { it.isNotEmpty() },
            colors = ButtonDefaults.buttonColors(containerColor = TarneebColors.Primary)
        ) { Text("ابدأ اللعبة", fontSize = 18.sp) }
    }
}

// ============================================================================
// NETWORK SETUP
// ============================================================================

@Composable
fun NetworkSetupScreen(networkManager: NetworkManager, onJoinGame: (String, String) -> Unit, onCreateGame: (String) -> Unit, onBack: () -> Unit) {
    var playerName by remember { mutableStateOf("") }
    var gameCode by remember { mutableStateOf("") }
    var showJoinForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(TarneebColors.Background).padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TarneebColors.Primary) }
        }

        Text("لعبة أونلاين", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TarneebColors.Primary, modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("اسمك") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TarneebColors.TextPrimary,
                unfocusedTextColor = TarneebColors.TextSecondary
            )
        )

        if (!showJoinForm) {
            Button(onClick = { if (playerName.isNotEmpty()) onCreateGame(playerName) }, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = playerName.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = TarneebColors.Success)) { Text("إنشاء لعبة جديدة", fontSize = 18.sp) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showJoinForm = true }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = TarneebColors.Primary)) { Text("الانضمام لعبة موجودة", fontSize = 18.sp) }
        } else {
            OutlinedTextField(value = gameCode, onValueChange = { gameCode = it }, label = { Text("كود اللعبة") }, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TarneebColors.TextPrimary, unfocusedTextColor = TarneebColors.TextSecondary))
            Button(onClick = { if (playerName.isNotEmpty() && gameCode.isNotEmpty()) onJoinGame(gameCode, playerName) }, modifier = Modifier.fillMaxWidth().height(50.dp), enabled = playerName.isNotEmpty() && gameCode.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = TarneebColors.Primary)) { Text("انضم", fontSize = 18.sp) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showJoinForm = false }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = TarneebColors.Surface)) { Text("رجوع", fontSize = 18.sp) }
        }
    }
}

// ============================================================================
// GAME SCREEN
// ============================================================================

@Composable
fun GameScreen(engine: EngineGod, gameState: TarneebGame?, error: String?, aiAction: AIAction?, onBack: () -> Unit) {
    if (gameState == null) {
        CenterText("جارٍ التحضير...")
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(TarneebColors.Background).padding(8.dp)) {
        GameHeader(gameState, onBack)

        when (gameState.gamePhase) {
            GamePhase.BIDDING -> BiddingPhaseUI(engine, gameState, aiAction)
            GamePhase.PLAYING -> PlayingPhaseUI(engine, gameState, aiAction)
            GamePhase.ROUND_END -> RoundEndPhaseUI(gameState, engine)
            GamePhase.GAME_END -> GameEndPhaseUI(gameState)
            else -> CenterText("جارٍ التحضير...")
        }

        error?.let {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp), colors = CardDefaults.cardColors(containerColor = TarneebColors.Error)) {
                Text(text = it, color = TarneebColors.White, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
            }
        }
    }
}

// ============================================================================
// PLAYING & BIDDING PHASES
// ============================================================================

@Composable
fun BiddingPhaseUI(engine: EngineGod, gameState: TarneebGame, aiAction: AIAction?) {
    gameState.currentPlayer?.let { currentPlayer ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("دور ${currentPlayer.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TarneebColors.TextPrimary, modifier = Modifier.padding(bottom = 24.dp))

            if (currentPlayer.isAI) {
                CircularProgressIndicator(color = TarneebColors.Primary)
                Text("الـ AI يختار البدية...", color = TarneebColors.TextSecondary, modifier = Modifier.padding(top = 16.dp))
                (aiAction as? AIAction.PlacingBid)?.let { Text("📢 بدية: ${it.bid}", color = TarneebColors.Secondary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp)) }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((2..13).toList()) { bid ->
                        Button(onClick = { engine.placeBid(currentPlayer.id, bid) }, modifier = Modifier.height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = TarneebColors.Primary)) {
                            Text(bid.toString())
                        }
                    }
                }
            }
        }
    } ?: CenterText("جارٍ التحضير...")
}

@Composable
fun PlayingPhaseUI(engine: EngineGod, gameState: TarneebGame, aiAction: AIAction?) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        gameState.currentTrick?.let { currentTrick ->
            Text("الخدعة ${gameState.currentTrickNumber}/13", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TarneebColors.Primary, modifier = Modifier.padding(bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                currentTrick.cardsPlayed.forEach { (_, card) -> CardView(card) }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        gameState.currentPlayer?.let { currentPlayer ->
            Text("دور: ${currentPlayer.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TarneebColors.TextPrimary, modifier = Modifier.padding(bottom = 12.dp))

            if (currentPlayer.isAI) {
                CircularProgressIndicator(color = TarneebColors.Primary)
                Text("الـ AI يفكر...", color = TarneebColors.TextSecondary, modifier = Modifier.padding(top = 12.dp))
                (aiAction as? AIAction.PlayingCard)?.let { Text("🃏 ${it.card}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TarneebColors.Secondary, modifier = Modifier.padding(top = 12.dp)) }
            } else {
                val validCards = engine.getValidCards(currentPlayer.id)
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(currentPlayer.hand) { card ->
                        CardButton(card, isValid = validCards.contains(card)) { engine.playCard(currentPlayer.id, card) }
                    }
                }
            }
        } ?: CenterText("جارٍ التحضير...")
    }
}

// ============================================================================
// UTILITY COMPONENTS (CardView, CardButton, ScoreCard, etc.)
// ============================================================================

@Composable
fun CardView(card: Card) {
    Card(modifier = Modifier.width(60.dp).height(90.dp), colors = CardDefaults.cardColors(containerColor = if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) TarneebColors.CardRed else TarneebColors.CardBlack), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(card.rank.display, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(card.suit.symbol, fontSize = 14.sp)
        }
    }
}

@Composable
fun CardButton(card: Card, isValid: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = isValid, modifier = Modifier.width(60.dp).height(90.dp), colors = ButtonDefaults.buttonColors(containerColor = if (card.suit == Suit.HE
