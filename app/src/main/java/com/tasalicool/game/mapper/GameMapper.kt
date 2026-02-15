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

        winnerTeamId = winningTeamId ?: 0, // إذا لم يتم تحديد الفائز بعد، نضع 0

        totalRounds = round ?: 0, // roundsPlayed غير موجود، استخدمنا round

        gameMode = gameMode?.name ?: "DEFAULT", // إذا gameMode غير موجود، نعطي قيمة افتراضية

        duration = duration ?: 0L, // إذا duration غير موجود، نعطي 0L

        playerCount = players.size
    )
}
