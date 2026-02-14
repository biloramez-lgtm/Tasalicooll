package com.tasalicool.game.model

/**
 * Represents the current phase of the game
 * 
 * Game Flow:
 * DEALING → BIDDING → PLAYING → ROUND_END → (repeat or GAME_END)
 */
enum class GamePhase {
    /**
     * DEALING Phase
     * 
     * مرحلة توزيع الأوراق
     * 
     * What happens:
     * - الكازي (Dealer) يوزع الأوراق
     * - كل لاعب يأخذ 13 ورقة
     * - الأوراق توزع واحدة واحدة بالدور
     * - اللاعبون يرتبون أوراقهم
     * 
     * Duration: ~30 seconds
     * Next Phase: BIDDING
     * 
     * Example:
     * Player 0: [2♥, 3♦, 5♠, 7♣, ...]
     * Player 1: [4♥, 6♦, 8♠, 9♣, ...]
     * Player 2: [A♥, K♦, Q♠, J♣, ...]
     * Player 3: [10♥, 9♦, 8♣, 7♠, ...]
     */
    DEALING {
        override fun getDisplayName(): String = "Dealing Cards"
        override fun getDescription(): String = "Distributing 13 cards to each player"
        override fun canBid(): Boolean = false
        override fun canPlayCard(): Boolean = false
        override fun isGameActive(): Boolean = true
    },

    /**
     * BIDDING Phase
     * 
     * مرحلة البدية
     * 
     * What happens:
     * - كل لاعب يقول كم خدعة بينربح (2-13)
     * - البدية تبدأ من اليمين من الكازي
     * - يجب أن يكون المجموع ≥ 11 (قبل 30 نقاط)
     * - إذا المجموع أقل، يعاد التوزيع
     * 
     * Duration: ~2-3 minutes
     * Next Phase: PLAYING (إذا كان المجموع صحيح) أو DEALING (إعادة توزيع)
     * 
     * Bidding Order:
     * 1. Right of Dealer (المحاول)
     * 2. Second player
     * 3. Third player
     * 4. Dealer (الكازي)
     * 
     * Rules:
     * - Minimum bid: 2 (إذا النقاط < 30)
     * - Maximum bid: 13 (عدد الخدعات)
     * - الحد الأدنى للمجموع:
     *   * 0-29 نقطة: 11
     *   * 30-39 نقطة: 12
     *   * 40-49 نقطة: 13
     *   * 50+ نقطة: 14
     * 
     * Example:
     * Player 1: "7"  ✓
     * Player 2: "6"  ✓
     * Player 3: "8"  ✓
     * Player 0: "5"  ✓
     * Total: 26 (≥ 11) → GO TO PLAYING
     * 
     * OR
     * 
     * Player 1: "2"  ✓
     * Player 2: "2"  ✓
     * Player 3: "2"  ✓
     * Player 0: "2"  ✓
     * Total: 8 (< 11) → RE-DEAL
     */
    BIDDING {
        override fun getDisplayName(): String = "Bidding"
        override fun getDescription(): String = "Players announce their bids"
        override fun canBid(): Boolean = true
        override fun canPlayCard(): Boolean = false
        override fun isGameActive(): Boolean = true
    },

    /**
     * PLAYING Phase
     * 
     * مرحلة لعب الأوراق
     * 
     * What happens:
     * - اللاعبون يلعبون أوراقهم
     * - 13 خدعة في المجموع (كل خدعة = 4 أوراق)
     * - المحاول الأول هو اللي يمين الكازي
     * - كل لاعب يرمي ورقة واحدة بدوره
     * - الرابح يبدأ الخدعة اللي بعدها
     * 
     * Duration: ~10-15 minutes
     * Next Phase: ROUND_END
     * 
     * Rules:
     * - يجب تتبع اللون (Suit) إذا في يدك
     * - إذا ما في اللون، ممكن ترمي أي ورقة
     * - القلب (Hearts) دائماً Trump
     * - أعلى قلب يربح الخدعة
     * - إذا ما في قلب، أعلى ورقة من نفس اللون يربح
     * 
     * Trick Sequence:
     * Trick 1:
     *   Player 1: 5♦
     *   Player 2: K♦ (أعلى ماسة)
     *   Player 3: 2♥ (قلب يربح!)
     *   Player 0: A♣
     *   WINNER: Player 3
     * 
     * Trick 2:
     *   Player 3: 7♠ (الرابح يبدأ)
     *   Player 0: 9♠
     *   Player 1: 3♠
     *   Player 2: Q♠ (أعلى سباتة)
     *   WINNER: Player 2
     * 
     * ... التكرار حتى 13 خدعة
     */
    PLAYING {
        override fun getDisplayName(): String = "Playing"
        override fun getDescription(): String = "Players playing their cards"
        override fun canBid(): Boolean = false
        override fun canPlayCard(): Boolean = true
        override fun isGameActive(): Boolean = true
    },

    /**
     * ROUND_END Phase
     * 
     * مرحلة نهاية الجولة
     * 
     * What happens:
     * - حساب عدد الخدعات لكل فريق
     * - تحديد إذا الفريق قابل بديته أو لا
     * - حساب النقاط
     * - إضافة النقاط للنتيجة
     * - التحقق من الفوز
     * 
     * Duration: ~5 seconds
     * Next Phase: GAME_END (إذا حد فاز) أو DEALING (جولة جديدة)
     * 
     * Scoring Calculation:
     * 
     * قبل 30 نقطة:
     * ├── بدية 2→4: نقاط = البدية
     * ├── بدية 5: 10
     * ├── بدية 6: 12
     * ├── بدية 7: 14
     * ├── بدية 8: 16
     * ├── بدية 9: 27
     * └── بدية 10-13: 40
     * 
     * من 30 نقطة فما فوق:
     * ├── بدية 2→6: نقاط = البدية
     * ├── بدية 7: 14
     * ├── بدية 8: 16
     * ├── بدية 9: 27
     * └── بدية 10-13: 40
     * 
     * إذا فشل الفريق:
     * └── يخسر البدية (سالب)
     * 
     * Example:
     * Team 1:
     *   Bid: 7 + 6 = 13
     *   Tricks Won: 8
     *   Bid Met: NO (8 < 13)
     *   Score Added: -(7 + 6) = -13
     *   Previous Score: 20
     *   New Score: 7
     * 
     * Team 2:
     *   Bid: 8 + 5 = 13
     *   Tricks Won: 5 (13 - 8 = 5)
     *   Bid Met: NO (5 < 13)
     *   Score Added: -(8 + 5) = -13
     *   Previous Score: 25
     *   New Score: 12
     */
    ROUND_END {
        override fun getDisplayName(): String = "Round End"
        override fun getDescription(): String = "Calculating scores"
        override fun canBid(): Boolean = false
        override fun canPlayCard(): Boolean = false
        override fun isGameActive(): Boolean = false
    },

    /**
     * GAME_END Phase
     * 
     * مرحلة نهاية اللعبة
     * 
     * What happens:
     * - واحد من الفريقين وصل 41 نقطة أو أكثر
     * - اللعبة انتهت
     * - إعلان الفريق الرابح
     * - عرض الإحصائيات
     * 
     * Duration: ∞
     * Next Phase: DEALING (لعبة جديدة) أو انتهاء البرنامج
     * 
     * Win Condition:
     * - Score ≥ 41
     * - كل لاعب في الفريق عنده نقطة ≥ 1
     * 
     * Example:
     * Team 1:
     *   Player 0: 30 points
     *   Player 2: 15 points
     *   Total: 45 points ≥ 41 ✓
     *   Both > 0 ✓
     *   WINNER: Team 1 🏆
     * 
     * Statistics:
     * - Total Rounds: 15
     * - Final Score Team 1: 45
     * - Final Score Team 2: 38
     * - Duration: 45 minutes
     * - Top Player: Player 0 (30 points)
     */
    GAME_END {
        override fun getDisplayName(): String = "Game End"
        override fun getDescription(): String = "Game is over"
        override fun canBid(): Boolean = false
        override fun canPlayCard(): Boolean = false
        override fun isGameActive(): Boolean = false
    };

    // ==================== ABSTRACT METHODS ====================

    /**
     * الاسم المعروض للمستخدم
     */
    abstract fun getDisplayName(): String

    /**
     * الوصف التفصيلي للمرحلة
     */
    abstract fun getDescription(): String

    /**
     * هل يمكن للاعب أن يقول بدية في هذه المرحلة؟
     */
    abstract fun canBid(): Boolean

    /**
     * هل يمكن للاعب أن يلعب ورقة في هذه المرحلة؟
     */
    abstract fun canPlayCard(): Boolean

    /**
     * هل اللعبة نشطة (مستمرة)؟
     */
    abstract fun isGameActive(): Boolean

    // ==================== UTILITY METHODS ====================

    /**
     * الانتقال للمرحلة التالية
     */
    fun getNextPhase(): GamePhase {
        return when (this) {
            DEALING -> BIDDING
            BIDDING -> PLAYING
            PLAYING -> ROUND_END
            ROUND_END -> DEALING  // أو GAME_END إذا حد فاز
            GAME_END -> DEALING   // لعبة جديدة
        }
    }

    /**
     * التحقق من أن هذه مرحلة معينة
     */
    fun isDealingPhase(): Boolean = this == DEALING
    fun isBiddingPhase(): Boolean = this == BIDDING
    fun isPlayingPhase(): Boolean = this == PLAYING
    fun isRoundEndPhase(): Boolean = this == ROUND_END
    fun isGameEndPhase(): Boolean = this == GAME_END

    /**
     * الحصول على رقم المرحلة (للترتيب)
     */
    fun getPhaseNumber(): Int {
        return when (this) {
            DEALING -> 1
            BIDDING -> 2
            PLAYING -> 3
            ROUND_END -> 4
            GAME_END -> 5
        }
    }

    /**
     * هل يمكن الانتقال من هذه المرحلة للمرحلة المطلوبة؟
     */
    fun canTransitionTo(targetPhase: GamePhase): Boolean {
        return when (this) {
            DEALING -> targetPhase == BIDDING
            BIDDING -> targetPhase == PLAYING || targetPhase == DEALING  // إعادة توزيع
            PLAYING -> targetPhase == ROUND_END
            ROUND_END -> targetPhase == DEALING || targetPhase == GAME_END
            GAME_END -> targetPhase == DEALING
        }
    }

    /**
     * الحصول على المعلومات الكاملة عن المرحلة
     */
    fun getFullInfo(): String {
        return """
            ╔═════════════════════════════════╗
            ║      ${getDisplayName().padEnd(26)}║
            ╚═════════════════════════════════╝
            
            Description: ${getDescription()}
            
            State:
            ├── Game Active: ${isGameActive()}
            ├── Can Bid: ${canBid()}
            ├── Can Play Card: ${canPlayCard()}
            ├── Phase Number: ${getPhaseNumber()}
            └── Next Phase: ${getNextPhase().getDisplayName()}
            
            Actions Available:
            ${if (canBid()) "✓ Place Bid" else "✗ Place Bid"}
            ${if (canPlayCard()) "✓ Play Card" else "✗ Play Card"}
        """.trimIndent()
    }

    /**
     * الحصول على السياق (Context) للمرحلة
     */
    fun getContext(): String {
        return when (this) {
            DEALING -> "الكازي يوزع 13 ورقة لكل لاعب"
            BIDDING -> "اللاعبون يعلنون بدياتهم (2-13)"
            PLAYING -> "اللاعبون يلعبون أوراقهم (13 خدعات)"
            ROUND_END -> "حساب النقاط والتحقق من الفوز"
            GAME_END -> "اللعبة انتهت - فريق واحد وصل 41+ نقطة"
        }
    }
}
