package com.tasalicool.game.rules

import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Player
import com.tasalicool.game.model.Suit
import com.tasalicool.game.model.Trick

/**
 * PlayRules - قوانين لعب الأوراق
 * 
 * القاعدة الذهبية: يجب تتبع اللون إن أمكن
 */
object PlayRules {
    
    /**
     * التحقق من صحة لعب الورقة
     * @param card الورقة المراد لعبها
     * @param player اللاعب
     * @param trick الخدعة الحالية
     * @param playedCards الأوراق الملعوبة في هذه الخدعة
     * @return صحيح أو خطأ
     */
    fun canPlayCard(
        card: Card,
        player: Player,
        trick: Trick,
        playedCards: List<Card>
    ): Boolean {
        // أول لاعب يلعب أي ورقة
        if (playedCards.isEmpty()) return true
        
        val trickSuit = trick.trickSuit ?: return true
        
        // يجب تتبع اللون إن أمكن
        if (player.canFollowSuit(trickSuit)) {
            return card.suit == trickSuit
        }
        
        return true
    }
    
    /**
     * الحصول على الأوراق الصحيحة للعب
     */
    fun getValidCards(player: Player, trick: Trick): List<Card> {
        if (trick.cards.isEmpty()) return player.hand.toList()
        
        val trickSuit = trick.trickSuit ?: return player.hand.toList()
        
        val cardsOfSuit = player.hand.filter { it.suit == trickSuit }
        return if (cardsOfSuit.isNotEmpty()) {
            cardsOfSuit
        } else {
            player.hand.toList()
        }
    }
    
    /**
     * هل يجب تتبع اللون
     */
    fun mustFollowSuit(player: Player, trickSuit: Suit): Boolean {
        return player.canFollowSuit(trickSuit)
    }
}
