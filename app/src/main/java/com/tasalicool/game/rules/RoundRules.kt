package com.tasalicool.game.rules

/**
 * RoundRules - قوانين الجولات
 *
 * الجولة الواحدة = 13 خدعة
 */
object RoundRules {

    private const val TRICKS_PER_ROUND = 13

    /**
     * عدد الخدعات في الجولة
     */
    fun getTricksPerRound(): Int = TRICKS_PER_ROUND

    /**
     * هل الجولة اكتملت
     */
    fun isRoundComplete(tricksPlayed: Int): Boolean {
        return tricksPlayed >= TRICKS_PER_ROUND
    }

    /**
     * هل يمكن بدء جولة جديدة
     * (طالما اللعبة لم تنتهِ)
     */
    fun canStartNewRound(isGameOver: Boolean): Boolean {
        return !isGameOver
    }
}
