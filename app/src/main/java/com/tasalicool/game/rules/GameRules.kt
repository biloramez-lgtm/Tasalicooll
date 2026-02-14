package com.tasalicool.game.rules

import com.tasalicool.game.model.GamePhase

/**
 * GameRules - قوانين مراحل اللعبة
 * 
 * المراحل الـ 5:
 * 1. DEALING - توزيع الأوراق
 * 2. BIDDING - البدية
 * 3. PLAYING - لعب الأوراق
 * 4. ROUND_END - نهاية الجولة
 * 5. GAME_END - نهاية اللعبة
 */
object GameRules {
    
    /**
     * التحقق من صحة الانتقال بين المراحل
     */
    fun canTransition(from: GamePhase, to: GamePhase): Boolean {
        return when (from) {
            GamePhase.DEALING -> to == GamePhase.BIDDING
            GamePhase.BIDDING -> to == GamePhase.PLAYING || to == GamePhase.DEALING
            GamePhase.PLAYING -> to == GamePhase.ROUND_END
            GamePhase.ROUND_END -> to == GamePhase.DEALING || to == GamePhase.GAME_END
            GamePhase.GAME_END -> to == GamePhase.DEALING
        }
    }
    
    /**
     * هل اللعبة نشطة (مستمرة)
     */
    fun isGameActive(phase: GamePhase): Boolean {
        return phase != GamePhase.GAME_END
    }
    
    /**
     * هل يمكن عمل بدية في هذه المرحلة
     */
    fun canBid(phase: GamePhase): Boolean = phase == GamePhase.BIDDING
    
    /**
     * هل يمكن لعب ورقة في هذه المرحلة
     */
    fun canPlayCard(phase: GamePhase): Boolean = phase == GamePhase.PLAYING
    
    // Constants
    const val TOTAL_PLAYERS = 4
    const val CARDS_PER_PLAYER = 13
    const val TOTAL_CARDS = 52
    const val TOTAL_TRICKS = 13
    const val WIN_SCORE = 41
}
