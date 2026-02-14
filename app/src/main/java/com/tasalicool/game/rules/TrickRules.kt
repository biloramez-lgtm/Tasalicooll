package com.tasalicool.game.rules

import com.tasalicool.game.model.Card
import com.tasalicool.game.model.Suit
import com.tasalicool.game.model.Trick

/**
 * TrickRules - قوانين الخدعات
 * 
 * القاعدة الذهبية: القلب (♥) دائماً ترامب
 * أعلى قلب يربح الخدعة
 */
object TrickRules {
    
    /**
     * عدد اللاعبين في الخدعة الواحدة
     */
    fun getPlayersPerTrick(): Int = 4
    
    /**
     * هل الخدعة اكتملت
     */
    fun isTrickComplete(trick: Trick): Boolean {
        return trick.isComplete(getPlayersPerTrick())
    }
    
    /**
     * حساب رابح الخدعة
     * 
     * القوانين:
     * 1. القلب يربح non-قلب
     * 2. أعلى قلب يربح قلب
     * 3. نفس لون الأول يربح الباقي
     * 4. نفس اللون، الأعلى يربح
     */
    fun calculateWinner(trick: Trick, trumpSuit: Suit = Suit.HEARTS): Int {
        if (trick.cards.isEmpty()) return -1
        
        var winningCard = trick.cards[trick.playOrder.first()] ?: return -1
        var winnerId = trick.playOrder.first()
        
        for (playerId in trick.playOrder.drop(1)) {
            val currentCard = trick.cards[playerId] ?: continue
            
            // Trump يربح non-trump
            if (currentCard.suit == trumpSuit && winningCard.suit != trumpSuit) {
                winningCard = currentCard
                winnerId = playerId
            }
            // أعلى trump يربح trump
            else if (currentCard.suit == trumpSuit && winningCard.suit == trumpSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
            // Trick suit يربح non-trick, non-trump
            else if (winningCard.suit != trumpSuit && currentCard.suit == trick.trickSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
            // نفس اللون، الأعلى يربح
            else if (currentCard.suit == winningCard.suit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
        }
        
        return winnerId
    }
    
    /**
     * الترامب دائماً القلب
     */
    fun getTrumpSuit(): Suit = Suit.HEARTS
    
    /**
     * هل الورقة ترامب (قلب)
     */
    fun isTrump(card: Card): Boolean = card.suit == getTrumpSuit()
    
    /**
     * هل الورقة الأولى تربح الورقة الثانية
     */
    fun canBeat(
        card: Card,
        otherCard: Card,
        trickSuit: Suit?,
        trumpSuit: Suit = Suit.HEARTS
    ): Boolean {
        // Trump يربح non-trump
        if (card.suit == trumpSuit && otherCard.suit != trumpSuit) return true
        
        // أعلى trump يربح أقل trump
        if (card.suit == trumpSuit && otherCard.suit == trumpSuit) {
            return card.rank.value > otherCard.rank.value
        }
        
        // Trick suit يربح non-trick, non-trump
        if (card.suit == trickSuit && otherCard.suit != trickSuit && otherCard.suit != trumpSuit) {
            return true
        }
        
        // نفس اللون، الأعلى يربح
        if (card.suit == otherCard.suit) {
            return card.rank.value > otherCard.rank.value
        }
        
        return false
    }
}
