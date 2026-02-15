package com.tarneeb.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

// ============================================================================
// MODELS - نماذج البيانات
// ============================================================================

/**
 * Suit - أنواع الأوراق
 */
enum class Suit(val symbol: String, val arabicName: String) {
    HEARTS("♥", "قلوب"),      // الأقوى دائماً
    DIAMONDS("♦", "ماسات"),
    CLUBS("♣", "نوادي"),
    SPADES("♠", "بستم")
}

/**
 * Rank - قيم الأوراق
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
 * Card - ورقة واحدة
 */
data class Card(val suit: Suit, val rank: Rank) {
    override fun toString(): String = "${rank.display}${suit.symbol}"
}

/**
 * Player - لاعب واحد
 */
data class Player(
    val id: Int,
    val name: String,
    val isAI: Boolean = false,
    val difficulty: AIDifficulty = AIDifficulty.MEDIUM
) {
    val hand: MutableList<Card> = mutableListOf()
    var bid: Int = 0
    var tricksWon: Int = 0
    var score: Int = 0
    
    fun addCard(card: Card) = hand.add(card)
    fun removeCard(card: Card): Boolean = hand.remove(card)
    fun clearHand() {
        hand.clear()
        bid = 0
        tricksWon = 0
    }
    fun canFollowSuit(suit: Suit): Boolean = hand.any { it.suit == suit }
    fun sortHand() = hand.sortWith(compareBy({ it.suit.ordinal }, { it.rank.value }))
}

/**
 * Team - فريق من لاعبين
 */
data class Team(
    val id: Int,
    val name: String,
    val players: List<Player>
) {
    var totalScore: Int = 0
    var totalBid: Int = 0
    var tricksWon: Int = 0
    
    val isWinner: Boolean get() = totalScore >= 41
    
    fun addScore(points: Int) { totalScore += points }
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
 * Trick - خدعة واحدة
 */
data class Trick(val number: Int = 0) {
    val cardsPlayed: MutableMap<Int, Card> = mutableMapOf()
    val playOrder: MutableList<Int> = mutableListOf()
    var winnerId: Int? = null
    
    val trickSuit: Suit? get() = cardsPlayed.values.firstOrNull()?.suit
    
    fun playCard(playerId: Int, card: Card) {
        if (!cardsPlayed.containsKey(playerId)) playOrder.add(playerId)
        cardsPlayed[playerId] = card
    }
    fun isComplete(totalPlayers: Int): Boolean = cardsPlayed.size == totalPlayers
}

/**
 * GamePhase - مراحل اللعبة
 */
enum class GamePhase {
    WAITING, BIDDING, PLAYING, ROUND_END, GAME_END
}

/**
 * AIDifficulty - مستويات صعوبة AI
 */
enum class AIDifficulty {
    EASY, MEDIUM, HARD
}

/**
 * GameMode - نمط اللعبة
 */
enum class GameMode {
    SINGLE_PLAYER, MULTIPLAYER_LOCAL
}

/**
 * AIAction - حركات AI
 */
sealed class AIAction {
    data class PlacingBid(val playerId: Int, val bid: Int) : AIAction()
    data class PlayingCard(val playerId: Int, val card: Card) : AIAction()
}

/**
 * TarneebGame - لعبة Tarneeb الكاملة
 */
data class TarneebGame(
    val team1: Team,
    val team2: Team,
    val players: List<Player>,
    val gameMode: GameMode = GameMode.SINGLE_PLAYER,
    val humanPlayerCount: Int = 1
) {
    var gamePhase: GamePhase = GamePhase.WAITING
    var currentPlayerIndex: Int = 0
    var dealerIndex: Int = 0
    var currentRound: Int = 1
    var currentTrickNumber: Int = 0
    var isGameOver: Boolean = false
    
    val tricks: MutableList<Trick> = mutableListOf()
    
    val currentPlayer: Player? get() = if (currentPlayerIndex < players.size) players[currentPlayerIndex] else null
    val currentTrick: Trick? get() = tricks.lastOrNull()
    
    fun nextPlayerIndex(): Int = (currentPlayerIndex + 1) % players.size
    fun advancePlayer() { currentPlayerIndex = nextPlayerIndex() }
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
    fun endRound() { gamePhase = GamePhase.ROUND_END }
    fun startNextRound() {
        dealerIndex = (dealerIndex + 1) % players.size
        currentRound++
        team1.resetBid()
        team2.resetBid()
        players.forEach { it.clearHand(); it.tricksWon = 0 }
    }
    fun endGame() {
        gamePhase = GamePhase.GAME_END
        isGameOver = true
    }
    fun getTeamByPlayerId(playerId: Int): Team? = when {
        team1.players.any { it.id == playerId } -> team1
        team2.players.any { it.id == playerId } -> team2
        else -> null
    }
    fun getOpponentTeam(playerId: Int): Team? {
        val playerTeam = getTeamByPlayerId(playerId)
        return when (playerTeam?.id) {
            team1.id -> team2
            team2.id -> team1
            else -> null
        }
    }
    fun getInfo(): String = """
        |====== Tarneeb Game ======
        |${team1.name}: ${team1.totalScore} نقاط (البدية: ${team1.totalBid})
        |${team2.name}: ${team2.totalScore} نقاط (البدية: ${team2.totalBid})
        |النمط: ${gameMode.name}
        |المرحلة: ${gamePhase.name}
        |الجولة: $currentRound
        |الخدعات: ${tricks.size}/13
        |الدور: ${currentPlayer?.name ?: "لا أحد"}
    """.trimMargin()
}

// ============================================================================
// GAME LOGIC - منطق اللعبة والقواعس
// ============================================================================

/**
 * EngineGod - محرك Tarneeb الكامل في ملف واحد
 * 
 * يجمع:
 * ✅ نماذج البيانات
 * ✅ منطق اللعبة
 * ✅ إدارة الحالة (StateFlow)
 * ✅ قواعس اللعبة
 * ✅ حسابات النقاط
 * ✅ خوارزميات الفوز
 * ✅ إدارة اللاعبين
 * ✅ Single Player + Multiplayer
 */
class EngineGod {
    
    // ========================================================================
    // STATE MANAGEMENT - إدارة الحالة
    // ========================================================================
    
    private val _gameState = MutableStateFlow<TarneebGame?>(null)
    val gameState: StateFlow<TarneebGame?> = _gameState.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _aiAction = MutableStateFlow<AIAction?>(null)
    val aiAction: StateFlow<AIAction?> = _aiAction.asStateFlow()
    
    private val random = Random(System.currentTimeMillis())
    
    // ========================================================================
    // GAME INITIALIZATION - تهيئة اللعبة
    // ========================================================================
    
    /**
     * بدء لعبة Single Player (لاعب + 3 AI)
     */
    fun startSinglePlayer(playerName: String, difficulty: AIDifficulty = AIDifficulty.MEDIUM) {
        val humanPlayer = Player(0, playerName, isAI = false)
        val aiPlayer1 = Player(1, "AI 1", isAI = true, difficulty = difficulty)
        val aiPlayer2 = Player(2, "AI 2", isAI = true, difficulty = difficulty)
        val aiPlayer3 = Player(3, "AI 3", isAI = true, difficulty = difficulty)
        
        val team1 = Team(1, "أنت والحليف", listOf(humanPlayer, aiPlayer2))
        val team2 = Team(2, "الخصوم", listOf(aiPlayer1, aiPlayer3))
        
        val game = TarneebGame(
            team1 = team1,
            team2 = team2,
            players = listOf(humanPlayer, aiPlayer1, aiPlayer2, aiPlayer3),
            gameMode = GameMode.SINGLE_PLAYER,
            humanPlayerCount = 1
        )
        
        dealCards(game)
        game.startBidding()
        _gameState.value = game
        
        if (game.currentPlayer?.isAI == true) executeAIAction(game)
    }
    
    /**
     * بدء لعبة Multiplayer Local
     */
    fun startMultiplayer(playerCount: Int, playerNames: List<String>, difficulty: AIDifficulty = AIDifficulty.MEDIUM) {
        require(playerCount in 2..4) { "عدد اللاعبين يجب أن يكون 2-4" }
        require(playerNames.size == playerCount) { "عدد الأسماء يجب أن يطابق عدد اللاعبين" }
        
        val humanPlayers = playerNames.mapIndexed { index, name ->
            Player(index, name, isAI = false)
        }
        
        val allPlayers = mutableListOf<Player>()
        allPlayers.addAll(humanPlayers)
        
        repeat(4 - playerCount) { index ->
            allPlayers.add(
                Player(
                    playerCount + index,
                    "AI ${index + 1}",
                    isAI = true,
                    difficulty = difficulty
                )
            )
        }
        
        val team1 = Team(1, "الفريق 1", listOf(allPlayers[0], allPlayers[2]))
        val team2 = Team(2, "الفريق 2", listOf(allPlayers[1], allPlayers[3]))
        
        val game = TarneebGame(
            team1 = team1,
            team2 = team2,
            players = allPlayers,
            gameMode = GameMode.MULTIPLAYER_LOCAL,
            humanPlayerCount = playerCount
        )
        
        dealCards(game)
        game.startBidding()
        _gameState.value = game
        
        if (game.currentPlayer?.isAI == true) executeAIAction(game)
    }
    
    // ========================================================================
    // CARD DEALING - توزيع الأوراق
    // ========================================================================
    
    /**
     * توزيع الأوراق
     */
    private fun dealCards(game: TarneebGame) {
        val deck = generateDeck().shuffled()
        game.players.forEach { it.clearHand() }
        deck.chunked(13).forEachIndexed { index, cards ->
            game.players[index].hand.addAll(cards)
            game.players[index].sortHand()
        }
    }
    
    /**
     * إنشاء مجموعة الأوراق
     */
    private fun generateDeck(): List<Card> {
        val deck = mutableListOf<Card>()
        Suit.values().forEach { suit ->
            Rank.values().forEach { rank ->
                deck.add(Card(suit, rank))
            }
        }
        return deck
    }
    
    // ========================================================================
    // BIDDING PHASE - مرحلة البدية
    // ========================================================================
    
    /**
     * وضع البدية
     */
    fun placeBid(playerId: Int, bid: Int): Boolean {
        val game = _gameState.value ?: return false
        
        val playerTeam = game.getTeamByPlayerId(playerId)
        
        if (!isValidBid(bid, playerTeam)) {
            _error.value = "بدية غير صحيحة (الحد الأدنى: ${getMinimumBid(playerTeam?.totalScore ?: 0)})"
            return false
        }
        
        val player = game.players.find { it.id == playerId } ?: return false
        player.bid = bid
        
        game.advancePlayer()
        _gameState.value = game
        
        // التحقق من انتهاء البدية
        val allBidded = game.players.all { it.bid > 0 }
        if (allBidded) {
            val team1TotalBid = game.team1.calculateBid()
            val team2TotalBid = game.team2.calculateBid()
            
            val minTotalBid = getMinimumTotalBid(game)
            if (team1TotalBid + team2TotalBid < minTotalBid) {
                resetBidsAndRedeal(game)
                return true
            }
            
            game.startPlaying()
            _gameState.value = game
        }
        
        // تشغيل AI للاعب التالي
        if (game.currentPlayer?.isAI == true && game.gamePhase == GamePhase.BIDDING) {
            executeAIAction(game)
        }
        
        return true
    }
    
    /**
     * التحقق من صحة البدية
     */
    private fun isValidBid(bid: Int, playerTeam: Team?): Boolean {
        if (bid < 2 || bid > 13) return false
        playerTeam ?: return false
        val minBid = getMinimumBid(playerTeam.totalScore)
        return bid >= minBid
    }
    
    /**
     * الحد الأدنى للبدية
     */
    private fun getMinimumBid(score: Int): Int = when {
        score >= 50 -> 5
        score >= 40 -> 4
        score >= 30 -> 3
        else -> 2
    }
    
    /**
     * الحد الأدنى لمجموع البديات
     */
    private fun getMinimumTotalBid(game: TarneebGame): Int {
        val maxScore = maxOf(game.team1.totalScore, game.team2.totalScore)
        return when {
            maxScore >= 50 -> 14
            maxScore >= 40 -> 13
            maxScore >= 30 -> 12
            else -> 11
        }
    }
    
    /**
     * إعادة التوزيع
     */
    private fun resetBidsAndRedeal(game: TarneebGame) {
        game.team1.resetBid()
        game.team2.resetBid()
        game.players.forEach { it.bid = 0 }
        
        dealCards(game)
        game.startBidding()
        
        _gameState.value = game
        
        if (game.currentPlayer?.isAI == true) {
            executeAIAction(game)
        }
    }
    
    // ========================================================================
    // PLAYING PHASE - مرحلة اللعب
    // ========================================================================
    
    /**
     * لعب ورقة
     */
    fun playCard(playerId: Int, card: Card): Boolean {
        val game = _gameState.value ?: return false
        
        if (game.gamePhase != GamePhase.PLAYING) {
            _error.value = "لا يمكن لعب ورقة الآن"
            return false
        }
        
        val player = game.players.find { it.id == playerId } ?: return false
        
        // التحقق من صحة اللعب
        if (!canPlayCard(card, player, game)) {
            _error.value = "ورقة غير صحيحة - يجب متابعة الرمز"
            return false
        }
        
        // إنشاء خدعة جديدة إذا لزم الأمر
        if (game.tricks.isEmpty() || game.currentTrick?.isComplete(game.players.size) == true) {
            game.currentTrickNumber++
            game.tricks.add(Trick(game.currentTrickNumber))
        }
        
        val currentTrick = game.currentTrick ?: return false
        
        player.removeCard(card)
        currentTrick.playCard(playerId, card)
        
        // إذا اكتملت الخدعة
        if (currentTrick.isComplete(game.players.size)) {
            val winnerId = calculateTrickWinner(currentTrick)
            currentTrick.winnerId = winnerId
            
            val winnerTeam = game.getTeamByPlayerId(winnerId)
            winnerTeam?.tricksWon = (winnerTeam?.tricksWon ?: 0) + 1
            
            game.currentPlayerIndex = winnerId
            
            // إذا انتهت الجولة
            if (game.tricks.size == 13) {
                endRound(game)
                return true
            }
        } else {
            game.advancePlayer()
        }
        
        _gameState.value = game
        
        // تشغيل AI للاعب التالي
        if (game.currentPlayer?.isAI == true && game.gamePhase == GamePhase.PLAYING) {
            executeAIAction(game)
        }
        
        return true
    }
    
    /**
     * التحقق من صحة لعب الورقة
     */
    private fun canPlayCard(card: Card, player: Player, game: TarneebGame): Boolean {
        if (!player.hand.contains(card)) return false
        
        val currentTrick = game.currentTrick ?: return true
        
        if (currentTrick.cardsPlayed.isEmpty()) return true
        
        val trickSuit = currentTrick.cardsPlayed.values.first().suit
        
        if (card.suit == trickSuit) return true
        
        if (player.hand.any { it.suit == trickSuit }) return false
        
        return true
    }
    
    /**
     * الحصول على الأوراق المسموحة
     */
    fun getValidCards(playerId: Int): List<Card> {
        val game = _gameState.value ?: return emptyList()
        val player = game.players.find { it.id == playerId } ?: return emptyList()
        val trick = game.currentTrick ?: return player.hand.toList()
        
        if (trick.cardsPlayed.isEmpty()) {
            return player.hand.toList()
        }
        
        val trickSuit = trick.cardsPlayed.values.first().suit
        val followSuitCards = player.hand.filter { it.suit == trickSuit }
        
        return if (followSuitCards.isNotEmpty()) {
            followSuitCards
        } else {
            player.hand.toList()
        }
    }
    
    /**
     * حساب فائز الخدعة
     */
    private fun calculateTrickWinner(trick: Trick): Int {
        if (trick.cardsPlayed.isEmpty()) return -1
        
        var winnerId = trick.playOrder.first()
        var winningCard = trick.cardsPlayed[winnerId]!!
        
        trick.playOrder.drop(1).forEach { playerId ->
            val currentCard = trick.cardsPlayed[playerId] ?: return@forEach
            
            // القلوب يفوز على كل شيء
            if (currentCard.suit == Suit.HEARTS && winningCard.suit != Suit.HEARTS) {
                winnerId = playerId
                winningCard = currentCard
            }
            // إذا كان كلاهما قلوب
            else if (currentCard.suit == Suit.HEARTS && winningCard.suit == Suit.HEARTS) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winnerId = playerId
                    winningCard = currentCard
                }
            }
            // إذا كان من نفس الرمز
            else if (currentCard.suit == winningCard.suit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winnerId = playerId
                    winningCard = currentCard
                }
            }
        }
        
        return winnerId
    }
    
    // ========================================================================
    // SCORING - حساب النقاط
    // ========================================================================
    
    /**
     * نهاية الجولة - حساب النقاط
     */
    private fun endRound(game: TarneebGame) {
        // حساب النقاط
        game.players.forEach { player ->
            val team = game.getTeamByPlayerId(player.id)
            val tricksWon = game.tricks.count { it.winnerId == player.id }
            val bid = player.bid
            
            val points = if (tricksWon >= bid) {
                calculatePoints(bid, team?.totalScore ?: 0)
            } else {
                -bid
            }
            
            team?.addScore(points)
        }
        
        // التحقق من الفائز
        if (game.team1.isWinner || game.team2.isWinner) {
            game.endGame()
        } else {
            game.endRound()
        }
        
        _gameState.value = game
    }
    
    /**
     * حساب النقاط
     */
    private fun calculatePoints(bid: Int, currentScore: Int): Int {
        val pointsTable = if (currentScore >= 30) {
            mapOf(
                2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6,
                7 to 14, 8 to 16, 9 to 27,
                10 to 40, 11 to 40, 12 to 40, 13 to 40
            )
        } else {
            mapOf(
                2 to 2, 3 to 3, 4 to 4, 5 to 10, 6 to 12,
                7 to 14, 8 to 16, 9 to 27,
                10 to 40, 11 to 40, 12 to 40, 13 to 40
            )
        }
        
        return pointsTable[bid] ?: 0
    }
    
    // ========================================================================
    // AI ENGINE - خوارزميات AI
    // ========================================================================
    
    /**
     * تنفيذ حركة AI
     */
    private fun executeAIAction(game: TarneebGame) {
        val currentPlayer = game.currentPlayer ?: return
        
        if (!currentPlayer.isAI) return
        
        when (game.gamePhase) {
            GamePhase.BIDDING -> {
                val bid = decideBid(currentPlayer, game)
                
                _aiAction.value = AIAction.PlacingBid(currentPlayer.id, bid)
                
                kotlin.runCatching {
                    Thread.sleep(1500)
                    placeBid(currentPlayer.id, bid)
                    _aiAction.value = null
                }
            }
            
            GamePhase.PLAYING -> {
                val validCards = getValidCards(currentPlayer.id)
                val card = decideCard(currentPlayer, game, validCards)
                
                _aiAction.value = AIAction.PlayingCard(currentPlayer.id, card)
                
                kotlin.runCatching {
                    Thread.sleep(1000)
                    playCard(currentPlayer.id, card)
                    _aiAction.value = null
                }
            }
            
            else -> Unit
        }
    }
    
    /**
     * قرار البدية للـ AI
     */
    private fun decideBid(player: Player, game: TarneebGame): Int {
        return when (player.difficulty) {
            AIDifficulty.EASY -> decideEasyBid(player, game)
            AIDifficulty.MEDIUM -> decideMediumBid(player, game)
            AIDifficulty.HARD -> decideHardBid(player, game)
        }
    }
    
    /**
     * بدية Easy
     */
    private fun decideEasyBid(player: Player, game: TarneebGame): Int {
        val minBid = getMinimumBid(game.getTeamByPlayerId(player.id)?.totalScore ?: 0)
        val highCards = player.hand.count { it.rank.value >= 10 }
        var baseBid = minBid + (highCards / 2)
        baseBid = baseBid.coerceIn(minBid, 13)
        val variance = random.nextInt(-1, 2)
        return (baseBid + variance).coerceIn(minBid, 13)
    }
    
    /**
     * بدية Medium
     */
    private fun decideMediumBid(player: Player, game: TarneebGame): Int {
        val minBid = getMinimumBid(game.getTeamByPlayerId(player.id)?.totalScore ?: 0)
        val team = game.getTeamByPlayerId(player.id) ?: return minBid
        
        val highCards = player.hand.count { it.rank.value >= 11 }
        val hearts = player.hand.filter { it.suit == Suit.HEARTS }
        val strongestSuit = player.hand.groupingBy { it.suit }.eachCount().values.maxOrNull() ?: 0
        
        var estimate = minBid + (highCards / 2) + (hearts.size / 2)
        
        if (strongestSuit >= 4) estimate += 2
        if (team.totalScore >= 30) estimate += 1
        if (team.totalScore >= 40) estimate += 1
        
        return estimate.coerceIn(minBid, 13)
    }
    
    /**
     * بدية Hard
     */
    private fun decideHardBid(player: Player, game: TarneebGame): Int {
        val minBid = getMinimumBid(game.getTeamByPlayerId(player.id)?.totalScore ?: 0)
        val team = game.getTeamByPlayerId(player.id) ?: return minBid
        val opponentTeam = game.getOpponentTeam(player.id) ?: return minBid
        
        val cardsByRank = player.hand.sortedByDescending { it.rank.value }
        
        val guaranteedWins = cardsByRank.takeWhile { 
            it.rank.value >= 12 || (it.suit == Suit.HEARTS && it.rank.value >= 8)
        }.size
        
        val potentialWins = cardsByRank.dropWhile { 
            it.rank.value >= 12 || (it.suit == Suit.HEARTS && it.rank.value >= 8)
        }.takeWhile { it.rank.value >= 8 || it.suit == Suit.HEARTS }.size
        
        var baseBid = minBid + (guaranteedWins / 2) + (potentialWins / 3)
        
        val scoreGap = team.totalScore - opponentTeam.totalScore
        
        if (scoreGap < -15) baseBid += 2
        else if (scoreGap > 15) baseBid -= 1
        
        return baseBid.coerceIn(minBid, 13)
    }
    
    /**
     * اختيار الورقة للـ AI
     */
    private fun decideCard(player: Player, game: TarneebGame, validCards: List<Card>): Card {
        if (validCards.isEmpty()) return player.hand.first()
        
        return when (player.difficulty) {
            AIDifficulty.EASY -> validCards.random(random)
            AIDifficulty.MEDIUM -> decideMediumCard(validCards, game)
            AIDifficulty.HARD -> decideHardCard(validCards, game, player)
        }
    }
    
    /**
     * اختيار ورقة Medium
     */
    private fun decideMediumCard(validCards: List<Card>, game: TarneebGame): Card {
        val trick = game.currentTrick ?: return validCards.first()
        
        if (trick.cardsPlayed.isEmpty()) {
            return validCards.sortedByDescending { it.rank.value }.getOrNull(validCards.size / 2) 
                ?: validCards.first()
        }
        
        val trickSuit = trick.cardsPlayed.values.first().suit
        val highestTrick = trick.cardsPlayed.values.maxByOrNull { it.rank.value }!!
        
        val canWin = validCards.any { card ->
            (card.suit == trickSuit && card.rank.value > highestTrick.rank.value) ||
            (trick.cardsPlayed.values.any { it.suit == Suit.HEARTS } && 
             card.suit == Suit.HEARTS && card.rank.value > highestTrick.rank.value)
        }
        
        return if (canWin) {
            validCards.filter { card ->
                (card.suit == trickSuit && card.rank.value > highestTrick.rank.value) ||
                (trick.cardsPlayed.values.any { it.suit == Suit.HEARTS } && 
                 card.suit == Suit.HEARTS && card.rank.value > highestTrick.rank.value)
            }.minByOrNull { it.rank.value } ?: validCards.first()
        } else {
            validCards.minByOrNull { it.rank.value } ?: validCards.first()
        }
    }
    
    /**
     * اختيار ورقة Hard
     */
    private fun decideHardCard(validCards: List<Card>, game: TarneebGame, player: Player): Card {
        val trick = game.currentTrick ?: return validCards.first()
        val team = game.getTeamByPlayerId(player.id) ?: return validCards.first()
        
        if (trick.cardsPlayed.isEmpty()) {
            val cardsBySuit = validCards.groupingBy { it.suit }.eachCount()
            val strongestSuit = cardsBySuit
                .filterKeys { it != Suit.HEARTS }
                .maxByOrNull { it.value }?.key ?: Suit.HEARTS
            
            val cardsInStrongestSuit = validCards.filter { it.suit == strongestSuit }
                .sortedByDescending { it.rank.value }
            
            return cardsInStrongestSuit.getOrNull(1) ?: cardsInStrongestSuit.first()
        }
        
        val currentTrickLeader = trick.cardsPlayed.maxByOrNull { it.value.rank.value }?.key
        val isTeammateLeading = currentTrickLeader?.let { id ->
            game.getTeamByPlayerId(id)?.id == team.id
        } ?: false
        
        if (isTeammateLeading) {
            return validCards.minByOrNull { it.rank.value } ?: validCards.first()
        } else {
            val trickSuit = trick.cardsPlayed.values.first().suit
            val highestInTrick = trick.cardsPlayed.values.maxByOrNull { it.rank.value }!!
            
            val winningCards = validCards.filter { card ->
                (card.suit == trickSuit && card.rank.value > highestInTrick.rank.value) ||
                (card.suit == Suit.HEARTS && highestInTrick.suit != Suit.HEARTS)
            }
            
            return if (winningCards.isNotEmpty()) {
                winningCards.minByOrNull { it.rank.value }!!
            } else {
                validCards.minByOrNull { it.rank.value }!!
            }
        }
    }
    
    // ========================================================================
    // GAME MANAGEMENT - إدارة اللعبة
    // ========================================================================
    
    /**
     * الجولة التالية
     */
    fun nextRound() {
        val game = _gameState.value ?: return
        
        if (game.gamePhase == GamePhase.ROUND_END) {
            game.startNextRound()
            
            val newGame = TarneebGame(
                team1 = game.team1,
                team2 = game.team2,
                players = game.players,
                gameMode = game.gameMode,
                humanPlayerCount = game.humanPlayerCount
            )
            
            dealCards(newGame)
            newGame.startBidding()
            _gameState.value = newGame
            
            if (newGame.currentPlayer?.isAI == true) {
                executeAIAction(newGame)
            }
        }
    }
    
    /**
     * إعادة تعيين اللعبة
     */
    fun resetGame() {
        _gameState.value = null
        _error.value = null
        _aiAction.value = null
    }
    
    /**
     * الحصول على معلومات اللعبة الحالية
     */
    fun getGameInfo(): String? {
        return _gameState.value?.getInfo()
    }
    
    /**
     * الحصول على يد اللاعب
     */
    fun getPlayerHand(playerId: Int): List<Card> {
        val game = _gameState.value ?: return emptyList()
        return game.players.find { it.id == playerId }?.hand?.toList() ?: emptyList()
    }
    
    /**
     * الحصول على معلومات لاعب
     */
    fun getPlayerInfo(playerId: Int): String? {
        val game = _gameState.value ?: return null
        val player = game.players.find { it.id == playerId } ?: return null
        
        return """
            |الاسم: ${player.name}
            |النوع: ${if (player.isAI) "AI" else "بشري"}
            |الأوراق: ${player.hand.size}
            |البدية: ${player.bid}
            |الخدعات: ${player.tricksWon}
            |النقاط: ${player.score}
        """.trimMargin()
    }
}

// ============================================================================
// USAGE EXAMPLES - أمثلة الاستخدام
// ============================================================================

/*
// Single Player مع AI
val engine = EngineGod()
engine.startSinglePlayer("أحمد", AIDifficulty.MEDIUM)

engine.gameState.collect { game ->
    println(game?.getInfo())
}

engine.placeBid(0, 7)
engine.playCard(0, card)

// Multiplayer Local
val engine = EngineGod()
engine.startMultiplayer(3, listOf("أحمد", "علي", "فاطمة"), AIDifficulty.MEDIUM)

engine.playCard(0, card)

// Multiplayer 4 بشريين
val engine = EngineGod()
engine.startMultiplayer(4, listOf("A", "B", "C", "D"), AIDifficulty.EASY)

engine.playCard(0, card)
*/
