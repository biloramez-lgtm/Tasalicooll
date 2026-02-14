package com.tasalicool.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasalicool.game.model.Card

/**
 * PlayerHand - يد اللاعب
 */
@Composable
fun PlayerHand(
    hand: List<Card>,
    onCardClick: (Card) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        color = Color(0xFF0d0d0d),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy((-16).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            hand.forEach { card ->
                HandCard(card, onCardClick)
            }
        }
    }
}

@Composable
fun HandCard(card: Card, onClick: (Card) -> Unit) {
    Surface(
        modifier = Modifier
            .width(50.dp)
            .height(75.dp)
            .background(Color.White, RoundedCornerShape(6.dp)),
        onClick = { onClick(card) },
        color = Color.White,
        shape = RoundedCornerShape(6.dp)
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
        }
    }
}
