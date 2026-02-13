package com.tarneeb.game.engine

import com.tarneeb.game.model.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineTest {

    private lateinit var gameEngine: GameEngine
    private lateinit var cardRulesEngine: CardRulesEngine
    private lateinit var scoringEngine: ScoringEngine
    private lateinit var biddingEngine: BiddingEngine

    @Before
    fun setup() {
        cardRulesEngine = CardRulesEngine()
        scoringEngine = ScoringEngine()
        biddingEngine = BiddingEngine()
        gameEngine = GameEngine(cardRulesEngine, scoringEngine, biddingEngine)
    }

    @Test
    fun testGameInitialization() {
        val player1 = Player(0, "Player 1")
        val player3 = Player(2, "Player 3")
        val player2 = Player(1, "Player 2")
        val player4 = Player(3, "Player 4")

        val team1 = Team("1", "Team 1", player1, player3)
        val team2 = Team("2", "Team 2", player2, player4)

        val game = gameEngine.initializeGame(team1, team2, 0)

        assertEquals(0, game.dealerIndex)
        assertEquals(4, game.players.size)
        assertEquals(GamePhase.DEALING, game.gamePhase)
    }

    @Test
    fun testCardDealing() {
        val player1 = Player(0, "Player 1")
        val player3 = Player(2, "Player 3")
        val player2 = Player(1, "Player 2")
        val player4 = Player(3, "Player 4")

        val team1 = Team("1", "Team 1", player1, player3)
        val team2 = Team("2", "Team 2", player2, player4)

        val game = gameEngine.initializeGame(team1, team2, 0)
        gameEngine.dealCards(game)

        // Each player should have 13 cards
        game.players.forEach { player ->
            assertEquals(13, player.hand.size)
        }

        // Total cards should be 52
        val totalCards = game.players.sumOf { it.hand.size }
        assertEquals(52, totalCards)

        // All cards should be unique
        val allCards = game.players.flatMap { it.hand }
        assertEquals(52, allCards.distinct().size)
    }

    @Test
    fun testBiddingPhase() {
        val player1 = Player(0, "Player 1")
        val player3 = Player(2, "Player 3")
        val player2 = Player(1, "Player 2")
        val player4 = Player(3, "Player 4")

        val team1 = Team("1", "Team 1", player1, player3)
        val team2 = Team("2", "Team 2", player2, player4)

        val game = gameEngine.initializeGame(team1, team2, 0)
        gameEngine.dealCards(game)

        // Place bids
        gameEngine.placeBid(game, 0, 5)
        assertEquals(5, player1.bid)

        gameEngine.placeBid(game, 1, 6)
        assertEquals(6, player2.bid)

        gameEngine.placeBid(game, 2, 7)
        assertEquals(7, player3.bid)

        gameEngine.placeBid(game, 3, 8)
        assertEquals(8, player4.bid)

        // Check if minimum total bids is met (>= 11)
        val totalBids = listOf(player1.bid, player2.bid, player3.bid, player4.bid).sum()
        assertTrue(totalBids >= 11)
    }

    @Test
    fun testTrickCalculation() {
        val trick = Trick()
        trick.addCard(0, Card(Suit.HEARTS, Rank.ACE))
        trick.addCard(1, Card(Suit.HEARTS, Rank.KING))
        trick.addCard(2, Card(Suit.HEARTS, Rank.QUEEN))
        trick.addCard(3, Card(Suit.HEARTS, Rank.JACK))

        val winnerId = cardRulesEngine.calculateTrickWinner(trick)
        assertEquals(0, winnerId) // Player 0 has the ace
    }

    @Test
    fun testTrumpSuitWins() {
        val trick = Trick()
        trick.addCard(0, Card(Suit.SPADES, Rank.ACE))
        trick.addCard(1, Card(Suit.HEARTS, Rank.TWO))
        trick.addCard(2, Card(Suit.DIAMONDS, Rank.KING))
        trick.addCard(3, Card(Suit.CLUBS, Rank.QUEEN))

        val winnerId = cardRulesEngine.calculateTrickWinner(trick, Suit.HEARTS)
        assertEquals(1, winnerId) // Player 1 has the trump suit (hearts)
    }

    @Test
    fun testFollowSuitRule() {
        val player = Player(0, "Test Player")
        player.addCard(Card(Suit.HEARTS, Rank.ACE))
        player.addCard(Card(Suit.HEARTS, Rank.KING))
        player.addCard(Card(Suit.DIAMONDS, Rank.QUEEN))

        assertTrue(player.canFollowSuit(Suit.HEARTS))
        assertTrue(player.canFollowSuit(Suit.DIAMONDS))
        assertFalse(player.canFollowSuit(Suit.CLUBS))
    }

    @Test
    fun testValidBidRange() {
        val hand = listOf(
            Card(Suit.HEARTS, Rank.ACE),
            Card(Suit.HEARTS, Rank.KING),
            Card(Suit.DIAMONDS, Rank.QUEEN)
        )

        assertTrue(cardRulesEngine.validateBid(2, hand.size, 2))
        assertTrue(cardRulesEngine.validateBid(3, hand.size, 2))
        assertFalse(cardRulesEngine.validateBid(5, hand.size, 2))
        assertFalse(cardRulesEngine.validateBid(1, hand.size, 2))
    }
}

class ScoringEngineTest {

    private lateinit var scoringEngine: ScoringEngine

    @Before
    fun setup() {
        scoringEngine = ScoringEngine()
    }

    @Test
    fun testScoreCalculationBelow30() {
        val points = scoringEngine.getPointsForBid(5, false)
        assertEquals(10, points)
    }

    @Test
    fun testScoreCalculationAbove30() {
        val points = scoringEngine.getPointsForBid(5, true)
        assertEquals(5, points)
    }

    @Test
    fun testHighBidScoring() {
        val points = scoringEngine.getPointsForBid(10, false)
        assertEquals(40, points)
    }

    @Test
    fun testMinimumBidCalculation() {
        val bid30 = scoringEngine.getMinimumBid(30)
        assertEquals(3, bid30)

        val bid40 = scoringEngine.getMinimumBid(40)
        assertEquals(4, bid40)

        val bid50 = scoringEngine.getMinimumBid(50)
        assertEquals(5, bid50)
    }

    @Test
    fun testGameWinCondition() {
        assertTrue(scoringEngine.isGameWon(41, 30, 1, 1))
        assertFalse(scoringEngine.isGameWon(40, 30, 1, 1))
        assertFalse(scoringEngine.isGameWon(41, 30, 0, 1))
    }
}

class CardRulesEngineTest {

    private lateinit var cardRulesEngine: CardRulesEngine

    @Before
    fun setup() {
        cardRulesEngine = CardRulesEngine()
    }

    @Test
    fun testCardSorting() {
        val cards = listOf(
            Card(Suit.SPADES, Rank.ACE),
            Card(Suit.HEARTS, Rank.TWO),
            Card(Suit.HEARTS, Rank.KING),
            Card(Suit.CLUBS, Rank.QUEEN)
        )

        val sorted = cardRulesEngine.sortCards(cards)
        assertEquals(Suit.CLUBS, sorted[0].suit)
        assertEquals(Suit.HEARTS, sorted[1].suit)
        assertEquals(Rank.TWO, sorted[1].rank)
    }

    @Test
    fun testValidPlayableCards() {
        val player = Player(0, "Test")
        player.addCard(Card(Suit.HEARTS, Rank.ACE))
        player.addCard(Card(Suit.HEARTS, Rank.KING))
        player.addCard(Card(Suit.DIAMONDS, Rank.QUEEN))

        val trick = Trick()
        val validCards = cardRulesEngine.getValidPlayableCards(player, trick)
        assertEquals(3, validCards.size)
    }

    @Test
    fun testFollowSuitRequirement() {
        val player = Player(0, "Test")
        player.addCard(Card(Suit.HEARTS, Rank.ACE))
        player.addCard(Card(Suit.HEARTS, Rank.KING))
        player.addCard(Card(Suit.DIAMONDS, Rank.QUEEN))

        val trick = Trick()
        trick.trickSuit = Suit.HEARTS

        val validCards = cardRulesEngine.getValidPlayableCards(player, trick)
        assertEquals(2, validCards.size)
        assertTrue(validCards.all { it.suit == Suit.HEARTS })
    }
}
