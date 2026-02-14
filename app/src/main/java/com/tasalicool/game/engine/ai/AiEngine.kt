package com.tasalicool.game.engine.ai

import com.tasalicool.game.model.*
import kotlin.math.min

/**
 * AI Engine - محرك الذكاء الاصطناعي
 * 
 * المسؤولية الوحيدة:
 * ✅ اتخاذ قرارات (bid أو card)
 * ✅ تحليل سياق اللعبة
 * ✅ NOT mutate game state
 * ✅ NOT call rules directly
 * ✅ NOT know about game structure
 * 
 * ما يعرفه فقط:
 * - يده من الأوراق
 * - الأوراق المسموحة
 * - معلومات اللعبة (read-only)
 */
class AiEngine {
    
    enum class DifficultyLevel {
        EASY,    // عشوائي
        MEDIUM,  // استراتيجي بسيط
        HARD     // تحليل متقدم
    }
    
    // ==================== BID DECISION ====================
    
    /**
     * تقرر البدية
     * 
     * المدخلات:
     * - يد اللاعب فقط
     * - نقاط الفريق
     * - نقاط الخصم
     * - الحد الأدنى للبدية (محسوب بـ Rules من بره)
     * 
     * المخرج:
     * - رقم البدية فقط
     */
    fun decideBid(
        hand: List<Card>,
        teamScore: Int,
        opponentScore: Int,
        minimumBid: Int,
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM
    ): Int {
        return when (difficulty) {
            DifficultyLevel.EASY -> decideBidEasy(hand, minimumBid)
            DifficultyLevel.MEDIUM -> decideBidMedium(hand, minimumBid, teamScore, opponentScore)
            DifficultyLevel.HARD -> decideBidHard(hand, minimumBid, teamScore, opponentScore)
        }
    }
    
    /**
     * بدية سهلة = عشوائية تقريباً
     */
    private fun decideBidEasy(hand: List<Card>, minimumBid: Int): Int {
        val maxBid = hand.size
        return (minimumBid..maxBid).random()
    }
    
    /**
     * بدية متوسطة = تحليل بسيط
     */
    private fun decideBidMedium(
        hand: List<Card>,
        minimumBid: Int,
        teamScore: Int,
        opponentScore: Int
    ): Int {
        val maxBid = hand.size
        val handStrength = calculateHandStrength(hand)
        
        // كل 10% قوة = اضف 1 للبدية
        val estimatedBid = minimumBid + (handStrength / 10)
        
        return estimatedBid.coerceIn(minimumBid, maxBid)
    }
    
    /**
     * بدية صعبة = تحليل متقدم
     */
    private fun decideBidHard(
        hand: List<Card>,
        minimumBid: Int,
        teamScore: Int,
        opponentScore: Int
    ): Int {
        val maxBid = hand.size
        val handStrength = calculateHandStrength(hand)
        
        // احسب الحاجة (كم نقطة نحتاج للفوز)
        val pointsNeeded = 41 - teamScore
        val pointsLeading = teamScore - opponentScore
        
        // إذا نحتاج نقاط كتير، نبدي أعلى
        val riskFactor = if (pointsNeeded > 15) 1.2f else 0.8f
        
        // إذا الخصم قريب من الفوز، نبدي أقل (محافظ)
        val defenseFactor = if (opponentScore > 35) 0.7f else 1.0f
        
        val estimatedBid = (minimumBid + (handStrength * riskFactor * defenseFactor / 10)).toInt()
        
        return estimatedBid.coerceIn(minimumBid, maxBid)
    }
    
    /**
     * احسب قوة اليد (0-100)
     * 
     * بناء على:
     * - الأوراق العالية
     * - توزيع الألوان
     * - الترامب (القلب)
     */
    private fun calculateHandStrength(hand: List<Card>): Int {
        var strength = 0
        
        // 1. الأوراق العالية (الآسات والملوك والكوينات)
        val highCards = hand.count { it.rank.value >= 12 }
        strength += highCards * 15
        
        // 2. العاشرات والولادات
        val mediumCards = hand.count { it.rank.value in 10..11 }
        strength += mediumCards * 8
        
        // 3. توزيع الألوان (كم لون عندك منه الكثير)
        val suitDistribution = hand.groupingBy { it.suit }.eachCount()
        val maxSuitCount = suitDistribution.values.maxOrNull() ?: 0
        strength += min(maxSuitCount * 5, 30)  // max 30 نقطة
        
        // 4. الملكات والولادات للقلب (الترامب)
        val heartCards = hand.filter { it.suit == Suit.HEARTS }
        val heartStrength = heartCards.sumOf { if (it.rank.value >= 12) 10 else it.rank.value / 2 }
        strength += min(heartStrength, 25)  // max 25 نقطة
        
        return strength.coerceIn(0, 100)
    }
    
    // ==================== CARD PLAY DECISION ====================
    
    /**
     * تقرر الورقة اللي تلعب
     * 
     * المدخلات:
     * - يد اللاعب
     * - الأوراق المسموحة فقط (محسوبة بـ PlayRules من بره)
     * - الأوراق الملعوبة في الخدعة الحالية
     * - رقم الخدعة الحالية
     * - قيمة الخدعة الحالية (من يربح)
     * - معلومات الفريق (read-only)
     * 
     * المخرج:
     * - الورقة اللي تلعب فقط
     */
    fun decideCard(
        hand: List<Card>,
        validCards: List<Card>,
        trickSuit: Suit?,
        playedCards: Map<Int, Card>,
        trickNumber: Int,
        teamScore: Int,
        opponentScore: Int,
        tricksBidded: Int,
        tricksWon: Int,
        currentTrickWinnerId: Int?,
        playerId: Int,
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM
    ): Card {
        require(validCards.isNotEmpty()) { "No valid cards!" }
        
        return when (difficulty) {
            DifficultyLevel.EASY -> decideCardEasy(validCards)
            DifficultyLevel.MEDIUM -> decideCardMedium(
                validCards,
                playedCards,
                trickSuit,
                currentTrickWinnerId,
                playerId
            )
            DifficultyLevel.HARD -> decideCardHard(
                validCards,
                playedCards,
                trickSuit,
                currentTrickWinnerId,
                playerId,
                teamScore,
                opponentScore,
                tricksBidded,
                tricksWon
            )
        }
    }
    
    /**
     * لعب سهل = عشوائي من الأوراق الصحيحة
     */
    private fun decideCardEasy(validCards: List<Card>): Card {
        return validCards.randomOrNull() ?: validCards.first()
    }
    
    /**
     * لعب متوسط = تحليل بسيط
     */
    private fun decideCardMedium(
        validCards: List<Card>,
        playedCards: Map<Int, Card>,
        trickSuit: Suit?,
        currentTrickWinnerId: Int?,
        playerId: Int
    ): Card {
        // إذا أول لاعب يلعب
        if (playedCards.isEmpty()) {
            // العب أقل ورقة (احفظ الأوراق القوية)
            return validCards.minByOrNull { it.rank.value } ?: validCards.first()
        }
        
        // إذا أنت الرابح حالياً
        val isCurrentlyWinning = currentTrickWinnerId == playerId
        
        return if (isCurrentlyWinning) {
            // لعب أقل ورقة تربح (احفظ الأوراق)
            validCards.minByOrNull { it.rank.value } ?: validCards.first()
        } else {
            // لعب أقل ورقة (احفظ الأوراق)
            validCards.minByOrNull { it.rank.value } ?: validCards.first()
        }
    }
    
    /**
     * لعب صعب = تحليل متقدم استراتيجي
     */
    private fun decideCardHard(
        validCards: List<Card>,
        playedCards: Map<Int, Card>,
        trickSuit: Suit?,
        currentTrickWinnerId: Int?,
        playerId: Int,
        teamScore: Int,
        opponentScore: Int,
        tricksBidded: Int,
        tricksWon: Int
    ): Card {
        // احسب كم خدعة بقت
        val tricksRemaining = 13 - (tricksWon + playedCards.size / 4)
        val tricksNeeded = tricksBidded - tricksWon
        
        return when {
            // إذا أول لاعب (Leader)
            playedCards.isEmpty() -> {
                decideLeaderCard(validCards, teamScore, opponentScore, tricksNeeded)
            }
            
            // إذا آخر لاعب (Closer)
            playedCards.size == 3 -> {
                decideCloserCard(
                    validCards,
                    playedCards,
                    currentTrickWinnerId,
                    playerId,
                    tricksNeeded,
                    tricksRemaining
                )
            }
            
            // إذا في الوسط
            else -> {
                decideMiddleCard(
                    validCards,
                    playedCards,
                    currentTrickWinnerId,
                    playerId,
                    tricksNeeded,
                    tricksRemaining
                )
            }
        }
    }
    
    /**
     * قرار اللاعب الأول (Leader)
     */
    private fun decideLeaderCard(
        validCards: List<Card>,
        teamScore: Int,
        opponentScore: Int,
        tricksNeeded: Int
    ): Card {
        val pointsNeeded = 41 - teamScore
        val pointsLeading = teamScore - opponentScore
        
        return if (pointsNeeded > 10) {
            // بحاجة نقاط، اعرض الأوراق القوية
            validCards.maxByOrNull { it.rank.value } ?: validCards.first()
        } else if (pointsLeading < 0) {
            // متأخر، لعب ورقة قوية
            validCards.maxByOrNull { it.rank.value } ?: validCards.first()
        } else {
            // متقدم، لعب ورقة ضعيفة (احفظ الأوراق)
            validCards.minByOrNull { it.rank.value } ?: validCards.first()
        }
    }
    
    /**
     * قرار آخر لاعب (Closer)
     */
    private fun decideCloserCard(
        validCards: List<Card>,
        playedCards: Map<Int, Card>,
        currentTrickWinnerId: Int?,
        playerId: Int,
        tricksNeeded: Int,
        tricksRemaining: Int
    ): Card {
        val isTeamWinning = currentTrickWinnerId != null && currentTrickWinnerId != playerId
        
        return if (isTeamWinning) {
            // فريقنا رابح، لعب أقل ورقة
            validCards.minByOrNull { it.rank.value } ?: validCards.first()
        } else {
            // الفريق الآخر رابح، حاول تربح إذا ممكن
            val winningCard = playedCards.values.maxByOrNull { it.rank.value }
            
            if (winningCard != null && canBeatCard(validCards, winningCard)) {
                // تقدر تربح، لعب أقل ورقة تربح
                findMinWinningCard(validCards, winningCard)
                    ?: validCards.minByOrNull { it.rank.value } ?: validCards.first()
            } else {
                // ما تقدر تربح، لعب أقل ورقة
                validCards.minByOrNull { it.rank.value } ?: validCards.first()
            }
        }
    }
    
    /**
     * قرار لاعب في الوسط
     */
    private fun decideMiddleCard(
        validCards: List<Card>,
        playedCards: Map<Int, Card>,
        currentTrickWinnerId: Int?,
        playerId: Int,
        tricksNeeded: Int,
        tricksRemaining: Int
    ): Card {
        val isTeamWinning = currentTrickWinnerId != null && currentTrickWinnerId != playerId
        
        return if (isTeamWinning) {
            // فريقنا رابح، لعب أقل ورقة
            validCards.minByOrNull { it.rank.value } ?: validCards.first()
        } else {
            // الفريق الآخر رابح
            // إذا نحتاج الخدعة، اعرض قوة
            if (tricksNeeded > 0 && tricksRemaining > 2) {
                validCards.maxByOrNull { it.rank.value } ?: validCards.first()
            } else {
                // ما نحتاج أو بقيت خدعات كتير، احفظ الأوراق
                validCards.minByOrNull { it.rank.value } ?: validCards.first()
            }
        }
    }
    
    /**
     * هل الورقة تقدر تربح
     */
    private fun canBeatCard(validCards: List<Card>, winningCard: Card): Boolean {
        return validCards.any { it.rank.value > winningCard.rank.value }
    }
    
    /**
     * احصل على أقل ورقة تربح
     */
    private fun findMinWinningCard(validCards: List<Card>, winningCard: Card): Card? {
        return validCards
            .filter { it.rank.value > winningCard.rank.value }
            .minByOrNull { it.rank.value }
    }
}
