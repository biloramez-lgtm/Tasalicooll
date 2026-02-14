package com.tasalicool.game.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tasalicool.game.network.ConnectionState
import com.tasalicool.game.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game")
    object Multiplayer : Screen("multiplayer")
    object Settings : Screen("settings")
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
            GameScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ================= MULTIPLAYER =================
        composable(Screen.Multiplayer.route) {

            MultiplayerScreen(
                onGameStart = {
                    // لما يضغط Start Game
                    navController.navigate(Screen.Game.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                connectionState = ConnectionState.DISCONNECTED,
                connectedPlayers = emptyList(),
                onHostGame = { /* TODO networking */ },
                onJoinGame = { _, _ -> /* TODO networking */ },
                onPlayerReady = { /* TODO */ },
                onDisconnect = { }
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
