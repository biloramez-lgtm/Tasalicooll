package com.tasalicool.game.rules

/**
 * RoundRules - قوانين الجولات
 * 
 * الجولة الواحدة = 13 خدعة
 */
object RoundRules {
    
    /**
     * عدد الخدعات في الجولة الواحدة
     */
    fun getTricksPerRound(): Int = 13
    
    /**
     * هل الجولة اكتملت
     */
    fun isRoundComplete(tricksPlayed: Int): Boolean {
        return tricksPlayed == getTricksPerRound()
    }
    
    /**
     * هل يمكن بدء جولة جديدة
     */
    fun canStartNewRound(isGameOver: Boolean): Boolean {
        return !isGameOver
    }
}
