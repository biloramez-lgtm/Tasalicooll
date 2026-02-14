package com.tasalicool.game.ui.components

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
import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Suit

@Composable
fun CardComponent(
    card: Card,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCardClick: (() -> Unit)? = null
) {
    val isRed = card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS

    val backgroundColor =
        if (isRed) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)

    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary
        else if (isRed) Color.Red
        else Color.Black

    Box(
        modifier = modifier
            .width(60.dp)
            .height(90.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = onCardClick != null) {
                onCardClick?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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
            containerColor =
                if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    Color(0xFFE0E0E0)
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
fun Scoreboard(
    team1Score: Int,
    team2Score: Int,
    team1Name: String = "Team 1",
    team2Name: String = "Team 2"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = team1Name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = team1Score.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = team2Name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = team2Score.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
