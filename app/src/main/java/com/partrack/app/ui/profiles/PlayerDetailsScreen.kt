package com.partrack.app.ui.profiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partrack.app.data.AppDatabase
import com.partrack.app.ui.viewmodels.PlayerDetailsViewModel
import com.partrack.app.ui.viewmodels.PlayerDetailsViewModelFactory
import com.partrack.app.ui.viewmodels.ProfilesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailsScreen(
    playerId: Long,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: PlayerDetailsViewModel = viewModel(factory = PlayerDetailsViewModelFactory(database.playerDao(), database.roundDao(), playerId))
    val player by viewModel.player.collectAsState(initial = null)
    val stats by viewModel.stats.collectAsState(initial = ProfilesViewModel.Stats(0, 0, 0))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(player?.name ?: "Player Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Overall Stats", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard("Total Rounds", "${stats.totalRounds}", Modifier.weight(1f))
                StatCard("Holes Played", "${stats.totalHoles}", Modifier.weight(1f))
                StatCard("Holes-in-One", "${stats.holesInOne}", Modifier.weight(1f))
            }
        }
    }
}
