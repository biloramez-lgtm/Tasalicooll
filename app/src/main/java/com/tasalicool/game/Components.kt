package com.tarneeb.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarneeb.game.model.Card
import com.tarneeb.game.model.Player
import com.tarneeb.game.model.Suit

@Composable
fun CardComponent(
    card: Card,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCardClick: (() -> Unit)? = null,
    isHidden: Boolean = false
) {
    val backgroundColor = when {
        isHidden -> Color.Gray
        card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS -> Color(0xFFFFEBEE)
        else -> Color(0xFFE8F5E9)
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS -> Color.Red
        else -> Color.Black
    }

    Box(
        modifier = modifier
            .width(60.dp)
            .height(90.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                if (isSelected) 3.dp else 2.dp,
                borderColor,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = onCardClick != null) { onCardClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        if (!isHidden) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = card.rank.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = card.suit.getSymbol(),
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun PlayerCard(
    player: Player,
    showHand: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (player.bid > 0) {
                        Text(
                            text = "Bid: ${player.bid}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Score: ${player.score}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (player.tricksWon > 0) {
                        Text(
                            text = "Tricks: ${player.tricksWon}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (showHand && player.hand.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(player.hand.size) { index ->
                        CardComponent(card = player.hand[index])
                    }
                }
            }
        }
    }
}

@Composable
fun BidButton(
    bid: Int,
    isSelected: Boolean = false,
    onBidClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onBidClick,
        modifier = modifier
            .width(56.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = bid.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun TrickDisplay(
    playedCards: Map<Int, Card>,
    playerNames: Map<Int, String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (playedCards.isEmpty()) {
            Text("No cards played yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                playedCards.forEach { (playerId, card) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        CardComponent(card = card)
                        Text(
                            text = playerNames[playerId] ?: "Player",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Scoreboard(
    team1Score: Int,
    team2Score: Int,
    team1Name: String = "Team 1",
    team2Name: String = "Team 2",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreItem(team1Name, team1Score)
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
        ScoreItem(team2Name, team2Score)
    }
}

@Composable
private fun ScoreItem(
    teamName: String,
    score: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
