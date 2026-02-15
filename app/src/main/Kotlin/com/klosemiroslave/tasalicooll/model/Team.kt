package com.klosemiroslave.tasalicooll.model

data class Team(val id: Int, val players: List<Player>) {
    fun teamScore() = players.sumOf { it.score }
}
