package com.tasalicool.game.mapper

import com.tasalicool.game.model.Game
import com.tasalicool.game.data.GameEntity

fun Game.toEntity(): GameEntity {
    return GameEntity(
        // id نحذفه لأن Room يولده تلقائياً

        team1Name = team1.name,
        team2Name = team2.name,

        team1Score = team1.score,
        team2Score = team2.score,

        winnerTeamId = winningTeamId,

        totalRounds = roundsPlayed,

        gameMode = gameMode.name,

        duration = duration,

        playerCount = players.size
    )
}
