package com.tasalicool.game.model

class Game(
    val players: List<Player>,
    val teams: List<Team>
) {

    init {
        require(players.size == 4) { "Game must have exactly 4 players" }
        require(teams.size == 2) { "Game must have exactly 2 teams" }
    }

    /* ================= GAME STATE ================= */

    var currentPhase: GamePhase = GamePhase.SETUP
        private set

    var currentPlayerIndex: Int = 0
        private set

    var currentTrick: Trick? = null
        private set

    var roundNumber: Int = 1
        private set

    val currentPlayer: Player
        get() = players[currentPlayerIndex]

    /* ================= GAME FLOW ================= */

    fun startGame() {
        currentPhase = GamePhase.BIDDING
        currentPlayerIndex = 0
    }

    fun startRound() {
        teams.forEach { it.resetRound() }
        roundNumber++
        currentPhase = GamePhase.BIDDING
    }

    fun startTrick() {
        currentTrick = Trick(startingPlayer = currentPlayer)
        currentPhase = GamePhase.PLAYING
    }

    /* ================= PLAY ================= */

    fun playCard(player: Player, card: Card): Boolean {
        if (currentPhase != GamePhase.PLAYING) return false
        if (player != currentPlayer) return false
        if (!player.removeCard(card)) return false

        currentTrick?.playCard(player, card)

        moveToNextPlayer()
        return true
    }

    /* ================= TRICK ================= */

    fun finishTrick() {
        val trick = currentTrick ?: return

        val winner = trick.getWinner()   // 👈 المنطق داخل Trick
        winner.tricksWon++

        currentPlayerIndex = players.indexOf(winner)
        currentTrick = null

        if (isRoundFinished()) {
            finishRound()
        } else {
            startTrick()
        }
    }

    /* ================= ROUND ================= */

    private fun finishRound() {
        teams.forEach { team ->
            if (team.isBidMet) {
                team.players.forEach { it.score += it.tricksWon }
            }
        }
        currentPhase = GamePhase.ROUND_END
    }

    private fun isRoundFinished(): Boolean {
        return players.all { it.hand.isEmpty() }
    }

    /* ================= TURN ================= */

    private fun moveToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }
}
