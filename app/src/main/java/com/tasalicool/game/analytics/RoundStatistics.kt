package com.tasalicool.game.analytics

import com.tasalicool.game.network.dto.RoundResultDTO
import kotlin.math.abs

/**
 * RoundStatistics
 *
 * مسؤول عن تحليل نتائج الجولة
 * لا يُستخدم عبر الشبكة
 * لا يُرسل للـ clients
 */
object RoundStatistics {

    // ==============================
    // BID ANALYSIS
    // ==============================

    fun calculateBidSuccessRate(result: RoundResultDTO): Float {
        var successCount = 0
        if (result.team1BidMet) successCount++
        if (result.team2BidMet) successCount++
        return successCount / 2f
    }

    fun calculateTotalBids(result: RoundResultDTO): Int {
        return result.team1Bid + result.team2Bid
    }

    fun calculateAverageBid(result: RoundResultDTO): Float {
        return calculateTotalBids(result) / 2f
    }

    fun calculateBidDifference(result: RoundResultDTO): Int {
        return abs(result.team1Bid - result.team2Bid)
    }

    // ==============================
    // TRICK ANALYSIS
    // ==============================

    fun calculateTotalTricks(result: RoundResultDTO): Int {
        return result.team1TricksWon + result.team2TricksWon
    }

    fun calculateTrickDifference(result: RoundResultDTO): Int {
        return abs(result.team1TricksWon - result.team2TricksWon)
    }

    fun isDominantWin(result: RoundResultDTO): Boolean {
        return calculateTrickDifference(result) >= 5
    }

    fun isCloseRound(result: RoundResultDTO): Boolean {
        return calculateTrickDifference(result) <= 1
    }

    // ==============================
    // SCORE ANALYSIS
    // ==============================

    fun calculateScoreDifference(result: RoundResultDTO): Int {
        return abs(result.team1Score - result.team2Score)
    }

    fun getLeadingTeam(result: RoundResultDTO): Int {
        return when {
            result.team1Score > result.team2Score -> 1
            result.team2Score > result.team1Score -> 2
            else -> 0
        }
    }

    fun isBalanced(result: RoundResultDTO): Boolean {
        return calculateScoreDifference(result) <= 2
    }

    // ==============================
    // PERFORMANCE
    // ==============================

    fun getBestPlayer(result: RoundResultDTO): Int? {
        return result.playerTricks.maxByOrNull { it.value }?.key
    }

    fun getWorstPlayer(result: RoundResultDTO): Int? {
        return result.playerTricks.minByOrNull { it.value }?.key
    }

    fun getMostAccurateBidder(result: RoundResultDTO): Int? {
        return result.playerBids
            .mapNotNull { (playerId, bid) ->
                val tricks = result.playerTricks[playerId] ?: return@mapNotNull null
                playerId to abs(tricks - bid)
            }
            .minByOrNull { it.second }
            ?.first
    }

    // ==============================
    // REPORT GENERATION (Optional UI Use)
    // ==============================

    fun generateSummary(result: RoundResultDTO): String {
        return buildString {
            appendLine("Round #${result.roundNumber}")
            appendLine("Team 1 Score: ${result.team1Score}")
            appendLine("Team 2 Score: ${result.team2Score}")
            appendLine("Score Difference: ${calculateScoreDifference(result)}")
            appendLine("Dominant Win: ${isDominantWin(result)}")
            appendLine("Close Round: ${isCloseRound(result)}")
            appendLine("Best Player: ${getBestPlayer(result) ?: "N/A"}")
        }
    }
}
