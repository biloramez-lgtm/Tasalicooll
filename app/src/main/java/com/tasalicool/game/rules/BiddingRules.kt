package com.tasalicool.game.rules

import com.tasalicool.game.model.Player

/**
 * BiddingRules - تحقق من صحة البدية
 * 
 * قوانين Tarneeb:
 * - البدية من 2-13
 * - الحد الأدنى يزداد حسب النقاط
 * - المجموع يجب يكون فوق الحد الأدنى
 */
object BiddingRules {
    
    /**
     * التحقق من صحة البدية
     * @param bid البدية (2-13)
     * @param handSize عدد الأوراق
     * @param minimumBid الحد الأدنى
     * @return صحيح أو خطأ
     */
    fun isValidBid(bid: Int, handSize: Int, minimumBid: Int): Boolean {
        return bid in minimumBid..handSize
    }
    
    /**
     * الحد الأدنى للبدية حسب نقاط الفريق
     */
    fun getMinimumBid(teamScore: Int): Int = when {
        teamScore >= 30 -> 3
        else -> 2
    }
    
    /**
     * الحد الأدنى لمجموع البديات
     */
    fun getMinimumTotalBids(maxTeamScore: Int): Int = when {
        maxTeamScore >= 30 -> 12
        else -> 11
    }
    
    /**
     * التحقق من صحة المجموع
     */
    fun isTotalBidsValid(totalBids: Int, maxTeamScore: Int): Boolean {
        return totalBids >= getMinimumTotalBids(maxTeamScore)
    }
    
    /**
     * اقتراح بدية ذكية بناء على الأوراق
     */
    fun suggestBid(player: Player, minimumBid: Int): Int {
        val highCardCount = player.hand.count { it.rank.value >= 10 }
        val maxSuitCount = player.hand.groupingBy { it.suit }.eachCount().values.maxOrNull() ?: 0
        var estimate = (highCardCount / 2) + (maxSuitCount / 3)
        return estimate.coerceIn(minimumBid, player.hand.size)
    }
}
