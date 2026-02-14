package com.tasalicool.game.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasalicool.game.network.ConnectionState
import com.tasalicool.game.ui.screens.*
import com.tasalicool.game.viewmodel.GameViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game")
    object Multiplayer : Screen("multiplayer")
    object Settings : Screen("settings")
    object GameOver : Screen("game_over")
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        // ================= HOME =================
        composable(Screen.Home.route) {
            HomeScreen(
                onSinglePlayerClick = {
                    navController.navigate(Screen.Game.route)
                },
                onMultiplayerClick = {
                    navController.navigate(Screen.Multiplayer.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // ================= GAME =================
        composable(Screen.Game.route) {

            val gameViewModel: GameViewModel = viewModel()

            GameScreen(
                viewModel = gameViewModel,
                onNavigateToGameOver = {
                    navController.navigate(Screen.GameOver.route)
                }
            )
        }

        // ================= MULTIPLAYER =================
        composable(Screen.Multiplayer.route) {

            MultiplayerScreen(
                onGameStart = {
                    navController.navigate(Screen.Game.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                connectionState = ConnectionState.DISCONNECTED,
                connectedPlayers = emptyList(),
                onHostGame = { },
                onJoinGame = { _, _ -> },
                onPlayerReady = { },
                onDisconnect = { }
            )
        }

        // ================= GAME OVER =================
        composable(Screen.GameOver.route) {
            GameOverScreen(
                onBackToHome = {
                    navController.popBackStack(
                        Screen.Home.route,
                        inclusive = false
                    )
                }
            )
        }

        // ================= SETTINGS =================
        composable(Screen.Settings.route) {
            SettingsDialog(
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
    }
}
