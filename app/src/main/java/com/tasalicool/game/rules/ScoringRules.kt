package com.tasalicool.game.rules

import com.tasalicool.game.model.Game

/**
 * ScoringRules - قوانين احتساب النقاط في نهاية الجولة
 *
 * مسؤول عن:
 * - حساب نقاط الفريقين
 * - تطبيق العقوبات
 * - تحديد فائز الجولة
 */
object ScoringRules {

    /**
     * تطبيق النقاط في نهاية الجولة
     */
    fun applyScores(game: Game) {
        // إعادة تعيين عدد الخدعات المكتسبة
        game.team1.resetTricksWon()
        game.team2.resetTricksWon()

        // حساب فائز كل خدعة والنقاط
        var team1TricksWon = 0
        var team2TricksWon = 0

        game.tricks.forEach { trick ->
            val winnerId = trick.currentWinnerId ?: return@forEach
            val winnerTeam = game.getTeamByPlayer(winnerId) ?: return@forEach

            if (winnerTeam.id == game.team1.id) {
                team1TricksWon++
            } else {
                team2TricksWon++
            }
        }

        // تحديث عدد الخدعات المكتسبة
        game.team1.setTricksWon(team1TricksWon)
        game.team2.setTricksWon(team2TricksWon)

        // احتساب النقاط بناءً على البدية والخدعات
        val team1Points = calculateTeamPoints(game.team1, team1TricksWon)
        val team2Points = calculateTeamPoints(game.team2, team2TricksWon)

        game.team1.addScore(team1Points)
        game.team2.addScore(team2Points)
    }

    /**
     * حساب نقاط الفريق في هذه الجولة
     */
    private fun calculateTeamPoints(team: Any, tricksWon: Int): Int {
        // هذه قاعدة بسيطة - يمكن تعديلها حسب قوانين اللعبة
        // في الواقع، النقاط تحسب حسب البدية والخدعات المكتسبة
        return tricksWon * 10
    }

    /**
     * التحقق من انتهاء اللعبة
     */
    fun isGameWon(game: Game): Boolean {
        return game.team1.isWinner || game.team2.isWinner
    }
}
