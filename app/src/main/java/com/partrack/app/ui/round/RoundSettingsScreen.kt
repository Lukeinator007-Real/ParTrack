package com.partrack.app.ui.round

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partrack.app.data.AppDatabase
import com.partrack.app.ui.viewmodels.RoundViewModel
import com.partrack.app.ui.viewmodels.RoundViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundSettingsScreen(
    roundId: Long,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: RoundViewModel = viewModel(factory = RoundViewModelFactory(database.roundDao(), roundId))
    val round by viewModel.round.collectAsState(initial = null)

    var roundName by remember(round) { mutableStateOf(round?.name ?: "") }
    var holes by remember(round) { mutableStateOf(round?.holes ?: 9) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Round Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.updateRoundDetails(roundName, holes)
                    onNavigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Changes")
            }
        }
    ) { innerPadding ->
        round?.let {
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                OutlinedTextField(
                    value = roundName,
                    onValueChange = { roundName = it },
                    label = { Text("Round Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Number of Holes", style = MaterialTheme.typography.titleMedium)
                Text("You can change this even during a round", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { if (holes > 1) holes-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease holes")
                    }
                    Text("$holes", style = MaterialTheme.typography.headlineLarge)
                    IconButton(onClick = { holes++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase holes")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Initial Hitting Order", style = MaterialTheme.typography.titleMedium)
                Text("This determines who hits first on hole 1 and breaks ties", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                // Player reordering can be added here

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = { 
                    // viewModel.deleteRound(it) // Need to handle deletion and navigation
                    onNavigateUp() 
                }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Text("Delete Round", color = Color.Red)
                }
            }
        }
    }
}
