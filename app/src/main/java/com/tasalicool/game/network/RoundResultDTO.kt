package com.tasalicool.game.network

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * RoundResultDTO
 * Network Safe - Data Only
 */
@Serializable
data class RoundResultDTO(

    // ===== IDENTIFICATION =====
    val roundNumber: Int,
    val gameId: String = "",

    // ===== TEAM SCORES =====
    val team1Score: Int,
    val team2Score: Int,
    val team1RoundPoints: Int = 0,
    val team2RoundPoints: Int = 0,
    val team1PreviousScore: Int = 0,
    val team2PreviousScore: Int = 0,

    // ===== BIDS =====
    val team1Bid: Int,
    val team2Bid: Int,
    val playerBids: Map<Int, Int> = emptyMap(),

    // ===== TRICKS =====
    val team1TricksWon: Int,
    val team2TricksWon: Int,
    val totalTricks: Int = 13,

    // ===== BID STATUS =====
    val team1BidMet: Boolean,
    val team2BidMet: Boolean,

    // ===== ROUND RESULT =====
    val roundWinner: Int,      // 1 or 2
    val isGameOver: Boolean = false,
    val gameWinner: Int = -1,  // -1 if not finished

    // ===== PLAYER STATS =====
    val playerScores: Map<Int, Int> = emptyMap(),
    val playerTricks: Map<Int, Int> = emptyMap(),

    // ===== TIMELINE =====
    val roundStartTime: Long = 0L,
    val roundEndTime: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()

) : JavaSerializable {

    // ===== BASIC VALIDATION =====

    fun isValid(): Boolean {
        return roundNumber >= 0 &&
                team1Score >= 0 &&
                team2Score >= 0 &&
                team1Bid in 0..26 &&
                team2Bid in 0..26 &&
                team1TricksWon in 0..13 &&
                team2TricksWon in 0..13 &&
                (team1TricksWon + team2TricksWon) == totalTricks &&
                roundWinner in 1..2 &&
                (gameWinner == -1 || gameWinner in 1..2)
    }

    override fun toString(): String {
        return "Round#$roundNumber T1:$team1Score T2:$team2Score Winner:$roundWinner"
    }

    // ===== FACTORY =====

    companion object {

        fun fromRoundResult(
            result: com.tasalicool.game.model.RoundResult,
            prevTeam1: Int = 0,
            prevTeam2: Int = 0
        ): RoundResultDTO {

            return RoundResultDTO(
                roundNumber = result.roundNumber,
                team1Score = result.team1Score,
                team2Score = result.team2Score,
                team1RoundPoints = result.team1Score - prevTeam1,
                team2RoundPoints = result.team2Score - prevTeam2,
                team1PreviousScore = prevTeam1,
                team2PreviousScore = prevTeam2,
                team1Bid = result.team1Bid,
                team2Bid = result.team2Bid,
                team1TricksWon = result.team1TricksWon,
                team2TricksWon = result.team2TricksWon,
                team1BidMet = result.team1BidMet,
                team2BidMet = result.team2BidMet,
                roundWinner = result.winner
            )
        }

        fun empty(): RoundResultDTO {
            return RoundResultDTO(
                roundNumber = 0,
                team1Score = 0,
                team2Score = 0,
                team1Bid = 0,
                team2Bid = 0,
                team1TricksWon = 0,
                team2TricksWon = 0,
                team1BidMet = false,
                team2BidMet = false,
                roundWinner = 1
            )
        }
    }
}
