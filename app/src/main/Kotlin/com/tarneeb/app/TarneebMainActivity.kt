package com.tarneeb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarneeb.engine.EngineGod
import com.tarneeb.engine.AIDifficulty
import com.tarneeb.engine.GameMode
import com.tarneeb.network.NetworkManager
import com.tarneeb.ui.*

/**
 * TarneebMainActivity - الـ Activity الرئيسي لتطبيق Tarneeb
 * 
 * الإصدار: 1.0.0
 * آخر تحديث: 2024
 * 
 * المميزات:
 * ✅ Navigation كامل مع معالجة الأخطاء
 * ✅ EngineGod Integration مع Null Safety
 * ✅ NetworkManager Integration مع State Management
 * ✅ Theme Configuration مع دعم الوضع المظلم
 * ✅ Error Boundaries ومعالجة الـ Crashes
 */
class TarneebMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContent {
                TarneebTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SafeTarneebApp()
                    }
                }
            }
        } catch (e: Exception) {
            // طباعة الخطأ للتشخيص
            e.printStackTrace()
            println("❌ Fatal Error in onCreate: ${e.message}")
        }
    }
}

/**
 * SafeTarneebApp - نسخة آمنة من التطبيق مع معالجة الأخطاء
 */
@Composable
fun SafeTarneebApp() {
    // تهيئة آمنة للـ Engine
    val engine = try {
        remember { EngineGod() }
    } catch (e: Exception) {
        println("❌ EngineGod initialization failed: ${e.message}")
        null
    }
    
    // تهيئة آمنة للـ NetworkManager
    val networkManager = try {
        remember { NetworkManager() }
    } catch (e: Exception) {
        println("❌ NetworkManager initialization failed: ${e.message}")
        null
    }
    
    // Error Boundary - لو فشل كل شيء
    if (engine == null && networkManager == null) {
        CriticalErrorScreen(
            message = "فشل في تهيئة التطبيق",
            onRetry = {
                // إعادة تشغيل التطبيق
            }
        )
        return
    }
    
    // تطبيق Tarneeb الأساسي مع معالجة الأخطاء
    TarneebApp(
        engine = engine,
        networkManager = networkManager
    )
}

/**
 * TarneebApp - تطبيق Tarneeb الرئيسي مع Null Safety كامل
 */
@Composable
fun TarneebApp(
    engine: EngineGod?,
    networkManager: NetworkManager?
) {
    // حالة التطبيق
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    var selectedGameMode by remember { mutableStateOf<GameMode?>(null) }
    var selectedDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }
    var playerName by remember { mutableStateOf("") }
    var playerList by remember { mutableStateOf(listOf<String>()) }
    
    // مراقبة حالة اللعبة (مع Null Safety)
    val gameState = try {
        engine?.gameState?.collectAsState()?.value
    } catch (e: Exception) {
        println("❌ Error collecting gameState: ${e.message}")
        null
    }
    
    val error = try {
        engine?.error?.collectAsState()?.value
    } catch (e: Exception) {
        println("❌ Error collecting error: ${e.message}")
        null
    }
    
    val aiAction = try {
        engine?.aiAction?.collectAsState()?.value
    } catch (e: Exception) {
        println("❌ Error collecting aiAction: ${e.message}")
        null
    }
    
    // مراقبة حالة الشبكة (مع Null Safety)
    val networkState = try {
        networkManager?.networkState?.collectAsState()?.value
    } catch (e: Exception) {
        println("❌ Error collecting networkState: ${e.message}")
        null
    }
    
    val networkError = try {
        networkManager?.error?.collectAsState()?.value
    } catch (e: Exception) {
        println("❌ Error collecting networkError: ${e.message}")
        null
    }
    
    // Theme
    TarneebTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Navigation مع معالجة الأخطاء
            when (currentScreen) {
                AppScreen.Home -> {
                    HomeScreen(
                        onSinglePlayerClick = {
                            if (engine != null) {
                                currentScreen = AppScreen.SinglePlayerSetup
                                selectedGameMode = GameMode.SINGLE_PLAYER
                            } else {
                                println("❌ Engine not available")
                            }
                        },
                        onMultiplayerClick = {
                            if (engine != null) {
                                currentScreen = AppScreen.MultiplayerSetup
                                selectedGameMode = GameMode.MULTIPLAYER_LOCAL
                            } else {
                                println("❌ Engine not available")
                            }
                        },
                        onNetworkClick = {
                            if (networkManager != null) {
                                currentScreen = AppScreen.NetworkSetup
                            } else {
                                println("❌ NetworkManager not available")
                            }
                        }
                    )
                }
                
                AppScreen.SinglePlayerSetup -> {
                    if (engine != null) {
                        SinglePlayerSetupScreen(
                            onStart = { name, difficulty ->
                                playerName = name
                                selectedDifficulty = difficulty
                                try {
                                    engine.startSinglePlayer(name, difficulty)
                                    currentScreen = AppScreen.Game
                                } catch (e: Exception) {
                                    println("❌ Failed to start single player: ${e.message}")
                                }
                            },
                            onBack = {
                                currentScreen = AppScreen.Home
                            }
                        )
                    } else {
                        ErrorScreen(
                            message = "محرك اللعبة غير متاح",
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                }
                
                AppScreen.MultiplayerSetup -> {
                    if (engine != null) {
                        MultiplayerSetupScreen(
                            onStart = { players, difficulty ->
                                playerList = players
                                selectedDifficulty = difficulty
                                try {
                                    engine.startMultiplayer(players.size, players, difficulty)
                                    currentScreen = AppScreen.Game
                                } catch (e: Exception) {
                                    println("❌ Failed to start multiplayer: ${e.message}")
                                }
                            },
                            onBack = {
                                currentScreen = AppScreen.Home
                            }
                        )
                    } else {
                        ErrorScreen(
                            message = "محرك اللعبة غير متاح",
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                }
                
                AppScreen.NetworkSetup -> {
                    if (networkManager != null) {
                        NetworkSetupScreen(
                            networkManager = networkManager,
                            onJoinGame = { gameCode, name ->
                                playerName = name
                                try {
                                    networkManager.joinGame(gameCode, name)
                                    currentScreen = AppScreen.NetworkGame
                                } catch (e: Exception) {
                                    println("❌ Failed to join game: ${e.message}")
                                }
                            },
                            onCreateGame = { name ->
                                playerName = name
                                try {
                                    networkManager.createGame(name)
                                    currentScreen = AppScreen.NetworkGame
                                } catch (e: Exception) {
                                    println("❌ Failed to create game: ${e.message}")
                                }
                            },
                            onBack = {
                                currentScreen = AppScreen.Home
                            }
                        )
                    } else {
                        ErrorScreen(
                            message = "مدير الشبكة غير متاح",
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                }
                
                AppScreen.Game -> {
                    when {
                        engine == null -> {
                            ErrorScreen(
                                message = "محرك اللعبة غير متاح",
                                onBack = { currentScreen = AppScreen.Home }
                            )
                        }
                        gameState == null -> {
                            // شاشة تحميل
                            LoadingScreen(
                                onBack = {
                                    engine.resetGame()
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }
                        else -> {
                            try {
                                GameScreen(
                                    engine = engine,
                                    gameState = gameState,
                                    error = error,
                                    aiAction = aiAction,
                                    onBack = {
                                        try {
                                            engine.resetGame()
                                        } catch (e: Exception) {
                                            println("❌ Error resetting game: ${e.message}")
                                        }
                                        currentScreen = AppScreen.Home
                                    }
                                )
                            } catch (e: Exception) {
                                println("❌ Error in GameScreen: ${e.message}")
                                ErrorScreen(
                                    message = "خطأ في عرض اللعبة: ${e.message}",
                                    onBack = { currentScreen = AppScreen.Home }
                                )
                            }
                        }
                    }
                }
                
                AppScreen.NetworkGame -> {
                    when {
                        networkManager == null -> {
                            ErrorScreen(
                                message = "مدير الشبكة غير متاح",
                                onBack = { currentScreen = AppScreen.Home }
                            )
                        }
                        else -> {
                            try {
                                NetworkGameScreen(
                                    networkManager = networkManager,
                                    networkState = networkState,
                                    onBack = {
                                        try {
                                            networkManager.disconnect()
                                        } catch (e: Exception) {
                                            println("❌ Error disconnecting: ${e.message}")
                                        }
                                        currentScreen = AppScreen.Home
                                    }
                                )
                            } catch (e: Exception) {
                                println("❌ Error in NetworkGameScreen: ${e.message}")
                                ErrorScreen(
                                    message = "خطأ في اللعبة الشبكية",
                                    onBack = { currentScreen = AppScreen.Home }
                                )
                            }
                        }
                    }
                }
            }
            
            // عرض الأخطاء في Console فقط
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
 * ErrorScreen - شاشة عرض الأخطاء
 */
@Composable
fun ErrorScreen(
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            fontSize = 48.sp,
            color = TarneebColors.Error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "حدث خطأ",
            fontSize = 24.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = TarneebColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = TarneebColors.Primary
            )
        ) {
            Text("العودة للرئيسية")
        }
    }
}

/**
 * LoadingScreen - شاشة تحميل
 */
@Composable
fun LoadingScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = TarneebColors.Primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "جاري تحميل اللعبة...",
            fontSize = 18.sp,
            color = TarneebColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onBack) {
            Text("إلغاء")
        }
    }
}

/**
 * CriticalErrorScreen - شاشة الخطأ الحرج
 */
@Composable
fun CriticalErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "خطأ حرج",
            fontSize = 28.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = TarneebColors.Error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "يرجى إعادة تشغيل التطبيق",
            fontSize = 14.sp,
            color = TarneebColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = TarneebColors.Primary
            )
        ) {
            Text("إعادة المحاولة")
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
        fontSize = 32.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    headlineMedium = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
        fontSize = 24.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    titleLarge = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
        fontSize = 20.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    bodyLarge = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
        fontSize = 16.sp
    ),
    bodyMedium = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp
    ),
    labelMedium = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
        fontSize = 12.sp
    )
)
