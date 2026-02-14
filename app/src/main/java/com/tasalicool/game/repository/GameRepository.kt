package com.tasalicool.game.repository

import com.tasalicool.game.data.*

class GameRepository(
    private val gameDao: GameDao,
    private val playerStatsDao: PlayerStatsDao
) {

    suspend fun saveGame(game: GameEntity) {
        gameDao.insertGame(game)
    }

    suspend fun saveRound(round: RoundEntity) {
        gameDao.insertRound(round)
    }

    suspend fun savePlayer(player: PlayerEntity) {
        gameDao.insertPlayer(player)
    }

    suspend fun savePlayerStats(stats: PlayerStatsEntity) {
        playerStatsDao.insertStats(stats)
    }

    fun getAllGames() = gameDao.getAllGames()

    fun getTopPlayers() = playerStatsDao.getTopPlayers()
}
