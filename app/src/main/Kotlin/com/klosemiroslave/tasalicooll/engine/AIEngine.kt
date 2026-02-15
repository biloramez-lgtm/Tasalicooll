package com.klosemiroslave.tasalicooll.engine

import com.klosemiroslave.tasalicooll.model.Card
import com.klosemiroslave.tasalicooll.model.Player
import com.klosemiroslave.tasalicooll.model.Suit
import kotlin.random.Random

object AIEngine {

    fun chooseCard(player: Player, suitToFollow: Suit?): Card {
        val followSuit = player.hand.filter { it.suit == suitToFollow }
        val chosen = if (followSuit.isNotEmpty()) followSuit.random() else player.hand.random()
        player.hand.remove(chosen)
        return chosen
    }

    fun decideBid(player: Player): Int {
        val highCards = player.hand.count { it.rank.value >= 11 }
        val bid = highCards.coerceAtLeast(BiddingEngine.getMinimumBid(player))
        player.bid = bid
        return bid
    }
}
