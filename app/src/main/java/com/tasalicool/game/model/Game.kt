package com.tasalicool.game.model

import java.util.UUID

// ==================== GAME STATE ENUMS ====================

enum class GamePhase {
    DEALING,
    BIDDING,
    PLAYING,
    ROUND_END,
    GAME_END
}

enum class BiddingPhase {
    WAITING,
    PLAYER1_BIDDING,
    PLAYER2_BIDDING,
    PLAYER3_BIDDING,
    PLAYER4_BIDDING,
    COMPLETE
}

enum class GameMode {
    SINGLE_PLAYER,      // لاعب واحد مع AI
    MULTIPLAYER_LOCAL,  // WiFi محلي
    MULTIPLAYER_ONLINE  // أونلاين
}

enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

// ==================== ROUND RESULT ====================

data class RoundResult(
    val roundNumber: Int,
    val team1Score: Int,
    val team2Score: Int,
    val team1Bid: Int,
    val team2Bid: Int,
    val team1TricksWon: Int,
    val team2TricksWon: Int,
    val team1BidMet: Boolean,
    val team2BidMet: Boolean,
    val winner: Int,  // 1 or 2
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getBidResult(teamId: Int): String {
        return if (teamId == 1) {
            if (team1BidMet) "✓ Bid Met" else "✗ Bid Failed"
        } else {
            if (team2BidMet) "✓ Bid Met" else "✗ Bid Failed"
        }
    }

    fun getTeamScore(teamId: Int): Int {
        return if (teamId == 1) team1Score else team2Score
    }
}

// ==================== TRICK CLASS ====================

data class Trick(
    val id: String = UUID.randomUUID().toString(),
    val cards: MutableMap<Int, Card> = mutableMapOf(),
    var winnerId: Int = -1,
    val playOrder: MutableList<Int> = mutableListOf(),
    var trickSuit: Suit? = null,
    var heartsBroken: Boolean = false,
    var trickNumber: Int = 0
) {
    fun addCard(playerId: Int, card: Card) {
        cards[playerId] = card
        playOrder.add(playerId)
        
        if (trickSuit == null) {
            trickSuit = card.suit
        }

        if (card.suit == Suit.HEARTS && !heartsBroken) {
            heartsBroken = true
        }
    }

    fun isComplete(playerCount: Int = 4): Boolean = cards.size == playerCount

    fun isEmpty(): Boolean = cards.isEmpty()

    fun isFull(): Boolean = cards.size == 4

    fun getHighestCard(trumpSuit: Suit = Suit.HEARTS): Card? {
        if (cards.isEmpty()) return null

        val trumpCards = cards.values.filter { it.suit == trumpSuit }
        if (trumpCards.isNotEmpty()) {
            return trumpCards.maxByOrNull { it.rank.value }
        }

        val trickSuitCards = cards.values.filter { it.suit == trickSuit }
        if (trickSuitCards.isNotEmpty()) {
            return trickSuitCards.maxByOrNull { it.rank.value }
        }

        return cards.values.firstOrNull()
    }

    fun getWinnerId(trumpSuit: Suit = Suit.HEARTS): Int {
        if (cards.isEmpty()) return -1

        val highestCard = getHighestCard(trumpSuit) ?: return -1
        return cards.entries.find { it.value == highestCard }?.key ?: -1
    }

    fun calculateWinner(trumpSuit: Suit = Suit.HEARTS): Int {
        if (cards.isEmpty()) return -1

        var winningCard = cards[playOrder.first()] ?: return -1
        var winnerId = playOrder.first()

        for (playerId in playOrder.drop(1)) {
            val currentCard = cards[playerId] ?: continue

            if (currentCard.suit == trumpSuit && winningCard.suit != trumpSuit) {
                winningCard = currentCard
                winnerId = playerId
            } else if (currentCard.suit == trumpSuit && winningCard.suit == trumpSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            } else if (winningCard.suit != trumpSuit && currentCard.suit == trickSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            } else if (currentCard.suit == winningCard.suit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
        }

        winnerId = winnerId
        return winnerId
    }

    fun reset() {
        cards.clear()
        playOrder.clear()
        winnerId = -1
        trickSuit = null
    }

    override fun toString(): String {
        return "Trick(number=$trickNumber, cards=${cards.size}, suit=$trickSuit, winner=$winnerId)"
    }
}

// ==================== GAME CLASS ====================

data class Game(
    val id: String = UUID.randomUUID().toString(),
    val team1: Team,
    val team2: Team,
    val players: List<Player>,
    
    // Game State
    var currentRound: Int = 1,
    var currentTrick: Int = 1,
    var dealerIndex: Int = 0,
    var currentPlayerToPlayIndex: Int = 1,
    
    // Game Phase
    var gamePhase: GamePhase = GamePhase.DEALING,
    var biddingPhase: BiddingPhase = BiddingPhase.WAITING,
    
    // Game Data
    val tricks: MutableList<Trick> = mutableListOf(),
    val roundHistory: MutableList<RoundResult> = mutableListOf(),
    
    // Game Status
    var isGameOver: Boolean = false,
    var winningTeamId: Int = -1,
    
    // Game Settings
    var gameMode: GameMode = GameMode.SINGLE_PLAYER,
    var difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    var startedAt: Long = 0,
    var endedAt: Long = 0
) {
    
    // ==================== BASIC GETTERS ====================
    
    fun getPlayers(): List<Player> = players

    fun getPlayerByPosition(position: Int): Player = players.getOrNull(position) 
        ?: throw IllegalArgumentException("Invalid position: $position")

    fun getPlayerById(id: Int): Player? = players.find { it.id == id }

    fun getTeamByPlayerId(playerId: Int): Team? {
        return when {
            team1.player1.id == playerId || team1.player2.id == playerId -> team1
            team2.player1.id == playerId || team2.player2.id == playerId -> team2
            else -> null
        }
    }

    fun getCurrentPlayer(): Player = players[currentPlayerToPlayIndex]

    fun getNextPlayerIndex(): Int = (currentPlayerToPlayIndex + 1) % 4

    fun getDealerPlayer(): Player = players[dealerIndex]

    fun getRightOfDealerIndex(): Int = (dealerIndex + 1) % 4

    // ==================== ROUND MANAGEMENT ====================

    fun resetForNewRound() {
        currentTrick = 1
        tricks.clear()
        dealerIndex = (dealerIndex + 1) % 4
        currentPlayerToPlayIndex = getRightOfDealerIndex()
        currentRound++
        team1.resetRound()
        team2.resetRound()
        gamePhase = GamePhase.DEALING
        biddingPhase = BiddingPhase.WAITING
    }

    fun startNewRound() {
        currentTrick = 1
        tricks.clear()
        gamePhase = GamePhase.BIDDING
        biddingPhase = BiddingPhase.PLAYER1_BIDDING
        currentPlayerToPlayIndex = getRightOfDealerIndex()
    }

    fun endRound() {
        gamePhase = GamePhase.ROUND_END
        
        // Create round result
        val roundResult = RoundResult(
            roundNumber = currentRound,
            team1Score = team1.score,
            team2Score = team2.score,
            team1Bid = team1.getTotalBid(),
            team2Bid = team2.getTotalBid(),
            team1TricksWon = team1.getTotalTricksWon(),
            team2TricksWon = team2.getTotalTricksWon(),
            team1BidMet = team1.isBidMet(),
            team2BidMet = team2.isBidMet(),
            winner = if (team1.score > team2.score) 1 else 2
        )
        
        roundHistory.add(roundResult)

        // Check for game over
        if (team1.isWinner || team2.isWinner) {
            gamePhase = GamePhase.GAME_END
            isGameOver = true
            winningTeamId = if (team1.isWinner) 1 else 2
            endedAt = System.currentTimeMillis()
        }
    }

    // ==================== BIDDING ====================

    fun allPlayersHaveBid(): Boolean {
        return players.all { it.bid > 0 }
    }

    fun getMinimumTotalBids(): Int {
        val maxScore = maxOf(team1.score, team2.score)
        return when {
            maxScore >= 50 -> 14
            maxScore >= 40 -> 13
            maxScore >= 30 -> 12
            else -> 11
        }
    }

    fun getMinimumBidForPlayer(): Int {
        val maxScore = maxOf(team1.score, team2.score)
        return when {
            maxScore >= 50 -> 5
            maxScore >= 40 -> 4
            maxScore >= 30 -> 3
            else -> 2
        }
    }

    fun getTotalBids(): Int {
        return players.sumOf { it.bid }
    }

    fun isBidValidForRound(): Boolean {
        return getTotalBids() >= getMinimumTotalBids()
    }

    // ==================== TRICKS ====================

    fun getCurrentTrick(): Trick? {
        return tricks.lastOrNull()
    }

    fun getOrCreateCurrentTrick(): Trick {
        val lastTrick = tricks.lastOrNull()
        return if (lastTrick != null && !lastTrick.isComplete(4)) {
            lastTrick
        } else {
            Trick(trickNumber = tricks.size + 1).also { tricks.add(it) }
        }
    }

    fun getTrickCount(): Int = tricks.size

    fun getTricksWonByTeam(teamId: Int): Int {
        return tricks.count { trick ->
            trick.winnerId in listOf(
                if (teamId == 1) team1.player1.id else team2.player1.id,
                if (teamId == 1) team1.player2.id else team2.player2.id
            )
        }
    }

    // ==================== GAME STATE ====================

    fun getGameStatus(): String {
        return when (gamePhase) {
            GamePhase.DEALING -> "Dealing cards..."
            GamePhase.BIDDING -> "Bidding phase - Player ${currentPlayerToPlayIndex + 1}"
            GamePhase.PLAYING -> "Playing - Round $currentRound, Trick $currentTrick"
            GamePhase.ROUND_END -> "Round ended"
            GamePhase.GAME_END -> "Game over - Team $winningTeamId wins!"
        }
    }

    fun isPlayerAI(playerId: Int): Boolean {
        return getPlayerById(playerId)?.isAI ?: false
    }

    fun getTeamScore(teamId: Int): Int {
        return if (teamId == 1) team1.score else team2.score
    }

    fun getTeam(teamId: Int): Team? {
        return if (teamId == 1) team1 else if (teamId == 2) team2 else null
    }

    // ==================== GAME DURATION ====================

    fun getDuration(): Long {
        val end = if (endedAt > 0) endedAt else System.currentTimeMillis()
        return if (startedAt > 0) end - startedAt else 0
    }

    fun getDurationInSeconds(): Long = getDuration() / 1000

    fun getDurationInMinutes(): Long = getDurationInSeconds() / 60

    // ==================== VALIDATION ====================

    fun validate(): Boolean {
        // Check players
        if (players.size != 4) return false
        
        // Check teams
        if (team1.player1.id == team2.player1.id) return false
        if (team1.player2.id == team2.player2.id) return false
        
        // Check dealer
        if (dealerIndex !in 0..3) return false
        
        return true
    }

    // ==================== COPY & DEBUG ====================

    fun copy(): Game {
        return this.copy(
            tricks = this.tricks.toMutableList(),
            roundHistory = this.roundHistory.toMutableList()
        )
    }

    override fun toString(): String {
        return """
            Game(
                id='$id',
                round=$currentRound,
                phase=$gamePhase,
                team1Score=${team1.score},
                team2Score=${team2.score},
                gameOver=$isGameOver
            )
        """.trimIndent()
    }

    fun getDetailedInfo(): String {
        return """
            ===== GAME INFO =====
            Game ID: $id
            Mode: $gameMode
            Difficulty: $difficulty
            
            ===== CURRENT STATE =====
            Phase: $gamePhase
            Round: $currentRound
            Trick: $currentTrick
            Current Player: ${getCurrentPlayer().name} (ID: ${getCurrentPlayer().id})
            
            ===== TEAMS =====
            Team 1: ${team1.name} - Score: ${team1.score}
              - ${team1.player1.name}: ${team1.player1.score}
              - ${team1.player2.name}: ${team1.player2.score}
            
            Team 2: ${team2.name} - Score: ${team2.score}
              - ${team2.player1.name}: ${team2.player1.score}
              - ${team2.player2.name}: ${team2.player2.score}
            
            ===== BIDDING =====
            ${players.joinToString("\n") { "${it.name}: ${it.bid}" }}
            
            ===== GAME STATUS =====
            Total Rounds: ${roundHistory.size}
            Game Over: $isGameOver
            ${if (isGameOver) "Winner: Team $winningTeamId" else ""}
            Duration: ${getDurationInMinutes()} minutes
        """.trimIndent()
    }
}
