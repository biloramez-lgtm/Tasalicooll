package com.tasalicool.game.model

enum class GamePhase { DEALING, BIDDING, PLAYING, ROUND_END, GAME_END }
enum class BiddingPhase { WAITING, ACTIVE, FINISHED }

data class Game(
    val team1: Team,
    val team2: Team,
    val players: List<Player>
) {
    var round: Int = 1
    var gamePhase: GamePhase = GamePhase.DEALING
    var biddingPhase: BiddingPhase = BiddingPhase.WAITING
    var currentPlayerIndex: Int = 0
    var dealerIndex: Int = 0
    val tricks: MutableList<Trick> = mutableListOf()
    var isGameOver: Boolean = false

    fun currentPlayer(): Player = players[currentPlayerIndex]
    fun rightOfDealerIndex(): Int = (dealerIndex + 1) % players.size
    fun nextPlayerIndex(): Int = (currentPlayerIndex + 1) % players.size
    fun getOrCreateCurrentTrick(): Trick {
        if (tricks.isEmpty() || tricks.last().isComplete(players.size)) {
            tricks.add(Trick())
        }
        return tricks.last()
    }

    fun startDealing() {
        gamePhase = GamePhase.DEALING
        biddingPhase = BiddingPhase.WAITING
        currentPlayerIndex = rightOfDealerIndex()
        tricks.clear()
    }

    fun startBidding() {
        gamePhase = GamePhase.BIDDING
        biddingPhase = BiddingPhase.ACTIVE
        currentPlayerIndex = rightOfDealerIndex()
    }

    fun startPlaying() {
        gamePhase = GamePhase.PLAYING
        currentPlayerIndex = rightOfDealerIndex()
        tricks.clear()
    }

    fun advanceTurn() { currentPlayerIndex = nextPlayerIndex() }

    fun endRound() { gamePhase = GamePhase.ROUND_END }

    fun endGame(winningTeamId: Int) { isGameOver = true }
}
