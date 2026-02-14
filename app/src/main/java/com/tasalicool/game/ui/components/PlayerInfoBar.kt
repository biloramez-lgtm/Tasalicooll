package com.tasalicool.game.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasalicool.game.R
import com.tasalicool.game.ui.theme.*

/* ================================
   UI STATUS (Pure UI Layer)
================================ */

enum class PlayerUiStatus {
    CONNECTED,
    BIDDING,
    PLAYING,
    WAITING,
    DISCONNECTED
}

/* ================================
   UI MODEL (Clean 100%)
================================ */

data class PlayerUiModel(
    val id: Int,
    val name: String,
    val bid: Int,
    val tricksWon: Int,
    val score: Int,
    val isConnected: Boolean,
    val isReady: Boolean,
    val status: PlayerUiStatus,
    val connectionDurationSeconds: Long = 0L,
    val isCurrentPlayer: Boolean = false,
    val isDealer: Boolean = false
)

/* ================================
   MAIN COMPONENT
================================ */

@Composable
fun PlayerInfoBar(
    player: PlayerUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (player.isCurrentPlayer) PrimaryRed else BackgroundBlack,
        animationSpec = tween(300),
        label = "bgColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (player.isCurrentPlayer) 8.dp else 4.dp,
        animationSpec = tween(300),
        label = "elevation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 60.dp else 80.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(player, if (compact) 40.dp else 56.dp)

            PlayerInfo(
                player = player,
                compact = compact,
                modifier = Modifier.weight(1f)
            )

            PlayerStats(player, compact)
        }
    }
}

/* ================================
   AVATAR
================================ */

@Composable
private fun PlayerAvatar(
    player: PlayerUiModel,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (player.isCurrentPlayer) SecondaryGold else BackgroundGreen
            )
            .shadow(4.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "👤",
            fontSize = (size.value / 2).sp
        )

        if (player.isCurrentPlayer) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen)
            )
        }

        if (player.isDealer) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp),
                color = PrimaryRed,
                shape = CircleShape
            ) {
                Text(
                    stringResource(R.string.dealer_badge),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.wrapContentSize(Alignment.Center)
                )
            }
        }

        if (!player.isConnected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ErrorRed)
            )
        }
    }
}

/* ================================
   INFO SECTION
================================ */

@Composable
private fun PlayerInfo(
    player: PlayerUiModel,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                player.name,
                fontSize = if (compact) 12.sp else 14.sp,
                color = if (player.isCurrentPlayer) Color.White else TextWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            StatusBadge(player.isConnected, player.isReady, compact)
        }

        if (!compact) {
            Text(
                text = stringResource(player.status.toStringRes()),
                fontSize = 10.sp,
                color = if (player.isConnected) SuccessGreen else ErrorRed
            )
        }

        if (!compact && player.isConnected && player.connectionDurationSeconds > 0) {
            Text(
                stringResource(
                    R.string.connected_seconds,
                    player.connectionDurationSeconds
                ),
                fontSize = 9.sp,
                color = TextGray
            )
        }
    }
}

/* ================================
   STATUS BADGE
================================ */

@Composable
private fun StatusBadge(
    isConnected: Boolean,
    isReady: Boolean,
    compact: Boolean
) {
    val (bg, txtColor, res) = when {
        !isConnected -> Triple(ErrorRed, Color.White, R.string.status_offline)
        isReady -> Triple(SuccessGreen, Color.Black, R.string.status_ready)
        else -> Triple(WarningOrange, Color.Black, R.string.status_waiting)
    }

    Surface(
        modifier = Modifier
            .height(if (compact) 16.dp else 20.dp),
        color = bg,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            stringResource(res),
            fontSize = if (compact) 8.sp else 9.sp,
            color = txtColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/* ================================
   STATS
================================ */

@Composable
private fun PlayerStats(
    player: PlayerUiModel,
    compact: Boolean
) {
    Column(
        modifier = Modifier
            .background(BackgroundGreen, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (!compact) {
            Text(stringResource(R.string.stat_bid), fontSize = 8.sp, color = TextGray)
        }

        Text(
            "${player.bid}",
            fontSize = if (compact) 12.sp else 16.sp,
            color = SecondaryGold,
            fontWeight = FontWeight.Bold
        )

        if (!compact) {
            Divider(color = BorderGray, thickness = 1.dp)
            Text(stringResource(R.string.stat_tricks), fontSize = 8.sp, color = TextGray)
        }

        Text(
            "${player.tricksWon}",
            fontSize = if (compact) 12.sp else 16.sp,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ================================
   STATUS → STRING RESOURCE
================================ */

private fun PlayerUiStatus.toStringRes(): Int = when (this) {
    PlayerUiStatus.CONNECTED -> R.string.status_connected
    PlayerUiStatus.BIDDING -> R.string.status_bidding
    PlayerUiStatus.PLAYING -> R.string.status_playing
    PlayerUiStatus.WAITING -> R.string.status_waiting
    PlayerUiStatus.DISCONNECTED -> R.string.status_offline
}

/* ================================
   COLORS
================================ */

private val ErrorRed = Color(0xFFE53935)
private val WarningOrange = Color(0xFFFF9800)
private val SuccessGreen = Color(0xFF4CAF50)
