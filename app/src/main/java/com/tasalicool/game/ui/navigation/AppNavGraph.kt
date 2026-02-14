package com.tasalicool.game.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

        // HOME
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

        // GAME
        composable(Screen.Game.route) {
            GameScreen() // تأكد إنها موجودة
        }

        // MULTIPLAYER
        composable(Screen.Multiplayer.route) {
            MultiplayerScreen()
        }

        // SETTINGS
        composable(Screen.Settings.route) {
            SettingsDialog(
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
    }
}
