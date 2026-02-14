package com.tasalicool.game.rules

/**
 * ScoreCalculator - حساب النقاط
 * 
 * جدولين للنقاط:
 * 1. قبل 30 نقطة
 * 2. من 30 نقطة فما فوق
 */
object ScoreCalculator {
    
    /**
     * حساب نقاط البدية
     * @param bid البدية (2-13)
     * @param isAfter30 هل النقاط من 30 فما فوق
     * @return النقاط
     */
    fun getPointsForBid(bid: Int, isAfter30: Boolean): Int {
        return if (isAfter30) {
            getPointsAfter30(bid)
        } else {
            getPointsBefore30(bid)
        }
    }
    
    /**
     * جدول النقاط قبل 30 نقطة
     */
    private fun getPointsBefore30(bid: Int): Int = when (bid) {
        2 -> 2
        3 -> 3
        4 -> 4
        5 -> 10
        6 -> 12
        7 -> 14
        8 -> 16
        9 -> 27
        10, 11, 12, 13 -> 40
        else -> 0
    }
    
    /**
     * جدول النقاط من 30 نقطة فما فوق
     */
    private fun getPointsAfter30(bid: Int): Int = when (bid) {
        2 -> 2
        3 -> 3
        4 -> 4
        5 -> 5
        6 -> 6
        7 -> 14
        8 -> 16
        9 -> 27
        10, 11, 12, 13 -> 40
        else -> 0
    }
    
    /**
     * حساب النقاط مباشرة بناء على نقاط الفريق
     */
    fun getBidPoints(bid: Int, teamScore: Int): Int {
        val isAfter30 = teamScore >= 30
        return getPointsForBid(bid, isAfter30)
    }
    
    /**
     * عقوبة الفشل في البدية
     */
    fun getPenalty(bid: Int): Int = -bid
}
