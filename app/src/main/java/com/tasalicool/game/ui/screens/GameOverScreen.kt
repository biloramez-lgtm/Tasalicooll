package com.tasalicool.game.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import kotlinx.coroutines.delay

/**
 * GameOverScreen - شاشة نهاية اللعبة
 * 
 * تعرض:
 * ✅ الفريق الرابح بتأثيرات
 * ✅ النقاط النهائية
 * ✅ إحصائيات اللعبة الكاملة
 * ✅ ترتيب اللاعبين (MVP)
 * ✅ عدد الجولات واللقطات
 * ✅ أفضل أداء ومحاولة
 * ✅ زر العودة أو اللعب مجدداً
 */
@Composable
fun GameOverScreen(
    game: Game,
    winningTeamId: Int,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit
) {
    var showAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        showAnimation = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // ==================== WINNER CELEBRATION ====================
            WinnerCelebrationSection(
                game = game,
                winningTeamId = winningTeamId,
                showAnimation = showAnimation
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ==================== FINAL SCORES ====================
            FinalScoresSection(game, winningTeamId)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ==================== GAME STATISTICS ====================
            GameStatisticsSection(game)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ==================== PLAYERS RANKING ====================
            PlayersRankingSection(game)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ==================== ROUND SUMMARY ====================
            RoundSummarySection(game)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ==================== ACTION BUTTONS ====================
            ActionButtonsSection(onPlayAgain, onReturnHome)
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==================== WINNER CELEBRATION ====================

@Composable
private fun WinnerCelebrationSection(
    game: Game,
    winningTeamId: Int,
    showAnimation: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Trophy Animation
        val scale by animateDpAsState(
            targetValue = if (showAnimation) 120.dp else 60.dp,
            animationSpec = tween(800),
            label = "trophyScale"
        )
        
        Box(
            modifier = Modifier.size(scale),
            contentAlignment = Alignment.Center
        ) {
            Text("🏆", fontSize = 100.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Winner Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "GAME OVER",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryGold
            )
            
            val winningTeam = if (winningTeamId == 1) game.team1 else game.team2
            val winningPlayers = if (winningTeamId == 1) {
                listOf(game.team1.player1.name, game.team1.player2.name)
            } else {
                listOf(game.team2.player1.name, game.team2.player2.name)
            }
            
            Text(
                "Team $winningTeamId Wins!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )
            
            Text(
                winningPlayers.joinToString(" & "),
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.Normal
            )
        }
        
        // Celebration Emojis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎉 ", fontSize = 24.sp)
            Text("🎊 ", fontSize = 24.sp)
            Text("🎉 ", fontSize = 24.sp)
        }
    }
}

// ==================== FINAL SCORES ====================

@Composable
private fun FinalScoresSection(game: Game, winningTeamId: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Final Scores",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(start = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinalScoreCard(
                teamName = "Team 1",
                score = game.team1.score,
                isWinner = winningTeamId == 1,
                modifier = Modifier.weight(1f)
            )
            
            FinalScoreCard(
                teamName = "Team 2",
                score = game.team2.score,
                isWinner = winningTeamId == 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FinalScoreCard(
    teamName: String,
    score: Int,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp),
        color = if (isWinner) PrimaryRed else BackgroundBlack,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isWinner) 12.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                teamName,
                fontSize = 12.sp,
                color = if (isWinner) Color.White else TextGray,
                fontWeight = FontWeight.Normal
            )
            
            Text(
                "$score",
                fontSize = 40.sp,
                color = if (isWinner) Color.White else SecondaryGold,
                fontWeight = FontWeight.Bold
            )
            
            if (isWinner) {
                Text(
                    "CHAMPION",
                    fontSize = 10.sp,
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== GAME STATISTICS ====================

@Composable
private fun GameStatisticsSection(game: Game) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Game Statistics",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(start = 8.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BackgroundBlack,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatisticRow(
                    label = "Total Rounds",
                    value = "${game.currentRound}",
                    icon = "🔄"
                )
                
                Divider(color = BorderGray, thickness = 1.dp)
                
                StatisticRow(
                    label = "Total Tricks",
                    value = "${game.tricks.size}",
                    icon = "🃏"
                )
                
                Divider(color = BorderGray, thickness = 1.dp)
                
                StatisticRow(
                    label = "Duration",
                    value = calculateGameDuration(game),
                    icon = "⏱️"
                )
                
                Divider(color = BorderGray, thickness = 1.dp)
                
                StatisticRow(
                    label = "Game Mode",
                    value = "Local Multi",
                    icon = "🎮"
                )
            }
        }
    }
}

@Composable
private fun StatisticRow(label: String, value: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 16.sp)
            Text(
                label,
                fontSize = 13.sp,
                color = TextGray,
                fontWeight = FontWeight.Normal
            )
        }
        
        Text(
            value,
            fontSize = 14.sp,
            color = SecondaryGold,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun calculateGameDuration(game: Game): String {
    val minutes = game.currentRound * 3  // تقريبي: 3 دقائق لكل جولة
    return if (minutes < 60) "$minutes min" else "${minutes / 60}h ${minutes % 60}m"
}

// ==================== PLAYERS RANKING ====================

@Composable
private fun PlayersRankingSection(game: Game) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Player Rankings",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(start = 8.dp)
        )
        
        // ترتيب اللاعبين حسب الأداء
        val rankedPlayers = game.players.sortedByDescending { it.score }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundBlack, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rankedPlayers.forEachIndexed { index, player ->
                PlayerRankCard(player, index + 1)
            }
        }
    }
}

@Composable
private fun PlayerRankCard(player: Player, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGreen, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when (rank) {
                        1 -> SecondaryGold
                        2 -> Color(0xFFC0C0C0)  // Silver
                        3 -> Color(0xFFCD7F32)  // Bronze
                        else -> BackgroundBlack
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "$rank"
                },
                fontSize = 16.sp
            )
        }
        
        // Player Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                player.name,
                fontSize = 14.sp,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Score: ${player.score}",
                fontSize = 12.sp,
                color = TextGray,
                fontWeight = FontWeight.Normal
            )
        }
        
        // Performance Indicator
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "${player.tricksWon}",
                fontSize = 14.sp,
                color = SecondaryGold,
                fontWeight = FontWeight.Bold
            )
            Text(
                "tricks",
                fontSize = 10.sp,
                color = TextGray,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ==================== ROUND SUMMARY ====================

@Composable
private fun RoundSummarySection(game: Game) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Round Summary",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(start = 8.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            color = BackgroundBlack,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // عرض الجولات الأخيرة
                game.tricks
                    .groupBy { it.trickNumber / 13 + 1 }
                    .entries
                    .takeLast(5)
                    .forEach { (round, tricks) ->
                        RoundCard(round, tricks.size)
                    }
            }
        }
    }
}

@Composable
private fun RoundCard(roundNumber: Int, tricksCount: Int) {
    Surface(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp),
        color = BackgroundGreen,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Round",
                fontSize = 10.sp,
                color = TextGray
            )
            Text(
                "$roundNumber",
                fontSize = 18.sp,
                color = SecondaryGold,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$tricksCount tricks",
                fontSize = 9.sp,
                color = TextGray
            )
        }
    }
}

// ==================== ACTION BUTTONS ====================

@Composable
private fun ActionButtonsSection(onPlayAgain: () -> Unit, onReturnHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Play Again Button
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SuccessGreen
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄", fontSize = 20.sp)
                Text(
                    "Play Again",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        // Return Home Button
        Button(
            onClick = onReturnHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BackgroundBlack
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏠", fontSize = 20.sp)
                Text(
                    "Return Home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }
    }
}

// ==================== PREVIEW ====================

@Composable
fun GameOverScreenPreview() {
    val game = Game(
        team1 = Team(
            id = 1,
            name = "Team 1",
            player1 = Player(id = 0, name = "Player 1", score = 45),
            player2 = Player(id = 2, name = "Player 3", score = 38)
        ),
        team2 = Team(
            id = 2,
            name = "Team 2",
            player1 = Player(id = 1, name = "Player 2", score = 32),
            player2 = Player(id = 3, name = "Player 4", score = 35)
        ),
        players = listOf(
            Player(id = 0, name = "Player 1", score = 45, tricksWon = 35),
            Player(id = 1, name = "Player 2", score = 32, tricksWon = 28),
            Player(id = 2, name = "Player 3", score = 38, tricksWon = 32),
            Player(id = 3, name = "Player 4", score = 35, tricksWon = 30)
        ),
        currentRound = 5,
        isGameOver = true,
        winningTeamId = 1
    )
    
    TasalicoolTheme {
        GameOverScreen(
            game = game,
            winningTeamId = 1,
            onPlayAgain = {},
            onReturnHome = {}
        )
    }
}
