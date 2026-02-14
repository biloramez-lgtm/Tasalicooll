package com.tasalicool.game.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasalicool.game.model.*
import com.tasalicool.game.ui.theme.*

/**
 * GamePlayScreen - شاشة لعب الأوراق
 * 
 * تعرض:
 * ✅ طاولة اللعبة مع الأوراق الملعوبة
 * ✅ معلومات اللاعبين حول الطاولة
 * ✅ معاينة حية للخدعة الحالية
 * ✅ يد اللاعب الحالي (قابلة للاختيار)
 * ✅ الأوراق الصحيحة فقط
 * ✅ معلومات حالية (رقم الخدعة، البديات، الخدعات)
 * ✅ عداد الوقت (اختياري)
 */
@Composable
fun GamePlayScreen(
    game: Game,
    currentPlayer: Player,
    validCards: List<Card>,
    onCardPlay: (Card) -> Unit,
    onConcede: () -> Unit
) {
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGreen)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==================== TOP INFO BAR ====================
            GamePlayTopBar(game, currentPlayer)
            
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
            }
            
            // ==================== BOTTOM PLAYING INTERFACE ====================
            GamePlayBottomInterface(
                currentPlayer = currentPlayer,
                validCards = validCards,
                selectedCard = selectedCard,
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
private fun GamePlayTopBar(game: Game, currentPlayer: Player) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = BackgroundBlack,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left - Game Info
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Round ${game.currentRound}",
                    fontSize = 11.sp,
                    color = TextGray
                )
                Text(
                    "Trick ${game.currentTrick}/13",
                    fontSize = 14.sp,
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Center - Bid Info
            Box(
                modifier = Modifier
                    .background(BackgroundGreen, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "Bid: ${currentPlayer.bid} | Won: ${currentPlayer.tricksWon}",
                    fontSize = 12.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Right - Status
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (game.currentPlayerIndex == game.players.indexOf(currentPlayer))
                            SecondaryGold
                        else
                            Color(0xFF555555)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (game.currentPlayerIndex == game.players.indexOf(currentPlayer))
                        "●" else "◯",
                    fontSize = 16.sp,
                    color = if (game.currentPlayerIndex == game.players.indexOf(currentPlayer))
                        Color.Black else TextGray
                )
            }
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
        // Dealer Button
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(PrimaryRed),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎴", fontSize = 40.sp)
                Text(
                    "${game.currentTrick}/13",
                    fontSize = 12.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Current Trick Cards
        val trick = game.getCurrentTrick()
        if (trick != null && trick.cards.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.spacedBy((-24).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trick.cards.forEach { (playerId, card) ->
                    CardInPlay(card, playerId, game)
                }
            }
        }
    }
}

@Composable
private fun CardInPlay(card: Card, playerId: Int, game: Game) {
    val player = game.players.getOrNull(playerId)
    val isCurrentPlayer = game.currentPlayerIndex == playerId
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Card
        Surface(
            modifier = Modifier
                .width(60.dp)
                .height(90.dp),
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
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
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .background(
                        if (isCurrentPlayer) SecondaryGold else BackgroundBlack,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    player.name.take(4),
                    fontSize = 9.sp,
                    color = if (isCurrentPlayer) Color.Black else TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== PLAYERS PLAYING LAYOUT ====================

@Composable
private fun PlayersPlayingLayout(game: Game, currentPlayer: Player) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Find indices
        val players = game.players
        val currentIndex = players.indexOf(currentPlayer)
        
        // Opposite Player
        if (currentIndex >= 0) {
            val oppositeIndex = (currentIndex + 2) % 4
            PlayerPlayingPosition(
                player = players[oppositeIndex],
                position = "TOP",
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
                position = "LEFT",
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
                position = "RIGHT",
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
    position: String,
    isCurrent: Boolean,
    bid: Int,
    tricks: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrent) SecondaryGold else Color(0xFF333333),
        label = "playerBgColor"
    )
    
    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Avatar with Status
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 32.sp)
            
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
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
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
        
        // Bid & Tricks
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bid
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
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
                    .clip(CircleShape)
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
    onCardSelected: (Card) -> Unit,
    onConcede: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        color = BackgroundBlack,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Instruction Text
            Text(
                "Select a card to play",
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Normal
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
                        onClick = { onCardSelected(card) }
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConcede,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF666666)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Concede",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayableCard(
    card: Card,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateDpAsState(
        targetValue = if (isSelected) 76.dp else 68.dp,
        label = "cardScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 4.dp,
        label = "cardElevation"
    )
    
    Surface(
        modifier = Modifier
            .width(54.dp)
            .height(scale)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = Color.White,
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
                Text(
                    "✓",
                    fontSize = 10.sp,
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
        currentPlayerIndex = 0
    )
    
    TasalicoolTheme {
        GamePlayScreen(
            game = game,
            currentPlayer = game.players[0],
            validCards = game.players[0].hand.take(5),
            onCardPlay = {},
            onConcede = {}
        )
    }
}
