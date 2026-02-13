package com.tarneeb.game.engine

import com.tarneeb.game.model.*
import com.tarneeb.game.utils.GameConstants
import kotlin.random.Random

class GameEngine(
    val cardRulesEngine: CardRulesEngine = CardRulesEngine(),
    val scoringEngine: ScoringEngine = ScoringEngine(),
    val biddingEngine: BiddingEngine = BiddingEngine()
) {

    fun initializeGame(
        team1: Team,
        team2: Team,
        dealerIndex: Int = 0
    ): Game {
        val players = listOf(team1.player1, team1.player2, team2.player1, team2.player2)
        return Game(
            team1 = team1,
            team2 = team2,
            players = players,
            dealerIndex = dealerIndex,
            currentPlayerToPlayIndex = (dealerIndex + 1) % 4
        )
    }

    fun dealCards(game: Game): Game {
        val deck = Card.createFullDeck().shuffled()
        var cardIndex = 0

        for (i in 0 until GameConstants.TOTAL_PLAYERS) {
            game.players[i].hand.clear()
            for (j in 0 until GameConstants.CARDS_PER_PLAYER) {
                game.players[i].addCard(deck[cardIndex++])
            }
            game.players[i].sortHand()
        }

        game.gamePhase = GamePhase.BIDDING
        game.biddingPhase = BiddingPhase.PLAYER1_BIDDING
        game.currentPlayerToPlayIndex = (game.dealerIndex + 1) % GameConstants.TOTAL_PLAYERS

        return game
    }

    fun placeBid(game: Game, playerIndex: Int, bid: Int): Game {
        val player = game.players[playerIndex]
        val minimumBid = scoringEngine.getMinimumBid(
            maxOf(game.team1.score, game.team2.score)
        )

        if (cardRulesEngine.validateBid(bid, player.hand.size, minimumBid)) {
            player.bid = bid

            // Move to next bidding phase
            game.biddingPhase = when (game.biddingPhase) {
                BiddingPhase.PLAYER1_BIDDING -> BiddingPhase.PLAYER2_BIDDING
                BiddingPhase.PLAYER2_BIDDING -> BiddingPhase.PLAYER3_BIDDING
                BiddingPhase.PLAYER3_BIDDING -> BiddingPhase.PLAYER4_BIDDING
                BiddingPhase.PLAYER4_BIDDING -> {
                    if (game.allPlayersHaveBid()) {
                        BiddingPhase.COMPLETE
                    } else {
                        BiddingPhase.PLAYER1_BIDDING
                    }
                }
                else -> BiddingPhase.WAITING
            }

            if (game.biddingPhase == BiddingPhase.COMPLETE) {
                val totalBids = game.players.sumOf { it.bid }
                val minimumTotalBids = scoringEngine.getMinimumTotalBids(
                    maxOf(game.team1.score, game.team2.score)
                )

                if (totalBids < minimumTotalBids) {
                    // Reshuffle - reset bids and deal again
                    resetBids(game)
                    dealCards(game)
                } else {
                    // Move to playing phase
                    game.gamePhase = GamePhase.PLAYING
                    game.currentPlayerToPlayIndex = (game.dealerIndex + 1) % GameConstants.TOTAL_PLAYERS
                }
            }
        }

        return game
    }

    fun playCard(game: Game, playerIndex: Int, card: Card): Game {
        val player = game.players[playerIndex]
        val currentTrick = game.tricks.lastOrNull() ?: Trick()

        val playedCards = currentTrick.cards.values.toList()
        if (cardRulesEngine.canPlayCard(card, player, currentTrick, playedCards)) {
            player.removeCard(card)
            currentTrick.addCard(playerIndex, card)

            if (currentTrick.cards.size == GameConstants.TOTAL_PLAYERS) {
                // Trick is complete
                val winnerId = cardRulesEngine.calculateTrickWinner(currentTrick, Suit.HEARTS)
                currentTrick.winnerId = winnerId
                game.tricks.add(currentTrick)

                // Increment tricks won for player and their team
                val winner = game.getPlayerById(winnerId)
                if (winner != null) {
                    winner.tricksWon++
                }

                // Check if all tricks are played
                if (game.tricks.size == GameConstants.CARDS_PER_PLAYER) {
                    endRound(game)
                } else {
                    // Start new trick with winner
                    game.currentPlayerToPlayIndex = winnerId
                    game.currentTrick = 1
                }
            } else {
                // Move to next player
                game.currentPlayerToPlayIndex = (playerIndex + 1) % GameConstants.TOTAL_PLAYERS
                while (game.players[game.currentPlayerToPlayIndex].hand.isEmpty()) {
                    game.currentPlayerToPlayIndex = (game.currentPlayerToPlayIndex + 1) % GameConstants.TOTAL_PLAYERS
                }
            }
        }

        return game
    }

    fun endRound(game: Game) {
        val isAfter30 = maxOf(game.team1.score, game.team2.score) >= 30
        val scoreDifference = game.team1.getTotalTricksWon() >= game.team1.getTotalBid()

        game.team1.player1.score += if (scoreDifference) {
            scoringEngine.getPointsForBid(game.team1.player1.bid, isAfter30)
        } else {
            -game.team1.player1.bid
        }

        game.team1.player2.score += if (scoreDifference) {
            scoringEngine.getPointsForBid(game.team1.player2.bid, isAfter30)
        } else {
            -game.team1.player2.bid
        }

        val scoreDifference2 = game.team2.getTotalTricksWon() >= game.team2.getTotalBid()
        game.team2.player1.score += if (scoreDifference2) {
            scoringEngine.getPointsForBid(game.team2.player1.bid, isAfter30)
        } else {
            -game.team2.player1.bid
        }

        game.team2.player2.score += if (scoreDifference2) {
            scoringEngine.getPointsForBid(game.team2.player2.bid, isAfter30)
        } else {
            -game.team2.player2.bid
        }

        // Check for winner
        if (game.team1.score >= GameConstants.WINNING_SCORE && game.team1.player2.score > 0) {
            game.isGameOver = true
            game.winningTeamId = game.team1.id.toIntOrNull() ?: 1
            game.gamePhase = GamePhase.GAME_END
        } else if (game.team2.score >= GameConstants.WINNING_SCORE && game.team2.player2.score > 0) {
            game.isGameOver = true
            game.winningTeamId = game.team2.id.toIntOrNull() ?: 2
            game.gamePhase = GamePhase.GAME_END
        } else {
            // Reset for next round
            game.resetForNewRound()
        }
    }

    private fun resetBids(game: Game) {
        game.players.forEach { it.bid = 0 }
    }

    fun shouldRedeal(game: Game): Boolean {
        val totalBids = game.players.sumOf { it.bid }
        val minimumTotalBids = scoringEngine.getMinimumTotalBids(
            maxOf(game.team1.score, game.team2.score)
        )
        return totalBids < minimumTotalBids
    }
}
