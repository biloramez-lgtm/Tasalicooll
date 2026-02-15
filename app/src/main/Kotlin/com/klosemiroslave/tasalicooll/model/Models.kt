package com.klosemiroslave.tasalicooll.model

/**
 * Suit - أنواع الأوراق
 * ترتيب القوة: القلوب > باقي الأنواع
 */
enum class Suit(val symbol: String, val arabicName: String) {
    HEARTS("♥", "قلوب"),      // الأقوى دائماً
    DIAMONDS("♦", "ماسات"),
    CLUBS("♣", "نوادي"),
    SPADES("♠", "بستم")
}

/**
 * Rank - قيم الأوراق
 * ترتيب القوة: 2 < 3 < ... < K < A (الأس هو الأعلى)
 */
enum class Rank(val value: Int, val display: String) {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A")
}

/**
 * Card - ورقة اللعب
 */
data class Card(
    val suit: Suit,
    val rank: Rank
) {
    override fun toString(): String = "${rank.display}${suit.symbol}"
}

/**
 * Player - لاعب واحد
 */
data class Player(
    val id: Int,
    val name: String,
    val isAI: Boolean = false
) {
    val hand: MutableList<Card> = mutableListOf()
    var bid: Int = 0              // البدية المعلنة
    var tricksWon: Int = 0        // عدد الخدعات المكتسبة
    var score: Int = 0            // النقاط الحالية
    
    fun addCard(card: Card) {
        hand.add(card)
    }
    
    fun removeCard(card: Card): Boolean {
        return hand.remove(card)
    }
    
    fun clearHand() {
        hand.clear()
        bid = 0
        tricksWon = 0
    }
    
    fun canFollowSuit(suit: Suit): Boolean {
        return hand.any { it.suit == suit }
    }
    
    fun sortHand() {
        hand.sortWith(compareBy({ it.suit.ordinal }, { it.rank.value }))
    }
}

/**
 * Team - فريق من لاعبين (2 لاعب)
 */
data class Team(
    val id: Int,
    val name: String,
    val players: List<Player>
) {
    var totalScore: Int = 0
    var totalBid: Int = 0
    var tricksWon: Int = 0
    
    val isWinner: Boolean
        get() = totalScore >= 41
    
    fun addScore(points: Int) {
        totalScore += points
    }
    
    fun resetBid() {
        totalBid = 0
        players.forEach { it.bid = 0 }
    }
    
    fun calculateBid(): Int {
        totalBid = players.sumOf { it.bid }
        return totalBid
    }
}

/**
 * Trick - خدعة واحدة (4 أوراق، واحدة من كل لاعب)
 */
data class Trick(
    val number: Int = 0
) {
    val cardsPlayed: MutableMap<Int, Card> = mutableMapOf()  // playerId -> Card
    val playOrder: MutableList<Int> = mutableListOf()        // ترتيب اللعب
    var winnerId: Int? = null                                 // فائز الخدعة
    
    val trickSuit: Suit?
        get() = cardsPlayed.values.firstOrNull()?.suit
    
    fun playCard(playerId: Int, card: Card) {
        if (!cardsPlayed.containsKey(playerId)) {
            playOrder.add(playerId)
        }
        cardsPlayed[playerId] = card
    }
    
    fun isComplete(totalPlayers: Int): Boolean {
        return cardsPlayed.size == totalPlayers
    }
}

/**
 * GamePhase - مراحل اللعبة
 */
enum class GamePhase {
    WAITING,         // انتظار اللاعبين
    BIDDING,         // مرحلة البدية
    PLAYING,         // مرحلة اللعب
    ROUND_END,       // نهاية الجولة
    GAME_END         // نهاية اللعبة
}

/**
 * TarneebGame - لعبة Tarneeb كاملة
 * 
 * القواعس:
 * - الهدف: 41 نقطة
 * - البدية: 2-13
 * - القلوب قوي دائماً
 * - تتبع الرمز إجباري
 */
data class TarneebGame(
    val team1: Team,
    val team2: Team,
    val players: List<Player>
) {
    // حالة اللعبة
    var gamePhase: GamePhase = GamePhase.WAITING
    var currentPlayerIndex: Int = 0
    var dealerIndex: Int = 0
    var currentRound: Int = 1
    var currentTrickNumber: Int = 0
    var isGameOver: Boolean = false
    
    // بيانات اللعبة
    val tricks: MutableList<Trick> = mutableListOf()
    
    val currentPlayer: Player?
        get() = if (currentPlayerIndex < players.size) players[currentPlayerIndex] else null
    
    val currentTrick: Trick?
        get() = tricks.lastOrNull()
    
    fun nextPlayerIndex(): Int {
        return (currentPlayerIndex + 1) % players.size
    }
    
    fun advancePlayer() {
        currentPlayerIndex = nextPlayerIndex()
    }
    
    fun startBidding() {
        gamePhase = GamePhase.BIDDING
        currentPlayerIndex = (dealerIndex + 1) % players.size
        players.forEach { it.bid = 0 }
    }
    
    fun startPlaying() {
        gamePhase = GamePhase.PLAYING
        currentTrickNumber = 1
        tricks.clear()
        team1.tricksWon = 0
        team2.tricksWon = 0
        players.forEach { it.tricksWon = 0 }
        currentPlayerIndex = (dealerIndex + 1) % players.size
    }
    
    fun endRound() {
        gamePhase = GamePhase.ROUND_END
    }
    
    fun startNextRound() {
        dealerIndex = (dealerIndex + 1) % players.size
        currentRound++
        team1.resetBid()
        team2.resetBid()
        players.forEach { 
            it.clearHand()
            it.tricksWon = 0
        }
    }
    
    fun endGame() {
        gamePhase = GamePhase.GAME_END
        isGameOver = true
    }
    
    /**
     * الحصول على فريق اللاعب
     */
    fun getTeamByPlayerId(playerId: Int): Team? {
        return when {
            team1.players.any { it.id == playerId } -> team1
            team2.players.any { it.id == playerId } -> team2
            else -> null
        }
    }
    
    /**
     * الحصول على فريق الخصم
     */
    fun getOpponentTeam(playerId: Int): Team? {
        val playerTeam = getTeamByPlayerId(playerId)
        return when (playerTeam?.id) {
            team1.id -> team2
            team2.id -> team1
            else -> null
        }
    }
    
    /**
     * معلومات اللعبة
     */
    fun getInfo(): String {
        return """
            |====== Tarneeb Game ======
            |${team1.name}: ${team1.totalScore} نقاط (البدية: ${team1.totalBid})
            |${team2.name}: ${team2.totalScore} نقاط (البدية: ${team2.totalBid})
            |المرحلة: ${gamePhase.name}
            |الجولة: $currentRound
            |الخدعات: ${tricks.size}/13
            |الدور: ${currentPlayer?.name ?: "لا أحد"}
        """.trimMargin()
    }
}
