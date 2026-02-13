package com.tasalicool.game.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: String,
    val team1Name: String,
    val team2Name: String,
    val team1Score: Int,
    val team2Score: Int,
    val winnerTeamId: Int,
    val totalRounds: Int,
    val gameMode: String, // "SINGLE_PLAYER", "MULTIPLAYER", "AI"
    val createdAt: Long = System.currentTimeMillis(),
    val duration: Long, // in milliseconds
    val playerCount: Int
)

@Entity(tableName = "rounds")
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: String,
    val roundNumber: Int,
    val team1Bid: Int,
    val team2Bid: Int,
    val team1TricksWon: Int,
    val team2TricksWon: Int,
    val team1Points: Int,
    val team2Points: Int
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: String,
    val playerName: String,
    val isAI: Boolean,
    val finalScore: Int,
    val totalBidsPlaced: Int,
    val successfulBids: Int,
    val tricksWon: Int
)

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playerName: String,
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val averageScore: Double = 0.0,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val totalSuccessfulBids: Int = 0,
    val totalBidsAttempted: Int = 0
)

@Dao
interface GameDao {
    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Insert
    suspend fun insertRound(round: RoundEntity)

    @Insert
    suspend fun insertPlayer(player: PlayerEntity)

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber")
    suspend fun getRoundsByGameId(gameId: String): List<RoundEntity>

    @Query("SELECT * FROM players WHERE gameId = :gameId")
    suspend fun getPlayersByGameId(gameId: String): List<PlayerEntity>

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Int)

    @Query("SELECT COUNT(*) FROM games")
    fun getTotalGamesCount(): Flow<Int>

    @Query("SELECT SUM(CASE WHEN winnerTeamId = 1 THEN 1 ELSE 0 END) FROM games WHERE team1Name = :playerName")
    suspend fun getPlayerWins(playerName: String): Int

    @Query("SELECT AVG(team1Score) FROM games WHERE team1Name = :playerName")
    suspend fun getAverageScore(playerName: String): Double
}

@Dao
interface PlayerStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: PlayerStatsEntity)

    @Query("SELECT * FROM player_stats WHERE playerName = :playerName")
    suspend fun getStats(playerName: String): PlayerStatsEntity?

    @Query("SELECT * FROM player_stats ORDER BY totalWins DESC LIMIT 10")
    fun getTopPlayers(): Flow<List<PlayerStatsEntity>>

    @Query("SELECT * FROM player_stats ORDER BY averageScore DESC LIMIT 10")
    fun getTopScoringPlayers(): Flow<List<PlayerStatsEntity>>

    @Query("UPDATE player_stats SET totalGames = totalGames + 1 WHERE playerName = :playerName")
    suspend fun incrementGameCount(playerName: String)

    @Query("UPDATE player_stats SET totalWins = totalWins + 1 WHERE playerName = :playerName")
    suspend fun incrementWinCount(playerName: String)

    @Query("DELETE FROM player_stats WHERE playerName = :playerName")
    suspend fun deleteStats(playerName: String)
}

@Database(
    entities = [GameEntity::class, RoundEntity::class, PlayerEntity::class, PlayerStatsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TasalicoolDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playerStatsDao(): PlayerStatsDao

    companion object {
        private var INSTANCE: TasalicoolDatabase? = null

        fun getDatabase(context: android.content.Context): TasalicoolDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TasalicoolDatabase::class.java,
                    "tasalicool_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
