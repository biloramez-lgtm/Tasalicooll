package com.tasalicool.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tasalicool.game.data.TasalicoolDatabase
import com.tasalicool.game.repository.GameRepository
import com.tasalicool.game.ui.navigation.AppNavGraph
import com.tasalicool.game.ui.theme.TasalicoolTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: TasalicoolDatabase
    private lateinit var repository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ استخدام Singleton الصحيح من Room
        database = TasalicoolDatabase.getDatabase(applicationContext)

        // ✅ ربط Repository مع DAO
        repository = GameRepository(
            gameDao = database.gameDao()
        )

        setContent {
            TasalicoolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.cleanup()
    }
}
