package com.tarneeb.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarneeb.game.model.Game
import com.tarneeb.game.model.GamePhase
import com.tarneeb.game.ui.components.*
import com.tarneeb.game.viewmodel.GameViewModel

@Composable
fun MainScreen(onStartGame: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "♥ TARNEEB ♥",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "The classic Lebanese card game for 4 players",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Start New Game",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BiddingScreen(
    viewModel: GameViewModel = viewModel(),
    game: Game
) {
    val validBids by viewModel.validBids.collectAsState()
    val currentPlayer = game.getCurrentPlayer()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with game info
            Column {
                Scoreboard(
                    team1Score = game.team1.score,
                    team2Score = game.team2.score,
                    team1Name = "Team 1",
                    team2Name = "Team 2"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Round ${game.currentRound} - Bidding Phase",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "${currentPlayer.name}'s turn to bid",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Player hands
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(game.players.size) { index ->
                    PlayerCard(
                        player = game.players[index],
                        showHand = game.players[index].id == currentPlayer.id
                    )
                }
            }

            // Bidding buttons
            if (!currentPlayer.isAI) {
                Column {
                    Text(
                        text = "Select your bid:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(validBids.size) { index ->
                            BidButton(
                                bid = validBids[index],
                                onBidClick = {
                                    viewModel.placeBid(currentPlayer.id, validBids[index])
                                }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel = viewModel(),
    game: Game
) {
    val validCards by viewModel.validCards.collectAsState()
    val currentPlayer = game.getCurrentPlayer()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Scoreboard(
                team1Score = game.team1.score,
                team2Score = game.team2.score
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game info
            Text(
                text = "Round ${game.currentRound} - Trick ${game.currentTrick}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Trick display
            val currentTrick = game.tricks.lastOrNull()
            if (currentTrick != null) {
                TrickDisplay(
                    playedCards = currentTrick.cards,
                    playerNames = game.players.associateBy({ it.id }, { it.name })
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current player info
            Text(
                text = "${currentPlayer.name}'s turn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Player cards
            if (!currentPlayer.isAI && currentPlayer.hand.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentPlayer.hand.size) { index ->
                        CardComponent(
                            card = currentPlayer.hand[index],
                            isSelected = validCards.contains(currentPlayer.hand[index]),
                            onCardClick = {
                                if (validCards.contains(currentPlayer.hand[index])) {
                                    viewModel.playCard(currentPlayer.id, currentPlayer.hand[index])
                                }
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(
    game: Game,
    onPlayAgain: () -> Unit
) {
    val winningTeam = if (game.team1.score >= 41) game.team1 else game.team2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${winningTeam.name} Wins!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Final Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Team 1", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${game.team1.score}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Team 2", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${game.team2.score}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Play Again",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
