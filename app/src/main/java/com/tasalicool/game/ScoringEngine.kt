package com.tarneeb.game.engine

import com.tarneeb.game.model.Team
import com.tarneeb.game.utils.GameConstants

class ScoringEngine {

    fun calculateTeamScore(
        team: Team,
        isAfter30Points: Boolean = false
    ): Pair<Int, Int> {
        val table = if (isAfter30Points) {
            GameConstants.SCORING_TABLE_30_PLUS
        } else {
            GameConstants.SCORING_TABLE_BELOW_30
        }

        val totalBid = team.getTotalBid()
        val totalTricksWon = team.getTotalTricksWon()

        val player1Score = if (totalTricksWon >= totalBid) {
            getPointsForBid(team.player1.bid, isAfter30Points)
        } else {
            -team.player1.bid
        }

        val player2Score = if (totalTricksWon >= totalBid) {
            getPointsForBid(team.player2.bid, isAfter30Points)
        } else {
            -team.player2.bid
        }

        return Pair(player1Score, player2Score)
    }

    fun getPointsForBid(bid: Int, isAfter30Points: Boolean): Int {
        val table = if (isAfter30Points) {
            GameConstants.SCORING_TABLE_30_PLUS
        } else {
            GameConstants.SCORING_TABLE_BELOW_30
        }

        return table[bid] ?: 0
    }

    fun isBidMet(totalBid: Int, totalTricksWon: Int): Boolean {
        return totalTricksWon >= totalBid
    }

    fun shouldUseAlternateTable(teamScore: Int): Boolean {
        return teamScore >= 30
    }

    fun isGameWon(team1Score: Int, team2Score: Int, team1Player2Score: Int = 0, team2Player2Score: Int = 0): Boolean {
        return (team1Score >= GameConstants.WINNING_SCORE && team1Player2Score > 0) ||
               (team2Score >= GameConstants.WINNING_SCORE && team2Player2Score > 0)
    }

    fun getWinningTeam(team1Score: Int, team2Score: Int, team1Player2Score: Int = 0, team2Player2Score: Int = 0): Int {
        return when {
            team1Score >= GameConstants.WINNING_SCORE && team1Player2Score > 0 -> 1
            team2Score >= GameConstants.WINNING_SCORE && team2Player2Score > 0 -> 2
            else -> -1
        }
    }

    fun calculateRoundResult(
        team1Bid: Int,
        team1TricksWon: Int,
        team2Bid: Int,
        team2TricksWon: Int,
        isAfter30: Boolean
    ): Pair<Int, Int> {
        val team1Total = team1Bid + team2Bid
        val team2Total = team1TricksWon + team2TricksWon

        val team1Points = if (team1TricksWon >= team1Bid) {
            getPointsForBid(team1Bid, isAfter30)
        } else {
            -team1Bid
        }

        val team2Points = if (team2TricksWon >= team2Bid) {
            getPointsForBid(team2Bid, isAfter30)
        } else {
            -team2Bid
        }

        return Pair(team1Points, team2Points)
    }

    fun getScoreStatus(teamScore: Int): String {
        return when {
            teamScore >= 50 -> "50+"
            teamScore >= 40 -> "40-49"
            teamScore >= 30 -> "30-39"
            else -> "Below 30"
        }
    }

    fun getMinimumBid(teamScore: Int): Int {
        return when {
            teamScore >= 50 -> 5
            teamScore >= 40 -> 4
            teamScore >= 30 -> 3
            else -> 2
        }
    }

    fun getMinimumTotalBids(maxTeamScore: Int): Int {
        return when {
            maxTeamScore >= 50 -> 14
            maxTeamScore >= 40 -> 13
            maxTeamScore >= 30 -> 12
            else -> 11
        }
    }
}
