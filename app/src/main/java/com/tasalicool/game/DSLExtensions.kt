package com.tarneeb.game.dsl

import com.tarneeb.game.model.*
import com.tarneeb.game.engine.GameEngine
import com.tarneeb.game.engine.CardRulesEngine
import com.tarneeb.game.engine.ScoringEngine
import com.tarneeb.game.engine.BiddingEngine

// DSL for creating players
class PlayerBuilder {
    private var id: Int = 0
    private var name: String = "Player"
    private var isAI: Boolean = false
    private var position: Int = 0

    fun id(value: Int) = apply { id = value }
    fun name(value: String) = apply { name = value }
    fun ai(value: Boolean) = apply { isAI = value }
    fun position(value: Int) = apply { position = value }

    fun build(): Player = Player(
        id = id,
        name = name,
        isAI = isAI,
        position = position
    )
}

fun player(init: PlayerBuilder.() -> Unit): Player {
    return PlayerBuilder().apply(init).build()
}

// DSL for creating teams
class TeamBuilder {
    private var id: String = ""
    private var name: String = "Team"
    private lateinit var player1: Player
    private lateinit var player2: Player

    fun id(value: String) = apply { id = value }
    fun name(value: String) = apply { name = value }
    fun player1(value: Player) = apply { player1 = value }
    fun player2(value: Player) = apply { player2 = value }

    fun build(): Team = Team(
        id = id,
        name = name,
        player1 = player1,
        player2 = player2
    )
}

fun team(init: TeamBuilder.() -> Unit): Team {
    return TeamBuilder().apply(init).build()
}

// DSL for creating games
class GameBuilder {
    private lateinit var team1: Team
    private lateinit var team2: Team
    private var dealerIndex: Int = 0

    fun team1(value: Team) = apply { team1 = value }
    fun team2(value: Team) = apply { team2 = value }
    fun dealerIndex(value: Int) = apply { dealerIndex = value }

    fun build(engine: GameEngine): Game {
        return engine.initializeGame(team1, team2, dealerIndex)
    }
}

fun game(init: GameBuilder.() -> Unit): GameBuilder {
    return GameBuilder().apply(init)
}

// Extension Functions

// String extensions
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

// Card extensions
fun Card.getDisplayName(): String {
    return "${this.rank.displayName}${this.suit.getSymbol()}"
}

fun Card.isHighCard(): Boolean {
    return this.rank.value >= 10
}

fun Card.isTrump(): Boolean {
    return this.suit == Suit.HEARTS
}

fun List<Card>.sortByValue(): List<Card> {
    return this.sortedWith(compareBy({ it.suit.ordinal }, { it.rank.value }))
}

fun List<Card>.countBySuit(): Map<Suit, Int> {
    return this.groupingBy { it.suit }.eachCount()
}

fun List<Card>.highestByRank(): Card? {
    return this.maxByOrNull { it.rank.value }
}

fun List<Card>.lowestByRank(): Card? {
    return this.minByOrNull { it.rank.value }
}

// Player extensions
fun Player.isBiddingPhase(): Boolean {
    return this.bid == 0
}

fun Player.hasCards(): Boolean {
    return this.hand.isNotEmpty()
}

fun Player.cardsOfSuit(suit: Suit): List<Card> {
    return this.hand.filter { it.suit == suit }
}

fun Player.highCards(): List<Card> {
    return this.hand.filter { it.isHighCard() }
}

fun Player.trumpCards(): List<Card> {
    return this.hand.filter { it.isTrump() }
}

fun Player.clearHand() {
    this.hand.clear()
}

fun Player.handSize(): Int {
    return this.hand.size
}

fun Player.hasFollowSuit(suit: Suit): Boolean {
    return this.canFollowSuit(suit)
}

// Team extensions
fun Team.getBidTotal(): Int {
    return this.player1.bid + this.player2.bid
}

fun Team.getTrickTotal(): Int {
    return this.player1.tricksWon + this.player2.tricksWon
}

fun Team.isSuccessful(): Boolean {
    return this.getTrickTotal() >= this.getBidTotal()
}

fun Team.getTeamMembers(): List<Player> {
    return listOf(this.player1, this.player2)
}

fun Team.getHighestScore(): Int {
    return maxOf(this.player1.score, this.player2.score)
}

fun Team.getLowestScore(): Int {
    return minOf(this.player1.score, this.player2.score)
}

// Game extensions
fun Game.getRound(): Int {
    return this.currentRound
}

fun Game.getDealer(): Player {
    return this.getDealerPlayer()
}

fun Game.getNextDealer(): Player {
    val nextDealerIndex = (this.dealerIndex + 1) % 4
    return this.players[nextDealerIndex]
}

fun Game.getTotalTricks(): Int {
    return this.tricks.size
}

fun Game.getRemainingTricks(): Int {
    return 13 - this.getTotalTricks()
}

fun Game.getMaxTeamScore(): Int {
    return maxOf(this.team1.score, this.team2.score)
}

fun Game.isTeam1Ahead(): Boolean {
    return this.team1.score > this.team2.score
}

fun Game.allPlayersReady(): Boolean {
    return this.players.all { it.hand.isNotEmpty() }
}

fun Game.hasAllBids(): Boolean {
    return this.players.all { it.bid > 0 }
}

// Trick extensions
fun Trick.addCardForPlayer(player: Player, card: Card) {
    this.addCard(player.id, card)
}

fun Trick.getPlayedCards(): List<Card> {
    return this.cards.values.toList()
}

fun Trick.getCardByPlayer(playerId: Int): Card? {
    return this.cards[playerId]
}

fun Trick.getPlayerCount(): Int {
    return this.cards.size
}

fun Trick.isComplete(): Boolean {
    return this.cards.size == 4
}

fun Trick.hasHeart(): Boolean {
    return this.cards.values.any { it.suit == Suit.HEARTS }
}

// Scoring helper extensions
fun Int.getScoreCategory(): String {
    return when {
        this >= 50 -> "50+"
        this >= 40 -> "40-49"
        this >= 30 -> "30-39"
        else -> "Below 30"
    }
}

fun Int.isWinningScore(): Boolean {
    return this >= 41
}

// Useful infix functions
infix fun Player.playedCard(card: Card): Pair<Player, Card> {
    return Pair(this, card)
}

infix fun Trick.winnerIs(playerId: Int) {
    this.winnerId = playerId
}

// Scope functions for cleaner game setup
inline fun <T> gameScope(block: GameScopeBlock.() -> T): T {
    return GameScopeBlock().block()
}

class GameScopeBlock {
    val engines = EnginesBundle(
        cardRulesEngine = CardRulesEngine(),
        scoringEngine = ScoringEngine(),
        biddingEngine = BiddingEngine()
    )

    val gameEngine = GameEngine(engines.cardRulesEngine, engines.scoringEngine, engines.biddingEngine)

    data class EnginesBundle(
        val cardRulesEngine: CardRulesEngine,
        val scoringEngine: ScoringEngine,
        val biddingEngine: BiddingEngine
    )
}

// Quick initialization helper
fun createGame(
    player1Name: String = "Player 1",
    player2Name: String = "Player 2",
    ai1Name: String = "AI 1",
    ai2Name: String = "AI 2",
    dealerIndex: Int = 0
): Game {
    val p1 = player {
        id(0)
        name(player1Name)
        position(0)
    }

    val p3 = player {
        id(2)
        name(player2Name)
        position(2)
    }

    val p2 = player {
        id(1)
        name(ai1Name)
        ai(true)
        position(1)
    }

    val p4 = player {
        id(3)
        name(ai2Name)
        ai(true)
        position(3)
    }

    val t1 = team {
        id("1")
        name("Team 1")
        player1(p1)
        player2(p3)
    }

    val t2 = team {
        id("2")
        name("Team 2")
        player1(p2)
        player2(p4)
    }

    val engine = GameEngine()
    return engine.initializeGame(t1, t2, dealerIndex)
}

// Utility lambda for card filtering
typealias CardFilter = (Card) -> Boolean

fun CardFilter.ofSuit(suit: Suit): CardFilter {
    return { card -> this(card) && card.suit == suit }
}

fun CardFilter.isHigh(): CardFilter {
    return { card -> this(card) && card.rank.value >= 10 }
}
