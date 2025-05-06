package com.example.lotocontrol.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lotocontrol.ui.screens.home.HomeScreen
import com.example.lotocontrol.ui.screens.client.ClientScreen
import com.example.lotocontrol.ui.screens.summary.SummaryScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Client : Screen("client/{clientId}") {
        fun createRoute(clientId: Long) = "client/$clientId"
    }
    object Summary : Screen("summary")
}

@Composable
fun LotoControlNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onClientClick = { clientId ->
                    navController.navigate(Screen.Client.createRoute(clientId))
                },
                onSummaryClick = {
                    navController.navigate(Screen.Summary.route)
                }
            )
        }

        composable(
            route = Screen.Client.route,
            arguments = listOf(
                navArgument("clientId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getLong("clientId") ?: return@composable
            ClientScreen(
                clientId = clientId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNextClient = { nextClientId ->
                    navController.navigate(Screen.Client.createRoute(nextClientId)) {
                        popUpTo(Screen.Client.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Summary.route) {
            SummaryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
