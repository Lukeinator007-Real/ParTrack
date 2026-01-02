package com.partrack.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.partrack.app.ui.home.HomeScreen
import com.partrack.app.ui.profiles.ProfilesScreen
import com.partrack.app.ui.round.NewRoundScreen
import com.partrack.app.ui.round.RoundScreen

@Composable
fun ParTrackApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNewRound = { navController.navigate("new_round") },
                onRoundClick = { roundId -> navController.navigate("round/$roundId") },
                onProfilesClick = { navController.navigate("profiles") }
            )
        }
        composable("new_round") {
            NewRoundScreen(
                onRoundCreated = { roundId ->
                    navController.navigate("round/$roundId") {
                        popUpTo("home")
                    }
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable("round/{roundId}") { backStackEntry ->
            val roundId = backStackEntry.arguments?.getString("roundId")?.toLongOrNull() ?: 0L
            RoundScreen(
                roundId = roundId,
                onNavigateUp = { navController.navigateUp() },
                onFinishRound = { navController.navigateUp() }
            )
        }
        composable("profiles") {
            ProfilesScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}
