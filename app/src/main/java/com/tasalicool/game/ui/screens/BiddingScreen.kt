package com.tasalicool.game.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.tasalicool.game.viewmodel.GameViewModel
import com.tasalicool.game.viewmodel.GameAction

/**
 * BiddingScreen - شاشة البدية الكاملة (MVI Version)
 */
@Composable
fun BiddingScreen(
    viewModel: GameViewModel,
    game: Game
) {
    val currentPlayer = game.players[game.currentPlayerIndex]
    val minimumBid = game.minimumBid
    val suggestedBid = game.suggestedBid

    var selectedBid by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGreen)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ==================== TOP SECTION ====================
            BiddingTopSection(game, currentPlayer)

            // ==================== MIDDLE SECTION ====================
            BiddingMiddleSection(game, minimumBid, suggestedBid) { bid ->
                selectedBid = bid

                viewModel.onAction(
                    GameAction.PlaceBid(
                        playerIndex = game.currentPlayerIndex,
                        bid = bid
                    )
                )
            }

            // ==================== BOTTOM SECTION ====================
            BiddingBottomSection(currentPlayer)
        }
    }
}

// ==================== TOP SECTION ====================

@Composable
private fun BiddingTopSection(game: Game, currentPlayer: Player) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreCard("Team 1", game.team1.score, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            ScoreCard("Team 2", game.team2.score, Modifier.weight(1f))
        }

        CurrentPlayerBiddingCard(currentPlayer)

        if (game.players.any { it.bid > 0 }) {
            OtherPlayersBidsDisplay(game, currentPlayer)
        }
    }
}

@Composable
private fun ScoreCard(teamName: String, score: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(60.dp),
        color = BackgroundGreen,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(teamName, fontSize = 11.sp, color = TextGray)
            Text("$score", fontSize = 24.sp, color = SecondaryGold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CurrentPlayerBiddingCard(player: Player) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        color = PrimaryRed,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 24.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Now Bidding", fontSize = 12.sp, color = Color.White)
                Text(player.name, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${player.hand.size}",
                    fontSize = 16.sp,
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OtherPlayersBidsDisplay(game: Game, currentPlayer: Player) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Other Players", fontSize = 12.sp, color = TextGray)

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            game.players
                .filter { it.id != currentPlayer.id && it.bid > 0 }
                .forEach { player ->
                    PlayerBidChip(player)
                }
        }
    }
}

@Composable
private fun PlayerBidChip(player: Player) {
    Surface(
        modifier = Modifier.height(36.dp),
        color = BackgroundGreen,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(player.name, fontSize = 11.sp, color = TextWhite)
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(SecondaryGold),
                contentAlignment = Alignment.Center
            ) {
                Text("${player.bid}", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==================== MIDDLE ====================

@Composable
private fun BiddingMiddleSection(
    game: Game,
    minimumBid: Int,
    suggestedBid: Int?,
    onBidSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Choose your order", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)

        MinimumBidInfo(minimumBid, suggestedBid)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(12) { index ->
                val bid = index + 2
                BidGridButton(
                    bid = bid,
                    isEnabled = bid >= minimumBid,
                    isSuggested = bid == suggestedBid,
                    onClick = { onBidSelected(bid) }
                )
            }
        }
    }
}

@Composable
private fun MinimumBidInfo(minimumBid: Int, suggestedBid: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth().background(BackgroundBlack, RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Minimum", fontSize = 10.sp, color = TextGray)
            Text("$minimumBid", fontSize = 16.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
        }

        if (suggestedBid != null) {
            Divider(color = BorderGray, modifier = Modifier.fillMaxHeight().width(1.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Suggested", fontSize = 10.sp, color = TextGray)
                Text("$suggestedBid", fontSize = 16.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BidGridButton(
    bid: Int,
    isEnabled: Boolean,
    isSuggested: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSuggested -> SecondaryGoldLight
            isEnabled -> ButtonYellow
            else -> Color(0xFF555555)
        }, label = ""
    )

    val elevation by animateDpAsState(
        targetValue = if (isSuggested) 8.dp else 2.dp, label = ""
    )

    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.size(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                bid.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) Color.Black else TextGray
            )
            if (isSuggested) {
                Text("★", fontSize = 10.sp, color = Color.Black)
            }
        }
    }
}

// ==================== BOTTOM ====================

@Composable
private fun BiddingBottomSection(currentPlayer: Player) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        color = BackgroundBlack,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Your Hand", fontSize = 12.sp, color = TextGray)

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy((-12).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentPlayer.hand.forEach { card ->
                    HandCardSmall(card)
                }
            }
        }
    }
}

@Composable
private fun HandCardSmall(card: Card) {
    Surface(
        modifier = Modifier.width(45.dp).height(68.dp),
        color = Color.White,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                card.rank.displayName,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (card.suit.isRed()) Color.Red else Color.Black
            )
            Text(
                card.suit.getSymbol(),
                fontSize = 9.sp,
                color = if (card.suit.isRed()) Color.Red else Color.Black
            )
        }
    }
}
