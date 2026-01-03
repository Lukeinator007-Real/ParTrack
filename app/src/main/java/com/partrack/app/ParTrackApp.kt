package com.partrack.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.partrack.app.ui.home.HomeScreen
import com.partrack.app.ui.profiles.ProfilesScreen
import com.partrack.app.ui.round.NewRoundScreen
import com.partrack.app.ui.round.RoundScreen
import com.partrack.app.ui.round.RoundSettingsScreen
import com.partrack.app.ui.round.ScorecardScreen
import com.partrack.app.ui.settings.SettingsScreen

@Composable
fun ParTrackApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNewRound = { navController.navigate("new_round") },
                onRoundClick = { roundId -> navController.navigate("round/$roundId") },
                onProfilesClick = { navController.navigate("profiles") },
                onSettingsClick = { navController.navigate("settings") }
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
                onFinishRound = { navController.navigateUp() },
                onScorecardClick = { navController.navigate("scorecard/$roundId") },
                onSettingsClick = { navController.navigate("round_settings/$roundId") }
            )
        }
        composable("round_settings/{roundId}") { backStackEntry ->
            val roundId = backStackEntry.arguments?.getString("roundId")?.toLongOrNull() ?: 0L
            RoundSettingsScreen(
                roundId = roundId,
                onNavigateUp = { navController.navigateUp() },
                onDeleteConfirmed = { 
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("scorecard/{roundId}") { backStackEntry ->
            val roundId = backStackEntry.arguments?.getString("roundId")?.toLongOrNull() ?: 0L
            ScorecardScreen(
                roundId = roundId,
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable("profiles") {
            ProfilesScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable("settings") {
            SettingsScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}
