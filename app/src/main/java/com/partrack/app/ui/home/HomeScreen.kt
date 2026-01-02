package com.partrack.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partrack.app.data.AppDatabase
import com.partrack.app.data.Round
import com.partrack.app.ui.viewmodels.HomeViewModel
import com.partrack.app.ui.viewmodels.HomeViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewRound: () -> Unit,
    onRoundClick: (Long) -> Unit,
    onProfilesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(database.roundDao(), database.settingsDao()))
    val uiState by viewModel.uiState.collectAsState()
    
    var roundToDelete by remember { mutableStateOf<Round?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    if (roundToDelete != null) {
        AlertDialog(
            onDismissRequest = { roundToDelete = null },
            title = { Text("Delete Round") },
            text = { Text("Are you sure you want to delete this round?") },
            confirmButton = {
                TextButton(onClick = {
                    roundToDelete?.let { viewModel.deleteRound(it) }
                    roundToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { roundToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
             Column {
                 Row(
                     modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(16.dp).padding(top = 24.dp),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Column {
                         Text(
                             text = "ParTrack",
                             style = MaterialTheme.typography.headlineMedium,
                             color = MaterialTheme.colorScheme.onPrimary,
                             fontWeight = FontWeight.Bold
                         )
                         Text(
                             text = "Track your rounds",
                             style = MaterialTheme.typography.bodyMedium,
                             color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                         )
                     }
                     Row {
                         IconButton(onClick = onProfilesClick) {
                             Icon(
                                 imageVector = Icons.Filled.Person, 
                                 contentDescription = "Profiles", 
                                 tint = MaterialTheme.colorScheme.onPrimary
                             )
                         }
                         IconButton(onClick = onSettingsClick) {
                             Icon(
                                 imageVector = Icons.Filled.Settings, 
                                 contentDescription = "Settings", 
                                 tint = MaterialTheme.colorScheme.onPrimary
                             )
                         }
                     }
                 }
                 if (uiState.showTabs) {
                     PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                         Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Golf") })
                         Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Mini Golf") })
                     }
                 }
             }
        }
    ) { innerPadding ->
        val rounds = when {
            uiState.showTabs && selectedTabIndex == 0 -> uiState.rounds.filter { !it.isMiniGolf }
            uiState.showTabs && selectedTabIndex == 1 -> uiState.rounds.filter { it.isMiniGolf }
            else -> uiState.rounds
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(
                    onClick = onNewRound,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Round", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            item {
                Text(
                    text = "Your Rounds (${rounds.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(rounds) { round ->
                RoundItem(
                    round = round,
                    onClick = { onRoundClick(round.id) },
                    onDelete = { roundToDelete = round },
                    showTabs = uiState.showTabs
                )
            }
        }
    }
}

@Composable
fun RoundItem(
    round: Round, 
    onClick: () -> Unit,
    onDelete: () -> Unit,
    showTabs: Boolean
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(round.date))
    
    // Calculate progress
    val holesPlayed = if (round.scores.values.isNotEmpty()) {
         round.scores.values.flatMap { it.keys }.maxOrNull() ?: 0
    } else 0
    
    val progress = if (round.holes > 0) holesPlayed.toFloat() / round.holes.toFloat() else 0f
    
    // Determine leader
    val scores = round.scores
    val playerTotalScores = round.playerNames.map { player ->
         player to (scores[player]?.values?.sum() ?: 0)
    }
    val minScore = playerTotalScores.minOfOrNull { it.second } ?: 0
    val leaders = playerTotalScores.filter { it.second == minScore }.map { it.first }
    
    val leaderText = if (leaders.isNotEmpty()) {
        "Leading: ${leaders.first()}" + if (leaders.size > 1) " (+${leaders.size - 1})" else ""
    } else "No scores yet"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = round.name, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (round.isFinished) MaterialTheme.colorScheme.secondary.copy(alpha=0.2f) else Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (round.isFinished) "Finished" else "In Progress",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (round.isFinished) MaterialTheme.colorScheme.secondary else Color(0xFFFBC02D)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                if (!showTabs) {
                    Spacer(modifier = Modifier.width(8.dp))
                    if (round.isMiniGolf) {
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha=0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text("Mini Golf", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    } else {
                        Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha=0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text("Golf", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Filled.GolfCourse, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                     Spacer(modifier = Modifier.width(4.dp))
                     Text("${round.holes} holes", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                     Spacer(modifier = Modifier.width(4.dp))
                     Text("${round.playerNames.size} players", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Progress", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("$holesPlayed/${round.holes} holes", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.3f), 
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(leaderText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(onClick = onClick) {
                        Text("Continue >", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
