package com.klosemiroslave.tasalicooll.model

data class Player(
    val name: String,
    val teamId: Int,
    var hand: MutableList<Card> = mutableListOf(),
    var bid: Int = 0,
    var tricksWon: Int = 0,
    var score: Int = 0,
    val isAI: Boolean = false
) {
    fun resetRound() {
        hand.clear()
        bid = 0
        tricksWon = 0
    }
}
