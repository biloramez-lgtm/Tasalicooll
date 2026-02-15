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
// HOME SCREEN - شاشة البداية
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
        // العنوان
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
        
        // زر Single Player
        MenuButton(
            icon = "🤖",
            title = "لعبة فردية",
            subtitle = "ضد الكمبيوتر",
            onClick = onSinglePlayerClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // زر Multiplayer
        MenuButton(
            icon = "👥",
            title = "لعبة محلية",
            subtitle = "على نفس الجهاز",
            onClick = onMultiplayerClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // زر Network
        MenuButton(
            icon = "🌐",
            title = "لعبة أونلاين",
            subtitle = "عبر الإنترنت",
            onClick = onNetworkClick
        )
    }
}

@Composable
fun MenuButton(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = TarneebColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 40.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TarneebColors.TextPrimary
                )
                
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TarneebColors.TextSecondary
                )
            }
        }
    }
}

// ============================================================================
// SINGLE PLAYER SETUP SCREEN
// ============================================================================

@Composable
fun SinglePlayerSetupScreen(
    onStart: (String, AIDifficulty) -> Unit,
    onBack: () -> Unit
) {
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
        // الرجوع
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TarneebColors.Primary
                )
            }
        }
        
        // العنوان
        Text(
            text = "لعبة فردية",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // اسم اللاعب
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("اسمك") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TarneebColors.TextPrimary,
                unfocusedTextColor = TarneebColors.TextSecondary
            )
        )
        
        // مستوى الصعوبة
        Text(
            text = "مستوى الصعوبة",
            fontSize = 18.sp,
            color = TarneebColors.TextPrimary,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AIDifficulty.values().forEach { difficulty ->
                DifficultyButton(
                    difficulty = difficulty,
                    isSelected = difficulty == selectedDifficulty,
                    onClick = { selectedDifficulty = difficulty }
                )
            }
        }
        
        // زر البدء
        Button(
            onClick = {
                if (playerName.isNotEmpty()) {
                    onStart(playerName, selectedDifficulty)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp),
            enabled = playerName.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TarneebColors.Primary,
                disabledContainerColor = TarneebColors.TextSecondary
            )
        ) {
            Text("ابدأ اللعبة", fontSize = 18.sp)
        }
    }
}

@Composable
fun DifficultyButton(
    difficulty: AIDifficulty,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) TarneebColors.Primary else TarneebColors.Surface
        )
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
// MULTIPLAYER SETUP SCREEN
// ============================================================================

@Composable
fun MultiplayerSetupScreen(
    onStart: (List<String>, AIDifficulty) -> Unit,
    onBack: () -> Unit
) {
    var playerCount by remember { mutableStateOf(2) }
    var playerNames by remember { mutableStateOf(List(4) { "" }) }
    var selectedDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TarneebColors.Background)
            .padding(24.dp)
    ) {
        // الرجوع
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TarneebColors.Primary
                )
            }
        }
        
        // العنوان
        Text(
            text = "لعبة محلية",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // عدد اللاعبين
            item {
                Text(
                    text = "عدد اللاعبين",
                    fontSize = 18.sp,
                    color = TarneebColors.TextPrimary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    (2..4).forEach { count ->
                        FilterChip(
                            selected = playerCount == count,
                            onClick = { playerCount = count },
                            label = { Text("$count لاعبين") }
                        )
                    }
                }
            }
            
            // أسماء اللاعبين
            items(playerCount) { index ->
                OutlinedTextField(
                    value = playerNames[index],
                    onValueChange = {
                        playerNames = playerNames.toMutableList().apply {
                            this[index] = it
                        }
                    },
                    label = { Text("لاعب ${index + 1}") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TarneebColors.TextPrimary,
                        unfocusedTextColor = TarneebColors.TextSecondary
                    )
                )
            }
            
            // مستوى الصعوبة
            item {
                Text(
                    text = "مستوى الصعوبة (للـ AI)",
                    fontSize = 18.sp,
                    color = TarneebColors.TextPrimary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    AIDifficulty.values().forEach { difficulty ->
                        DifficultyButton(
                            difficulty = difficulty,
                            isSelected = difficulty == selectedDifficulty,
                            onClick = { selectedDifficulty = difficulty }
                        )
                    }
                }
            }
        }
        
        // زر البدء
        Button(
            onClick = {
                val validNames = playerNames.take(playerCount).filter { it.isNotEmpty() }
                if (validNames.size == playerCount) {
                    onStart(validNames, selectedDifficulty)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = playerNames.take(playerCount).all { it.isNotEmpty() },
            colors = ButtonDefaults.buttonColors(
                containerColor = TarneebColors.Primary
            )
        ) {
            Text("ابدأ اللعبة", fontSize = 18.sp)
        }
    }
}

// ============================================================================
// NETWORK SETUP SCREEN
// ============================================================================

@Composable
fun NetworkSetupScreen(
    networkManager: NetworkManager,
    onJoinGame: (String, String) -> Unit,
    onCreateGame: (String) -> Unit,
    onBack: () -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var gameCode by remember { mutableStateOf("") }
    var showJoinForm by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TarneebColors.Background)
            .padding(24.dp)
    ) {
        // الرجوع
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TarneebColors.Primary
                )
            }
        }
        
        // العنوان
        Text(
            text = "لعبة أونلاين",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // اسم اللاعب
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("اسمك") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TarneebColors.TextPrimary,
                unfocusedTextColor = TarneebColors.TextSecondary
            )
        )
        
        if (!showJoinForm) {
            // إنشاء لعبة جديدة
            Button(
                onClick = {
                    if (playerName.isNotEmpty()) {
                        onCreateGame(playerName)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = playerName.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TarneebColors.Success
                )
            ) {
                Text("إنشاء لعبة جديدة", fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // الانضمام لعبة
            Button(
                onClick = { showJoinForm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TarneebColors.Primary
                )
            ) {
                Text("الانضمام لعبة موجودة", fontSize = 18.sp)
            }
        } else {
            // نموذج الانضمام
            OutlinedTextField(
                value = gameCode,
                onValueChange = { gameCode = it },
                label = { Text("كود اللعبة") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TarneebColors.TextPrimary,
                    unfocusedTextColor = TarneebColors.TextSecondary
                )
            )
            
            Button(
                onClick = {
                    if (playerName.isNotEmpty() && gameCode.isNotEmpty()) {
                        onJoinGame(gameCode, playerName)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = playerName.isNotEmpty() && gameCode.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TarneebColors.Primary
                )
            ) {
                Text("انضم", fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { showJoinForm = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TarneebColors.Surface
                )
            ) {
                Text("رجوع", fontSize = 18.sp)
            }
        }
    }
}

// ============================================================================
// GAME SCREEN - شاشة اللعب
// ============================================================================

@Composable
fun GameScreen(
    engine: EngineGod,
    gameState: TarneebGame,
    error: String?,
    aiAction: AIAction?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TarneebColors.Background)
            .padding(8.dp)
    ) {
        // رأس الشاشة
        GameHeader(gameState, onBack)
        
        // محتوى اللعبة
        when (gameState.gamePhase) {
            GamePhase.BIDDING -> BiddingPhaseUI(engine, gameState, aiAction)
            GamePhase.PLAYING -> PlayingPhaseUI(engine, gameState, aiAction)
            GamePhase.ROUND_END -> RoundEndPhaseUI(gameState, engine)
            GamePhase.GAME_END -> GameEndPhaseUI(gameState)
            else -> CenterText("جاري التحضير...")
        }
        
        // عرض الأخطاء
        if (error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TarneebColors.Error
                )
            ) {
                Text(
                    text = error,
                    color = TarneebColors.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun GameHeader(gameState: TarneebGame, onBack: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = TarneebColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "خروج",
                    tint = TarneebColors.Error
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${gameState.team1.name} - ${gameState.team2.name}",
                    fontSize = 14.sp,
                    color = TarneebColors.TextPrimary
                )
                Text(
                    text = "${gameState.team1.totalScore} : ${gameState.team2.totalScore}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TarneebColors.Primary
                )
            }
            
            Text(
                text = "الجولة ${gameState.currentRound}",
                fontSize = 12.sp,
                color = TarneebColors.TextSecondary
            )
        }
    }
}

@Composable
fun BiddingPhaseUI(engine: EngineGod, gameState: TarneebGame, aiAction: AIAction?) {
    val currentPlayer = gameState.currentPlayer
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (currentPlayer != null) {
            Text(
                text = "دور ${currentPlayer.name}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TarneebColors.TextPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            if (currentPlayer.isAI) {
                CircularProgressIndicator(color = TarneebColors.Primary)
                Text(
                    text = "الـ AI يختار البدية...",
                    color = TarneebColors.TextSecondary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                if (aiAction is AIAction.PlacingBid) {
                    Text(
                        text = "📢 بدية: ${aiAction.bid}",
                        color = TarneebColors.Secondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((2..13).toList()) { bid ->
                        Button(
                            onClick = { engine.placeBid(currentPlayer.id, bid) },
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TarneebColors.Primary
                            )
                        ) {
                            Text(bid.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayingPhaseUI(engine: EngineGod, gameState: TarneebGame, aiAction: AIAction?) {
    val currentPlayer = gameState.currentPlayer
    val currentTrick = gameState.currentTrick
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // معلومات الخدعة
        if (currentTrick != null) {
            Text(
                text = "الخدعة ${gameState.currentTrickNumber}/13",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TarneebColors.Primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // الأوراق الملعوبة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentTrick.cardsPlayed.forEach { (playerId, card) ->
                    CardView(card)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // دور اللاعب
        if (currentPlayer != null) {
            Text(
                text = "دور: ${currentPlayer.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TarneebColors.TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (currentPlayer.isAI) {
                CircularProgressIndicator(color = TarneebColors.Primary)
                Text(
                    text = "الـ AI يفكر...",
                    color = TarneebColors.TextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
                
                if (aiAction is AIAction.PlayingCard) {
                    Text(
                        text = "🃏 ${aiAction.card}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TarneebColors.Secondary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            } else {
                // أوراق اللاعب
                val validCards = engine.getValidCards(currentPlayer.id)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentPlayer.hand) { card ->
                        CardButton(
                            card = card,
                            isValid = validCards.contains(card),
                            onClick = { engine.playCard(currentPlayer.id, card) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardView(card: Card) {
    Card(
        modifier = Modifier
            .width(60.dp)
            .height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (card.suit) {
                Suit.HEARTS, Suit.DIAMONDS -> TarneebColors.CardRed
                else -> TarneebColors.CardBlack
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(card.rank.display, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(card.suit.symbol, fontSize = 14.sp)
        }
    }
}

@Composable
fun CardButton(card: Card, isValid: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isValid,
        modifier = Modifier
            .width(60.dp)
            .height(90.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when (card.suit) {
                Suit.HEARTS, Suit.DIAMONDS -> TarneebColors.CardRed
                else -> TarneebColors.CardBlack
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(card.rank.display, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(card.suit.symbol, fontSize = 12.sp)
        }
    }
}

@Composable
fun RoundEndPhaseUI(gameState: TarneebGame, engine: EngineGod) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "انتهت الجولة",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        ScoreCard(gameState.team1)
        Spacer(modifier = Modifier.height(16.dp))
        ScoreCard(gameState.team2)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { engine.nextRound() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TarneebColors.Primary
            )
        ) {
            Text("جولة جديدة", fontSize = 18.sp)
        }
    }
}

@Composable
fun GameEndPhaseUI(gameState: TarneebGame) {
    val winner = if (gameState.team1.isWinner) gameState.team1 else gameState.team2
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉 انتهت اللعبة!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Secondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "الفائز: ${winner.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "${winner.totalScore} نقطة",
            fontSize = 20.sp,
            color = TarneebColors.TextSecondary
        )
    }
}

@Composable
fun ScoreCard(team: Team) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TarneebColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(team.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("البدية: ${team.totalBid}", fontSize = 12.sp, color = TarneebColors.TextSecondary)
            }
            
            Text(
                text = "${team.totalScore}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TarneebColors.Primary
            )
        }
    }
}

// ============================================================================
// NETWORK GAME SCREEN
// ============================================================================

@Composable
fun NetworkGameScreen(
    networkManager: NetworkManager,
    networkState: NetworkGameState?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TarneebColors.Background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "لعبة أونلاين",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TarneebColors.Primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        if (networkState != null) {
            Text(
                text = "الحالة: ${networkState.status}",
                fontSize = 18.sp,
                color = TarneebColors.TextSecondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            if (networkState.gameCode != null) {
                Text(
                    text = "كود اللعبة: ${networkState.gameCode}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TarneebColors.Primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            if (networkState.players.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 24.dp)
                ) {
                    items(networkState.players) { player ->
                        Text(
                            text = "👤 $player",
                            fontSize = 14.sp,
                            color = TarneebColors.TextPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            CircularProgressIndicator(color = TarneebColors.Primary)
            Text(
                text = "جاري الاتصال...",
                color = TarneebColors.TextSecondary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TarneebColors.Error
            )
        ) {
            Text("قطع الاتصال", fontSize = 18.sp)
        }
    }
}

// ============================================================================
// UTILITY COMPOSABLES
// ============================================================================

@Composable
fun CenterText(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = TarneebColors.TextPrimary
        )
    }
}
