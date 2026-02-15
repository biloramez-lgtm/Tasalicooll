package com.klosemiroslave.tasalicooll.engine

import com.klosemiroslave.tasalicooll.model.*

class GameEngine(val players: List<Player>, val teams: List<Team>) {

    private val deck = mutableListOf<Card>()

    init { createDeck() }

    private fun createDeck() {
        deck.clear()
        for (s in Suit.values()) for (r in Rank.values()) deck.add(Card(s, r))
    }

    fun shuffleDeck() = deck.shuffle()

    fun dealCards() {
        shuffleDeck()
        players.forEach { it.hand.clear() }
        val perPlayer = deck.size / players.size
        players.forEachIndexed { index, player ->
            player.hand.addAll(deck.slice(index*perPlayer until (index+1)*perPlayer))
            player.hand.sortBy { it.rank.value }
        }
    }

    fun playTrick(startIndex: Int, suitToFollow: Suit? = null): Int {
        val trick = mutableListOf<Pair<Player, Card>>()
        var suit = suitToFollow
        var currentIndex = startIndex

        for (i in 0 until players.size) {
            val player = players[currentIndex]
            val card = if (player.isAI) AIEngine.chooseCard(player, suit)
                       else player.hand.removeAt(0) // لاحقاً UI
            if (suit == null) suit = card.suit
            trick.add(player to card)
            currentIndex = (currentIndex + 1) % players.size
        }

        val winner = trick.filter { it.second.suit == suit || it.second.suit == Suit.HEARTS }
            .maxByOrNull { it.second.rank.value }!!

        winner.first.tricksWon += 1
        return players.indexOf(winner.first)
    }

    fun playRound() {
        players.forEach { it.resetRound() }
        dealCards()
        players.forEach { if (it.isAI) AIEngine.decideBid(it) }

        var currentIndex = 0
        repeat(13) { currentIndex = playTrick(currentIndex) }

        players.forEach { ScoringEngine.calculateScore(it) }
    }
}
