package com.tasalicool.game.rules

import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Player
import com.tasalicool.game.model.Trick

/**
 * CardRules - قوانين لعب الأوراق
 *
 * مسؤول عن:
 * - التحقق من صحة اللعب
 * - حساب فائز الخدعة
 * - الحصول على الأوراق المسموح لعبها
 */
object CardRules {

    /**
     * التحقق من صحة لعب الورقة
     */
    fun canPlayCard(
        card: Card,
        player: Player,
        trick: Trick
    ): Boolean {
        // التحقق من أن الورقة موجودة في يد اللاعب
        if (!player.hand.contains(card)) return false

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
     * الحصول على الأوراق المسموح لعبها
     */
    fun getValidPlayableCards(
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

    /**
     * حساب فائز الخدعة
     * @return player index للاعب الفائز
     */
    fun calculateTrickWinner(trick: Trick): Int {
        if (trick.cards.isEmpty()) return -1

        val trickSuit = trick.trickSuit ?: return trick.cards.keys.first()

        var highestCard = trick.cards.values.first()
        var winnerId = trick.cards.keys.first()

        trick.cards.forEach { (playerId, card) ->
            if (card.suit == trickSuit) {
                // التحقق من أعلى ورقة من نفس اللون
                if (card.rank.value > highestCard.rank.value) {
                    highestCard = card
                    winnerId = playerId
                }
            }
        }

        return winnerId
    }

    /**
     * الحصول على لون الخدعة
     */
    fun getTrickSuit(trick: Trick) = trick.trickSuit
}
