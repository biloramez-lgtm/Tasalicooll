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
        composable(Screen.Home.route) {
            HomeScreen(
                onStartGame = {
                    navController.navigate(Screen.Game.route)
                },
                onViewLeaderboard = {}
            )
        }
        
        composable(Screen.Game.route) {
            // Game Screen
        }
        
        composable(Screen.Multiplayer.route) {
            MultiplayerScreen()
        }
    }
}
