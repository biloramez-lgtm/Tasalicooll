package com.tarneeb.game.engine

import com.tarneeb.game.model.Card
import com.tarneeb.game.model.Suit
import com.tarneeb.game.model.Trick
import com.tarneeb.game.model.Player

class CardRulesEngine {

    fun canPlayCard(
        card: Card,
        player: Player,
        trick: Trick,
        playedInTrick: List<Card>
    ): Boolean {
        // First card of the trick can always be played
        if (playedInTrick.isEmpty()) {
            return true
        }

        val trickSuit = trick.trickSuit ?: return true

        // Player must follow suit if they have it
        if (player.canFollowSuit(trickSuit)) {
            return card.suit == trickSuit
        }

        // Player can play any card if they don't have the trick suit
        return true
    }

    fun calculateTrickWinner(trick: Trick, trumpSuit: Suit = Suit.HEARTS): Int {
        if (trick.cards.isEmpty()) return -1

        var winningCard = trick.cards.values.first()
        var winnerId = trick.playOrder.first()

        // First, filter by suit - either trump suit or the suit that was led
        val trickSuit = trick.trickSuit

        for (playerId in trick.playOrder.drop(1)) {
            val currentCard = trick.cards[playerId] ?: continue

            // Trump suit beats everything
            if (currentCard.suit == trumpSuit && winningCard.suit != trumpSuit) {
                winningCard = currentCard
                winnerId = playerId
            }
            // Same suit as trump and higher rank
            else if (currentCard.suit == trumpSuit && winningCard.suit == trumpSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
            // Same suit as led and trump hasn't been played
            else if (winningCard.suit != trumpSuit && currentCard.suit == trickSuit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
            // Same suit as winning card but higher rank
            else if (currentCard.suit == winningCard.suit) {
                if (currentCard.rank.value > winningCard.rank.value) {
                    winningCard = currentCard
                    winnerId = playerId
                }
            }
        }

        return winnerId
    }

    fun validateBid(bid: Int, playerHandSize: Int, minimumBid: Int): Boolean {
        return bid in minimumBid..playerHandSize
    }

    fun validateTotalBids(totalBids: Int, minimumTotalBids: Int): Boolean {
        return totalBids >= minimumTotalBids
    }

    fun isTrumpSuitHeartsOnly(heartPlayed: Boolean): Boolean {
        // Hearts is always trump
        return true
    }

    fun hasValidFollowSuit(
        card: Card,
        trick: Trick,
        player: Player
    ): Boolean {
        val trickSuit = trick.trickSuit ?: return true
        if (card.suit == trickSuit) return true

        // Check if player has cards of the trick suit
        return !player.canFollowSuit(trickSuit)
    }

    fun sortCards(cards: List<Card>): List<Card> {
        return cards.sortedWith(
            compareBy<Card> { it.suit.ordinal }
                .thenBy { it.rank.value }
        )
    }

    fun canPlayHearts(trick: Trick, playerHand: List<Card>): Boolean {
        // Hearts cannot be the first card unless hearts have been broken
        if (trick.cards.isEmpty()) {
            return trick.heartsBroken || playerHand.all { it.suit == Suit.HEARTS }
        }
        return true
    }

    fun getValidPlayableCards(
        player: Player,
        trick: Trick
    ): List<Card> {
        if (trick.cards.isEmpty()) {
            // First card in trick
            return player.hand.toList()
        }

        val trickSuit = trick.trickSuit ?: return player.hand.toList()

        // Must follow suit if possible
        val cardsOfTrickSuit = player.hand.filter { it.suit == trickSuit }
        return if (cardsOfTrickSuit.isNotEmpty()) {
            cardsOfTrickSuit
        } else {
            // Can play any card if can't follow suit
            player.hand.toList()
        }
    }
}
