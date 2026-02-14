package com.tasalicool.game.rules

import com.tasalicool.game.model.GamePhase

/**
 * GameRules - قوانين مراحل اللعبة
 */
object GameRules {

    // ===== Game Constants =====
    const val TOTAL_PLAYERS = 4
    const val CARDS_PER_PLAYER = 13
    const val TOTAL_CARDS = 52
    const val TOTAL_TRICKS = 13
    const val WIN_SCORE = 41

    /**
     * التحقق من صحة الانتقال بين المراحل
     */
    fun canTransition(from: GamePhase, to: GamePhase): Boolean {
        if (from == to) return false

        return when (from) {
            GamePhase.DEALING ->
                to == GamePhase.BIDDING

            GamePhase.BIDDING ->
                to == GamePhase.PLAYING || to == GamePhase.DEALING

            GamePhase.PLAYING ->
                to == GamePhase.ROUND_END

            GamePhase.ROUND_END ->
                to == GamePhase.DEALING || to == GamePhase.GAME_END

            GamePhase.GAME_END ->
                to == GamePhase.DEALING
        }
    }

    /**
     * هل اللعبة نشطة
     */
    fun isGameActive(phase: GamePhase): Boolean =
        phase != GamePhase.GAME_END

    /**
     * هل يمكن عمل بدية
     */
    fun canBid(phase: GamePhase): Boolean =
        phase == GamePhase.BIDDING

    /**
     * هل يمكن لعب ورقة
     */
    fun canPlayCard(phase: GamePhase): Boolean =
        phase == GamePhase.PLAYING
}
