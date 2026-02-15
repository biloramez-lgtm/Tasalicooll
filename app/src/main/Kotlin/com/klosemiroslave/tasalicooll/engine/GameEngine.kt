package com.klosemiroslave.tasalicooll.engine

import com.tarneeb.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * TarneebEngine - محرك لعبة Tarneeb 400 الكامل
 * 
 * القواعس:
 * - الهدف: وصول الفريق إلى 41 نقطة
 * - الحد الأدنى للبدية: 2 إلى 13
 * - القلوب (Hearts) هو الرمز الأقوى دائماً
 * - تتبع الرمز إجباري
 */
class TarneebEngine {
    
    private val _gameState = MutableStateFlow<TarneebGame?>(null)
    val gameState: StateFlow<TarneebGame?> = _gameState.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * بدء لعبة Tarneeb جديدة
     */
    fun startGame(team1: Team, team2: Team) {
        val players = team1.players + team2.players
        
        val game = TarneebGame(
            team1 = team1,
            team2 = team2,
            players = players
        )
        
        dealCards(game)
        game.startBidding()
        
        _gameState.value = game
    }
    
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
     * إنشاء مجموعة الأوراق (52 ورقة)
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
    
    /**
     * وضع البدية
     */
    fun placeBid(playerId: Int, bid: Int): Boolean {
        val game = _gameState.value ?: return false
        
        val playerTeam = game.getTeamByPlayerId(playerId)
        
        // التحقق من صحة البدية
        if (!isValidBid(bid, playerTeam)) {
            _error.value = "بدية غير صحيحة"
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
            
            // إذا كان المجموع أقل من الحد الأدنى، إعادة توزيع
            val minTotalBid = getMinimumTotalBid(game)
            if (team1TotalBid + team2TotalBid < minTotalBid) {
                resetBidsAndRedeal(game)
                return true
            }
            
            game.startPlaying()
            _gameState.value = game
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
     * الحد الأدنى للبدية حسب النقاط الحالية
     */
    private fun getMinimumBid(score: Int): Int {
        return when {
            score >= 50 -> 5
            score >= 40 -> 4
            score >= 30 -> 3
            else -> 2
        }
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
     * إعادة توزيع إذا كان المجموع قليل
     */
    private fun resetBidsAndRedeal(game: TarneebGame) {
        game.team1.resetBid()
        game.team2.resetBid()
        game.players.forEach { it.bid = 0 }
        
        dealCards(game)
        game.startBidding()
        
        _gameState.value = game
    }
    
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
            val newTrick = Trick(game.currentTrickNumber)
            game.tricks.add(newTrick)
        }
        
        val currentTrick = game.currentTrick ?: return false
        
        player.removeCard(card)
        currentTrick.playCard(playerId, card)
        
        // إذا اكتملت الخدعة
        if (currentTrick.isComplete(game.players.size)) {
            val winnerId = calculateTrickWinner(currentTrick)
            currentTrick.winnerId = winnerId
            
            // تحديث عدد الخدعات المكتسبة
            val winnerTeam = game.getTeamByPlayerId(winnerId)
            winnerTeam?.tricksWon = (winnerTeam?.tricksWon ?: 0) + 1
            
            game.currentPlayerIndex = winnerId
            
            // إذا انتهت الجولة (13 خدعة)
            if (game.tricks.size == 13) {
                endRound(game)
                return true
            }
        } else {
            game.advancePlayer()
        }
        
        _gameState.value = game
        return true
    }
    
    /**
     * التحقق من صحة لعب الورقة
     * القاعدة: يجب متابعة الرمز (suit) إن أمكن
     */
    private fun canPlayCard(card: Card, player: Player, game: TarneebGame): Boolean {
        if (!player.hand.contains(card)) return false
        
        val currentTrick = game.currentTrick ?: return true
        
        // إذا كانت أول ورقة
        if (currentTrick.cardsPlayed.isEmpty()) return true
        
        // الرمز الذي يجب متابعته
        val trickSuit = currentTrick.cardsPlayed.values.first().suit
        
        // إذا كانت الورقة من نفس الرمز
        if (card.suit == trickSuit) return true
        
        // إذا كان اللاعب لديه أوراق من الرمز المطلوب
        if (player.hand.any { it.suit == trickSuit }) return false
        
        // إذا لم يكن لديه من الرمز، يمكنه لعب أي ورقة
        return true
    }
    
    /**
     * حساب فائز الخدعة
     * 
     * القواعس:
     * 1. القلوب (Hearts) يفوز على كل الرموز الأخرى
     * 2. في نفس الرمز، الورقة الأعلى تفوز
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
            // إذا كان كلاهما قلوب، الأعلى يفوز
            else if (currentCard.suit == Suit.HEARTS && winningCard.suit == Suit.HEARTS) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winnerId = playerId
                    winningCard = currentCard
                }
            }
            // إذا كان من نفس الرمز (وليس قلوب)، الأعلى يفوز
            else if (currentCard.suit == winningCard.suit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winnerId = playerId
                    winningCard = currentCard
                }
            }
        }
        
        return winnerId
    }
    
    /**
     * نهاية الجولة - حساب النقاط
     */
    private fun endRound(game: TarneebGame) {
        // حساب النقاط لكل لاعب
        game.players.forEach { player ->
            val team = game.getTeamByPlayerId(player.id)
            val tricksWon = game.tricks.count { it.winnerId == player.id }
            val bid = player.bid
            
            val points = if (tricksWon >= bid) {
                // نجح في البدية
                calculatePoints(bid, team?.totalScore ?: 0)
            } else {
                // فشل في البدية
                -bid
            }
            
            team?.addScore(points)
        }
        
        // التحقق من فوز الفريق
        if (game.team1.isWinner || game.team2.isWinner) {
            game.endGame()
        } else {
            game.endRound()
        }
        
        _gameState.value = game
    }
    
    /**
     * حساب النقاط حسب البدية والنقاط الحالية
     */
    private fun calculatePoints(bid: Int, currentScore: Int): Int {
        val pointsTable = if (currentScore >= 30) {
            // جدول النقاط من 30 نقطة فما فوق
            mapOf(
                2 to 2,
                3 to 3,
                4 to 4,
                5 to 5,
                6 to 6,
                7 to 14,
                8 to 16,
                9 to 27,
                10 to 40,
                11 to 40,
                12 to 40,
                13 to 40
            )
        } else {
            // جدول النقاط قبل 30 نقطة
            mapOf(
                2 to 2,
                3 to 3,
                4 to 4,
                5 to 10,
                6 to 12,
                7 to 14,
                8 to 16,
                9 to 27,
                10 to 40,
                11 to 40,
                12 to 40,
                13 to 40
            )
        }
        
        return pointsTable[bid] ?: 0
    }
    
    /**
     * إنهاء اللعبة والبدء من جديد
     */
    fun nextRound() {
        val game = _gameState.value ?: return
        
        if (game.gamePhase == GamePhase.ROUND_END) {
            game.startNextRound()
            _gameState.value = game
            
            // توزيع الأوراق الجديدة
            val newGame = TarneebGame(
                team1 = game.team1,
                team2 = game.team2,
                players = game.players
            )
            dealCards(newGame)
            newGame.startBidding()
            _gameState.value = newGame
        }
    }
    
    /**
     * إعادة تعيين اللعبة
     */
    fun resetGame() {
        _gameState.value = null
        _error.value = null
    }
    
    /**
     * الحصول على معلومات اللعبة الحالية
     */
    fun getGameInfo(): String? {
        val game = _gameState.value ?: return null
        
        return """
            الفريق 1: ${game.team1.name} - ${game.team1.totalScore} نقطة
            الفريق 2: ${game.team2.name} - ${game.team2.totalScore} نقطة
            المرحلة: ${game.gamePhase.name}
            الجولة: ${game.currentRound}
        """.trimIndent()
    }
}
