package com.tasalicool.game.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasalicool.game.model.*
import com.tasalicool.game.ui.theme.*
import kotlinx.coroutines.delay

/**
 * GamePlayScreen - شاشة لعب الأوراق الكاملة الاحترافية
 * 
 * الميزات:
 * ✅ طاولة لعبة مع الأوراق الملعوبة
 * ✅ 3 لاعبين حول الطاولة
 * ✅ معاينة حية للخدعة
 * ✅ يد اللاعب بجميع الأوراق الصحيحة
 * ✅ تأثيرات انتقالية سلسة
 * ✅ مؤشر الدور الحالي
 * ✅ عرض البديات والخدعات
 * ✅ أزرار الإجراءات
 * ✅ معلومات حالية دقيقة
 */
@Composable
fun GamePlayScreen(
    game: Game,
    currentPlayer: Player,
    validCards: List<Card>,
    onCardPlay: (Card) -> Unit,
    onConcede: () -> Unit,
    isCurrentTurn: Boolean = true
) {
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    var playAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedCard) {
        if (selectedCard != null) {
            delay(100)
            playAnimation = true
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGreen)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==================== TOP INFO BAR ====================
            GamePlayTopBar(game, currentPlayer, isCurrentTurn)
            
            // ==================== SCORES DISPLAY ====================
            ScoresBar(game)
            
            // ==================== MAIN PLAYING AREA ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Game Table with Trick
                GamePlayTable(game, selectedCard)
                
                // Players Around Table
                PlayersPlayingLayout(game, currentPlayer)
                
                // Current Turn Indicator
                if (isCurrentTurn) {
                    CurrentTurnIndicator()
                }
            }
            
            // ==================== BOTTOM PLAYING INTERFACE ====================
            GamePlayBottomInterface(
                currentPlayer = currentPlayer,
                validCards = validCards,
                selectedCard = selectedCard,
                isCurrentTurn = isCurrentTurn,
                onCardSelected = { card ->
                    selectedCard = card
                    onCardPlay(card)
                },
                onConcede = onConcede
            )
        }
    }
}

// ==================== TOP INFO BAR ====================

@Composable
private fun GamePlayTopBar(
    game: Game,
    currentPlayer: Player,
    isCurrentTurn: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = BackgroundBlack,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Section - Player Info
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    currentPlayer.name,
                    color = if (isCurrentTurn) SecondaryGold else TextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isCurrentTurn) SuccessGreen else ErrorRed)
                    )
                    Text(
                        if (isCurrentTurn) "Your Turn" else "Waiting",
                        color = if (isCurrentTurn) SuccessGreen else TextGray,
                        fontSize = 11.sp
                    )
                }
            }
            
            // Center Section - Game Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Round ${game.currentRound}",
                    color = TextGray,
                    fontSize = 10.sp
                )
                Text(
                    "Trick ${game.currentTrick}/13",
                    color = SecondaryGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Right Section - Quick Stats
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bid Badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "B",
                                fontSize = 8.sp,
                                color = TextGray
                            )
                            Text(
                                "${currentPlayer.bid}",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    
                    // Tricks Badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BackgroundGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Alignment.Center
                        ) {
                            Text(
                                "T",
                                fontSize = 8.sp,
                                color = TextGray
                            )
                            Text(
                                "${currentPlayer.tricksWon}",
                                fontSize = 11.sp,
                                color = SecondaryGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SCORES BAR ====================

@Composable
private fun ScoresBar(game: Game) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        color = BackgroundBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team 1 Score
            ScoreItem(
                teamName = "Team 1",
                score = game.team1.score,
                modifier = Modifier.weight(1f)
            )
            
            // Divider
            Divider(
                color = BorderGray,
                modifier = Modifier
                    .fillMaxHeight(0.6f)
                    .width(1.dp)
            )
            
            // VS Text
            Text(
                "VS",
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Divider
            Divider(
                color = BorderGray,
                modifier = Modifier
                    .fillMaxHeight(0.6f)
                    .width(1.dp)
            )
            
            // Team 2 Score
            ScoreItem(
                teamName = "Team 2",
                score = game.team2.score,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScoreItem(
    teamName: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            teamName,
            fontSize = 10.sp,
            color = TextGray
        )
        Text(
            "$score",
            fontSize = 20.sp,
            color = SecondaryGold,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== CURRENT TURN INDICATOR ====================

@Composable
private fun CurrentTurnIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("🎯", fontSize = 32.sp)
            Text(
                "Your Turn",
                fontSize = 12.sp,
                color = SecondaryGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== GAME PLAY TABLE ====================

@Composable
private fun GamePlayTable(game: Game, selectedCard: Card?) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Dealer Button (موزع)
        Box(
            modifier = Modifier
                .size(95.dp)
                .clip(CircleShape)
                .background(PrimaryRed)
                .shadow(12.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("🎴", fontSize = 48.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${game.currentTrick}/13",
                        fontSize = 10.sp,
                        color = PrimaryRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        
        // Current Trick Cards
        val trick = game.getCurrentTrick()
        if (trick != null && trick.cards.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(horizontal = 50.dp),
                horizontalArrangement = Arrangement.spacedBy((-28).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trick.playOrder.forEach { playerId ->
                    val card = trick.cards[playerId]
                    if (card != null) {
                        CardInPlay(card, playerId, game, selectedCard)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardInPlay(
    card: Card,
    playerId: Int,
    game: Game,
    selectedCard: Card?
) {
    val player = game.players.getOrNull(playerId)
    val isCurrentPlayer = game.currentPlayerIndex == playerId
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Card
        Surface(
            modifier = Modifier
                .width(62.dp)
                .height(94.dp),
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    card.rank.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (card.suit.isRed()) Color.Red else Color.Black
                )
                Text(
                    card.suit.getSymbol(),
                    fontSize = 16.sp,
                    color = if (card.suit.isRed()) Color.Red else Color.Black
                )
            }
        }
        
        // Player Badge
        if (player != null) {
            Surface(
                modifier = Modifier.height(22.dp),
                color = if (isCurrentPlayer) SecondaryGold else BackgroundBlack,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    player.name.take(5),
                    fontSize = 9.sp,
                    color = if (isCurrentPlayer) Color.Black else TextWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp),
                    maxLines = 1
                )
            }
        }
    }
}

// ==================== PLAYERS PLAYING LAYOUT ====================

@Composable
private fun PlayersPlayingLayout(game: Game, currentPlayer: Player) {
    Box(modifier = Modifier.fillMaxSize()) {
        val players = game.players
        val currentIndex = players.indexOf(currentPlayer)
        
        // Top Player (Opposite)
        if (currentIndex >= 0) {
            val oppositeIndex = (currentIndex + 2) % 4
            PlayerPlayingPosition(
                player = players[oppositeIndex],
                isCurrent = game.currentPlayerIndex == oppositeIndex,
                bid = players[oppositeIndex].bid,
                tricks = players[oppositeIndex].tricksWon,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        
        // Left Player
        if (currentIndex >= 0) {
            val leftIndex = (currentIndex + 1) % 4
            PlayerPlayingPosition(
                player = players[leftIndex],
                isCurrent = game.currentPlayerIndex == leftIndex,
                bid = players[leftIndex].bid,
                tricks = players[leftIndex].tricksWon,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        
        // Right Player
        if (currentIndex >= 0) {
            val rightIndex = (currentIndex + 3) % 4
            PlayerPlayingPosition(
                player = players[rightIndex],
                isCurrent = game.currentPlayerIndex == rightIndex,
                bid = players[rightIndex].bid,
                tricks = players[rightIndex].tricksWon,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun PlayerPlayingPosition(
    player: Player,
    isCurrent: Boolean,
    bid: Int,
    tricks: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrent) SecondaryGold else Color(0xFF444444),
        label = "playerBgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isCurrent) SecondaryGold else Color.Transparent,
        label = "playerBorderColor"
    )
    
    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Avatar with Status
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .shadow(if (isCurrent) 8.dp else 2.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 32.sp)
            
            // Status Indicator
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
            }
        }
        
        // Name
        Text(
            player.name,
            fontSize = 12.sp,
            color = if (isCurrent) SecondaryGold else TextWhite,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
        
        // Bid & Tricks Display
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bid
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BackgroundBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$bid",
                    fontSize = 10.sp,
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Slash
            Text("/", fontSize = 10.sp, color = TextGray)
            
            // Tricks
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BackgroundBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$tricks",
                    fontSize = 10.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== BOTTOM PLAYING INTERFACE ====================

@Composable
private fun GamePlayBottomInterface(
    currentPlayer: Player,
    validCards: List<Card>,
    selectedCard: Card?,
    isCurrentTurn: Boolean,
    onCardSelected: (Card) -> Unit,
    onConcede: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        color = BackgroundBlack,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Instruction Text
            Text(
                if (isCurrentTurn) "Select a card to play" else "Waiting for your turn",
                fontSize = 13.sp,
                color = if (isCurrentTurn) TextWhite else TextGray,
                fontWeight = FontWeight.Bold
            )
            
            // Valid Cards Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy((-14).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                validCards.forEach { card ->
                    PlayableCard(
                        card = card,
                        isSelected = card == selectedCard,
                        isEnabled = isCurrentTurn,
                        onClick = { 
                            if (isCurrentTurn) {
                                onCardSelected(card)
                            }
                        }
                    )
                }
            }
            
            // Action Button
            Button(
                onClick = onConcede,
                enabled = isCurrentTurn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    disabledContainerColor = Color(0xFF555555)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Concede Game",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PlayableCard(
    card: Card,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateDpAsState(
        targetValue = if (isSelected) 78.dp else 70.dp,
        animationSpec = tween(200),
        label = "cardScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 14.dp else 4.dp,
        label = "cardElevation"
    )
    
    val bgColor by animateColorAsState(
        targetValue = if (isEnabled) Color.White else Color(0xFFDDDDDD),
        label = "cardBg"
    )
    
    Surface(
        modifier = Modifier
            .width(56.dp)
            .height(scale)
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = isEnabled, onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                card.rank.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (card.suit.isRed()) Color.Red else Color.Black
            )
            Text(
                card.suit.getSymbol(),
                fontSize = 11.sp,
                color = if (card.suit.isRed()) Color.Red else Color.Black
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "✓",
                    fontSize = 9.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== PREVIEW ====================

@Composable
fun GamePlayScreenPreview() {
    val deck = Card.createGameDeck()
    val game = Game(
        team1 = Team(
            id = 1,
            name = "Team 1",
            player1 = Player(id = 0, name = "You", bid = 7, tricksWon = 3),
            player2 = Player(id = 2, name = "Partner", bid = 6, tricksWon = 2)
        ),
        team2 = Team(
            id = 2,
            name = "Team 2",
            player1 = Player(id = 1, name = "Opponent 1", bid = 5, tricksWon = 2),
            player2 = Player(id = 3, name = "Opponent 2", bid = 8, tricksWon = 3)
        ),
        players = listOf(
            Player(id = 0, name = "You", hand = deck.take(13), bid = 7, tricksWon = 3),
            Player(id = 1, name = "Opponent 1", bid = 5, tricksWon = 2),
            Player(id = 2, name = "Partner", bid = 6, tricksWon = 2),
            Player(id = 3, name = "Opponent 2", bid = 8, tricksWon = 3)
        ),
        currentPlayerIndex = 0,
        currentTrick = 5
    )
    
    TasalicoolTheme {
        GamePlayScreen(
            game = game,
            currentPlayer = game.players[0],
            validCards = game.players[0].hand.take(5),
            onCardPlay = {},
            onConcede = {},
            isCurrentTurn = true
        )
    }
}
