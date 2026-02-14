package com.tasalicool.game.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/* =====================================================
   ENTITIES
   ===================================================== */

@Entity(
    tableName = "games",
    indices = [Index(value = ["createdAt"])]
)
data class GameEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val team1Name: String,
    val team2Name: String,

    val team1Score: Int,
    val team2Score: Int,

    val winnerTeamId: Int?, // nullable مهم جداً

    val totalRounds: Int,

    val gameMode: String, // SINGLE_PLAYER | MULTIPLAYER | AI

    val createdAt: Long = System.currentTimeMillis(),

    val duration: Long,

    val playerCount: Int
)


@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gameOwnerId"])]
)
data class RoundEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val gameOwnerId: Int, // مربوط بـ GameEntity.id

    val roundNumber: Int,

    val team1Bid: Int,
    val team2Bid: Int,

    val team1TricksWon: Int,
    val team2TricksWon: Int,

    val team1Points: Int,
    val team2Points: Int
)


@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gameOwnerId"])]
)
data class PlayerEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val gameOwnerId: Int, // مربوط بـ GameEntity.id

    val playerName: String,

    val isAI: Boolean,

    val finalScore: Int,

    val totalBidsPlaced: Int,

    val successfulBids: Int,

    val tricksWon: Int
)


@Entity(
    tableName = "player_stats",
    indices = [Index(value = ["playerName"], unique = true)]
)
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

/* =====================================================
   DAOs
   ===================================================== */

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Insert
    suspend fun insertRound(round: RoundEntity)

    @Insert
    suspend fun insertPlayer(player: PlayerEntity)

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("SELECT * FROM rounds WHERE gameOwnerId = :gameId ORDER BY roundNumber")
    suspend fun getRoundsByGameId(gameId: Int): List<RoundEntity>

    @Query("SELECT * FROM players WHERE gameOwnerId = :gameId")
    suspend fun getPlayersByGameId(gameId: Int): List<PlayerEntity>

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Int)

    @Query("SELECT COUNT(*) FROM games")
    fun getTotalGamesCount(): Flow<Int>
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

/* =====================================================
   DATABASE
   ===================================================== */

@Database(
    entities = [
        GameEntity::class,
        RoundEntity::class,
        PlayerEntity::class,
        PlayerStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TasalicoolDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun playerStatsDao(): PlayerStatsDao

    companion object {

        @Volatile
        private var INSTANCE: TasalicoolDatabase? = null

        fun getDatabase(context: Context): TasalicoolDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TasalicoolDatabase::class.java,
                    "tasalicool_database"
                )
                    .fallbackToDestructiveMigration() // مهم أثناء التطوير
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
