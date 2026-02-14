package com.tasalicool.game.rules

import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Player
import com.tasalicool.game.model.Trick

/**
 * PlayRules - قوانين لعب الأوراق
 *
 * القاعدة الذهبية:
 * يجب تتبع لون الخدعة إن أمكن
 */
object PlayRules {

    /**
     * التحقق من صحة لعب الورقة
     */
    fun canPlayCard(
        card: Card,
        player: Player,
        trick: Trick
    ): Boolean {

        // أول لاعب في الخدعة يلعب أي ورقة
        if (trick.cards.isEmpty()) return true

        val trickSuit = trick.trickSuit ?: return true

        // إذا اللاعب قادر يتبع اللون → يجب يتبعه
        return if (player.canFollowSuit(trickSuit)) {
            card.suit == trickSuit
        } else {
            true
        }
    }

    /**
     * إرجاع الأوراق المسموح لعبها
     */
    fun getValidCards(
        player: Player,
        trick: Trick
    ): List<Card> {

        // أول لاعب
        if (trick.cards.isEmpty()) {
            return player.hand.toList()
        }

        val trickSuit = trick.trickSuit ?: return player.hand.toList()

        val followSuitCards = player.hand.filter { it.suit == trickSuit }

        return if (followSuitCards.isNotEmpty()) {
            followSuitCards
        } else {
            player.hand.toList()
        }
    }
}
