package com.tarneeb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tarneeb.engine.*
import com.tarneeb.network.NetworkManager
import com.tarneeb.ui.*

class TarneebMainActivity : ComponentActivity() {

    private lateinit var engine: EngineGod
    private lateinit var networkManager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⛔ إنشاء الكائنات خارج Compose (حل الكراش)
        engine = EngineGod()
        networkManager = NetworkManager()

        setContent {
            TarneebApp(
                engine = engine,
                networkManager = networkManager
            )
        }
    }
}

@Composable
fun TarneebApp(
    engine: EngineGod,
    networkManager: NetworkManager
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    // ✅ قيم ابتدائية آمنة
    val gameState by engine.gameState.collectAsState(initial = null)
    val error by engine.error.collectAsState(initial = null)
    val aiAction by engine.aiAction.collectAsState(initial = null)

    val networkState by networkManager.networkState.collectAsState(initial = null)
    val networkError by networkManager.error.collectAsState(initial = null)

    TarneebTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            when (currentScreen) {

                AppScreen.Home -> HomeScreen(
                    onSinglePlayerClick = {
                        currentScreen = AppScreen.SinglePlayerSetup
                    },
                    onMultiplayerClick = {
                        currentScreen = AppScreen.MultiplayerSetup
                    },
                    onNetworkClick = {
                        currentScreen = AppScreen.NetworkSetup
                    }
                )

                AppScreen.SinglePlayerSetup -> SinglePlayerSetupScreen(
                    onStart = { name, difficulty ->
                        engine.startSinglePlayer(name, difficulty)
                        currentScreen = AppScreen.Game
                    },
                    onBack = { currentScreen = AppScreen.Home }
                )

                AppScreen.MultiplayerSetup -> MultiplayerSetupScreen(
                    onStart = { players, difficulty ->
                        engine.startMultiplayer(players.size, players, difficulty)
                        currentScreen = AppScreen.Game
                    },
                    onBack = { currentScreen = AppScreen.Home }
                )

                AppScreen.Game -> {
                    if (gameState == null) {
                        LoadingScreen()
                    } else {
                        GameScreen(
                            engine = engine,
                            gameState = gameState,
                            error = error,
                            aiAction = aiAction,
                            onBack = {
                                engine.resetGame()
                                currentScreen = AppScreen.Home
                            }
                        )
                    }
                }

                AppScreen.NetworkSetup -> NetworkSetupScreen(
                    networkManager = networkManager,
                    onJoinGame = { code, name ->
                        networkManager.joinGame(code, name)
                        currentScreen = AppScreen.NetworkGame
                    },
                    onCreateGame = { name ->
                        networkManager.createGame(name)
                        currentScreen = AppScreen.NetworkGame
                    },
                    onBack = { currentScreen = AppScreen.Home }
                )

                AppScreen.NetworkGame -> NetworkGameScreen(
                    networkManager = networkManager,
                    networkState = networkState,
                    onBack = {
                        networkManager.disconnect()
                        currentScreen = AppScreen.Home
                    }
                )
            }
        }
    }
}
