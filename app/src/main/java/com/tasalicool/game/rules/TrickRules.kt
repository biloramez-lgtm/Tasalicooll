package com.tasalicool.game.rules

import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Suit
import com.tasalicool.game.model.Trick

/**
 * TrickRules - قوانين الخدعة (Trick Engine)
 *
 * القاعدة الذهبية:
 * ♥ (القلب) هو الترامب دائماً
 * أعلى قلب يربح أي خدعة
 */
object TrickRules {

    private const val PLAYERS_PER_TRICK = 4

    /**
     * عدد اللاعبين في الخدعة
     */
    fun getPlayersPerTrick(): Int = PLAYERS_PER_TRICK

    /**
     * هل الخدعة اكتملت
     */
    fun isTrickComplete(trick: Trick): Boolean {
        return trick.isComplete(PLAYERS_PER_TRICK)
    }

    /**
     * حلّ الخدعة بالكامل
     * - يحسب الرابح
     * - يثبت winnerId داخل Trick
     */
    fun resolveTrick(trick: Trick, trumpSuit: Suit = Suit.HEARTS): Int {
        val winner = calculateWinner(trick, trumpSuit)
        trick.winnerId = winner
        return winner
    }

    /**
     * حساب رابح الخدعة
     */
    fun calculateWinner(trick: Trick, trumpSuit: Suit = Suit.HEARTS): Int {
        if (trick.cards.isEmpty() || trick.playOrder.isEmpty()) return -1

        var winnerId = trick.playOrder.first()
        var winningCard = trick.cards[winnerId] ?: return -1

        for (playerId in trick.playOrder.drop(1)) {
            val currentCard = trick.cards[playerId] ?: continue

            if (canBeat(
                    card = currentCard,
                    otherCard = winningCard,
                    trickSuit = trick.trickSuit,
                    trumpSuit = trumpSuit
                )
            ) {
                winningCard = currentCard
                winnerId = playerId
            }
        }

        return winnerId
    }

    /**
     * الترامب دائماً قلب
     */
    fun getTrumpSuit(): Suit = Suit.HEARTS

    /**
     * هل الورقة ترامب
     */
    fun isTrump(card: Card): Boolean = card.suit == getTrumpSuit()

    /**
     * هل card تغلب otherCard
     */
    fun canBeat(
        card: Card,
        otherCard: Card,
        trickSuit: Suit?,
        trumpSuit: Suit = Suit.HEARTS
    ): Boolean {

        // Trump يربح non-trump
        if (card.suit == trumpSuit && otherCard.suit != trumpSuit) return true

        // أعلى trump يربح trump
        if (card.suit == trumpSuit && otherCard.suit == trumpSuit) {
            return card.rank.value > otherCard.rank.value
        }

        // نفس لون الخدعة يربح غيره
        if (
            trickSuit != null &&
            card.suit == trickSuit &&
            otherCard.suit != trickSuit &&
            otherCard.suit != trumpSuit
        ) {
            return true
        }

        // نفس اللون، الأعلى يربح
        if (card.suit == otherCard.suit) {
            return card.rank.value > otherCard.rank.value
        }

        return false
    }
}
