package com.partrack.app.ui.round

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.partrack.app.data.AppDatabase
import com.partrack.app.data.Round
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRoundScreen(
    onRoundCreated: (Long) -> Unit,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getDatabase(context)

    var roundName by remember { mutableStateOf("") }
    var selectedHolesOption by remember { mutableIntStateOf(9) }
    var customHoles by remember { mutableStateOf("") }
    val players = remember { mutableStateListOf("") }

    // Initial player
    if (players.isEmpty()) {
        players.add("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Round") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = roundName,
                onValueChange = { roundName = it },
                label = { Text("Round Name (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Number of Holes", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedHolesOption == 9,
                    onClick = { selectedHolesOption = 9 }
                )
                Text("9 Holes")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = selectedHolesOption == 18,
                    onClick = { selectedHolesOption = 18 }
                )
                Text("18 Holes")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedHolesOption == -1,
                    onClick = { selectedHolesOption = -1 }
                )
                Text("Custom: ")
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = customHoles,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            customHoles = it
                            selectedHolesOption = -1
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    enabled = selectedHolesOption == -1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Players", style = MaterialTheme.typography.titleMedium)
            Text("Enter names in hitting order", style = MaterialTheme.typography.bodySmall)
            
            Column {
                players.forEachIndexed { index, name ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { players[index] = it },
                            label = { Text("Player ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        if (players.size > 1) {
                            IconButton(onClick = { players.removeAt(index) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove Player")
                            }
                        }
                    }
                }
            }
            
            OutlinedButton(
                onClick = { players.add("") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Player")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val holes = if (selectedHolesOption == -1) {
                        customHoles.toIntOrNull() ?: 9
                    } else {
                        selectedHolesOption
                    }
                    
                    val validPlayers = players.filter { it.isNotBlank() }
                    val finalPlayers = if (validPlayers.isEmpty()) listOf("Player 1") else validPlayers
                    
                    val name = if (roundName.isBlank()) "Round ${System.currentTimeMillis()}" else roundName

                    scope.launch {
                        val newRound = Round(
                            name = name,
                            date = System.currentTimeMillis(),
                            holes = holes,
                            playerNames = finalPlayers,
                            scores = emptyMap()
                        )
                        val id = database.roundDao().insertRound(newRound)
                        onRoundCreated(id)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Round")
            }
        }
    }
}
