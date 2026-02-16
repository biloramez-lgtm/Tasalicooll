package com.tarneeb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarneeb.engine.EngineGod
import com.tarneeb.engine.AIDifficulty
import com.tarneeb.engine.GameMode
import com.tarneeb.network.NetworkManager
import com.tarneeb.ui.*

/**
 * TarneebMainActivity - الـ Activity الرئيسي لتطبيق Tarneeb
 * 
 * يحتوي على:
 * ✅ Navigation كامل
 * ✅ EngineGod Integration
 * ✅ NetworkManager Integration
 * ✅ State Management
 * ✅ Theme Configuration
 */
class TarneebMainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TarneebApp()
        }
    }
}

/**
 * TarneebApp - تطبيق Tarneeb الرئيسي
 */
@Composable
fun TarneebApp() {
    val engine = remember { EngineGod() }
    val networkManager = remember { NetworkManager() }
    
    // حالة التطبيق
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    var selectedGameMode by remember { mutableStateOf<GameMode?>(null) }
    var selectedDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }
    var playerName by remember { mutableStateOf("") }
    var playerList by remember { mutableStateOf(listOf<String>()) }
    
    // مراقبة حالة اللعبة
    val gameState by engine.gameState.collectAsState()
    val error by engine.error.collectAsState()
    val aiAction by engine.aiAction.collectAsState()
    
    // مراقبة حالة الشبكة
    val networkState by networkManager.networkState.collectAsState()
    val networkError by networkManager.error.collectAsState()
    
    // Theme
    TarneebTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Navigation
            when (currentScreen) {
                AppScreen.Home -> {
                    HomeScreen(
                        onSinglePlayerClick = {
                            currentScreen = AppScreen.SinglePlayerSetup
                            selectedGameMode = GameMode.SINGLE_PLAYER
                        },
                        onMultiplayerClick = {
                            currentScreen = AppScreen.MultiplayerSetup
                            selectedGameMode = GameMode.MULTIPLAYER_LOCAL
                        },
                        onNetworkClick = {
                            currentScreen = AppScreen.NetworkSetup
                        }
                    )
                }
                
                AppScreen.SinglePlayerSetup -> {
                    SinglePlayerSetupScreen(
                        onStart = { name, difficulty ->
                            playerName = name
                            selectedDifficulty = difficulty
                            engine.startSinglePlayer(name, difficulty)
                            currentScreen = AppScreen.Game
                        },
                        onBack = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }
                
                AppScreen.MultiplayerSetup -> {
                    MultiplayerSetupScreen(
                        onStart = { players, difficulty ->
                            playerList = players
                            selectedDifficulty = difficulty
                            engine.startMultiplayer(players.size, players, difficulty)
                            currentScreen = AppScreen.Game
                        },
                        onBack = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }
                
                AppScreen.NetworkSetup -> {
                    NetworkSetupScreen(
                        networkManager = networkManager,
                        onJoinGame = { gameCode, name ->
                            playerName = name
                            networkManager.joinGame(gameCode, name)
                            currentScreen = AppScreen.NetworkGame
                        },
                        onCreateGame = { name ->
                            playerName = name
                            networkManager.createGame(name)
                            currentScreen = AppScreen.NetworkGame
                        },
                        onBack = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }
                
                AppScreen.Game -> {
                    if (gameState != null) {
                        GameScreen(
                            engine = engine,
                            gameState = gameState!!,
                            error = error,
                            aiAction = aiAction,
                            onBack = {
                                engine.resetGame()
                                currentScreen = AppScreen.Home
                            }
                        )
                    }
                }
                
                AppScreen.NetworkGame -> {
                    NetworkGameScreen(
                        networkManager = networkManager,
                        networkState = networkState,
                        onBack = {
                            networkManager.disconnect()
                            currentScreen = AppScreen.Home
                        }
                    )
                }
            }
            
            // عرض الأخطاء
            if (error != null) {
                LaunchedEffect(error) {
                    println("❌ Engine Error: $error")
                }
            }
            
            if (networkError != null) {
                LaunchedEffect(networkError) {
                    println("❌ Network Error: $networkError")
                }
            }
        }
    }
}

/**
 * AppScreen - شاشات التطبيق
 */
sealed class AppScreen {
    object Home : AppScreen()
    object SinglePlayerSetup : AppScreen()
    object MultiplayerSetup : AppScreen()
    object NetworkSetup : AppScreen()
    object Game : AppScreen()
    object NetworkGame : AppScreen()
}

/**
 * TarneebTheme - المظهر
 */
@Composable
fun TarneebTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = TarneebColors.Primary,
            onPrimary = TarneebColors.White,
            secondary = TarneebColors.Secondary,
            onSecondary = TarneebColors.White,
            background = TarneebColors.Background,
            onBackground = TarneebColors.TextPrimary,
            surface = TarneebColors.Surface,
            onSurface = TarneebColors.TextPrimary,
            error = TarneebColors.Error
        ),
        typography = TarneebTypography,
        content = content
    )
}

/**
 * TarneebColors - الألوان
 */
object TarneebColors {
    val Primary = androidx.compose.ui.graphics.Color(0xFF1976D2)
    val Secondary = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    val Background = androidx.compose.ui.graphics.Color(0xFF121212)
    val Surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
    val TextPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val TextSecondary = androidx.compose.ui.graphics.Color(0xFFB0B0B0)
    val White = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val Error = androidx.compose.ui.graphics.Color(0xFFF44336)
    val Success = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    val CardRed = androidx.compose.ui.graphics.Color(0xFFE53935)
    val CardBlack = androidx.compose.ui.graphics.Color(0xFF2C2C2C)
}

/**
 * TarneebTypography - الخطوط
 */
val TarneebTypography = Typography(
    headlineLarge = androidx.compose.material3.MaterialTheme.typography.headlineLarge.copy(
        fontSize = androidx.compose.ui.unit.sp(32),
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    headlineMedium = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
        fontSize = androidx.compose.ui.unit.sp(24),
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    titleLarge = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
        fontSize = androidx.compose.ui.unit.sp(20),
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    bodyLarge = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
        fontSize = androidx.compose.ui.unit.sp(16)
    ),
    bodyMedium = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
        fontSize = androidx.compose.ui.unit.sp(14)
    ),
    labelMedium = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
        fontSize = androidx.compose.ui.unit.sp(12)
    )
)
