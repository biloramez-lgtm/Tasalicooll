package com.tasalicool.game.rules

import com.tasalicool.game.model.Player

/**
 * BiddingRules - قوانين البدية (Tarneeb)
 *
 * - البدية بين 2 و 13
 * - الحد الأدنى يرتفع بعد 30 نقطة
 * - مجموع البديات له حد أدنى
 */
object BiddingRules {

    /**
     * التحقق من صحة بدية لاعب واحد
     */
    fun isValidBid(
        bid: Int,
        handSize: Int,
        minimumBid: Int
    ): Boolean {
        if (bid <= 0) return false
        return bid in minimumBid..handSize
    }

    /**
     * الحد الأدنى للبدية حسب نقاط الفريق
     */
    fun getMinimumBid(teamScore: Int): Int =
        if (teamScore >= 30) 3 else 2

    /**
     * الحد الأدنى لمجموع البديات على الطاولة
     */
    fun getMinimumTotalBids(maxTeamScore: Int): Int =
        if (maxTeamScore >= 30) 12 else 11

    /**
     * التحقق من صحة مجموع البديات
     */
    fun isTotalBidsValid(
        totalBids: Int,
        maxTeamScore: Int
    ): Boolean {
        return totalBids >= getMinimumTotalBids(maxTeamScore)
    }

    /**
     * اقتراح بدية ذكية (AI / مساعد)
     *
     * يعتمد على:
     * - عدد الأوراق العالية
     * - أقوى لون بيد اللاعب
     */
    fun suggestBid(
        player: Player,
        minimumBid: Int
    ): Int {
        val hand = player.hand

        val highCards = hand.count { it.rank.value >= 11 } // J,Q,K,A
        val strongestSuit = hand
            .groupingBy { it.suit }
            .eachCount()
            .values
            .maxOrNull() ?: 0

        val estimate = (highCards / 2) + (strongestSuit / 2)

        return estimate.coerceIn(minimumBid, hand.size)
    }
}
