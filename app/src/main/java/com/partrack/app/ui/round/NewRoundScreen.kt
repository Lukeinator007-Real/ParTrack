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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partrack.app.data.AppDatabase
import com.partrack.app.data.Course
import com.partrack.app.ui.viewmodels.NewRoundViewModel
import com.partrack.app.ui.viewmodels.NewRoundViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRoundScreen(
    onRoundCreated: (Long) -> Unit,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: NewRoundViewModel = viewModel(
        factory = NewRoundViewModelFactory(database.courseDao(), database.roundDao())
    )

    val courses by viewModel.courses.collectAsState(initial = emptyList())

    var roundName by remember { mutableStateOf("") }
    var selectedHolesOption by remember { mutableIntStateOf(9) }
    var customHoles by remember { mutableStateOf("") }
    
    // Course Selection
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var expandedCourseDropdown by remember { mutableStateOf(false) }
    var isCreatingNewCourse by remember { mutableStateOf(false) }
    
    // Game Mode
    var isMiniGolf by remember { mutableStateOf(false) }

    // Pars
    val pars = remember { mutableStateListOf<Int>() }

    // Players
    val players = remember { mutableStateListOf("") }
    if (players.isEmpty()) {
        players.add("")
    }

    // Initialize Pars when holes change
    val currentHolesCount = if (selectedHolesOption == -1) {
        customHoles.toIntOrNull() ?: 9
    } else {
        selectedHolesOption
    }

    // Update pars list size when hole count changes
    if (pars.size != currentHolesCount && !isCreatingNewCourse) {
         if (currentHolesCount > pars.size) {
             val defaultPar = if (isMiniGolf) 2 else 4
             repeat(currentHolesCount - pars.size) { pars.add(defaultPar) } 
         } else {
             while (pars.size > currentHolesCount) {
                 pars.removeLast()
             }
         }
         
         // If switching modes, maybe update existing default pars? 
         // For simplicity, let's just ensure defaults for new entries are correct.
         // Or we could force reset if user toggles mode? 
         // Let's reset pars if user toggles mode and hasn't manually edited them (complex to track).
         // Simple approach: When toggling mode, reset all pars to new default.
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
            // Course Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCourseDropdown,
                onExpandedChange = { expandedCourseDropdown = !expandedCourseDropdown }
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    readOnly = true,
                    value = selectedCourse?.name ?: "Select Course (Optional)",
                    onValueChange = {},
                    label = { Text("Course") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourseDropdown) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = expandedCourseDropdown,
                    onDismissRequest = { expandedCourseDropdown = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Custom Round)") },
                        onClick = {
                            selectedCourse = null
                            isCreatingNewCourse = false
                            expandedCourseDropdown = false
                            roundName = ""
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course.name) },
                            onClick = {
                                selectedCourse = course
                                isCreatingNewCourse = false
                                expandedCourseDropdown = false
                                roundName = course.name
                                selectedHolesOption = course.holes
                                pars.clear()
                                pars.addAll(course.pars)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ Add New Course") },
                        onClick = {
                            selectedCourse = null
                            isCreatingNewCourse = true
                            expandedCourseDropdown = false
                            roundName = ""
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = roundName,
                onValueChange = { roundName = it },
                label = { Text(if (isCreatingNewCourse) "New Course Name" else "Round Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Game Mode Selection
            if (selectedCourse == null || isCreatingNewCourse) {
                Text("Game Mode", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isMiniGolf,
                        onClick = { 
                            if (isMiniGolf) {
                                isMiniGolf = false
                                // Reset pars to 4
                                pars.clear()
                                repeat(currentHolesCount) { pars.add(4) }
                            }
                        }
                    )
                    Text("Standard Golf")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = isMiniGolf,
                        onClick = { 
                            if (!isMiniGolf) {
                                isMiniGolf = true
                                // Reset pars to 2
                                pars.clear()
                                repeat(currentHolesCount) { pars.add(2) }
                            }
                        }
                    )
                    Text("Mini Golf")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Number of Holes", style = MaterialTheme.typography.titleMedium)
            
            // Disable changing holes if a preset course is selected (unless we edit it, but keep simple for now)
            val holesEnabled = selectedCourse == null || isCreatingNewCourse

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedHolesOption == 9,
                    onClick = { 
                        if(holesEnabled) {
                            selectedHolesOption = 9 
                            // Re-init pars logic handles the list update
                        }
                    },
                    enabled = holesEnabled
                )
                Text("9 Holes")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = selectedHolesOption == 18,
                    onClick = { 
                         if(holesEnabled) {
                             selectedHolesOption = 18 
                         }
                    },
                    enabled = holesEnabled
                )
                Text("18 Holes")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedHolesOption == -1,
                    onClick = { 
                         if(holesEnabled) {
                             selectedHolesOption = -1 
                         }
                    },
                    enabled = holesEnabled
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
                    enabled = holesEnabled && selectedHolesOption == -1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Par Configuration Section
            if (isCreatingNewCourse || selectedCourse == null) { // Show if creating new or custom round
                Text("Pars", style = MaterialTheme.typography.titleMedium)
                Text("Scroll horizontally to set pars for all holes", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentHolesCount) { index ->
                        // Ensure pars list is big enough (safe access)
                        if (index < pars.size) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hole ${index + 1}")
                                OutlinedTextField(
                                    value = pars[index].toString(),
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() } && newValue.isNotEmpty()) {
                                            pars[index] = newValue.toInt()
                                        } else if (newValue.isEmpty()) {
                                             // Allow empty temporarily for editing, handle validation later or default
                                        }
                                    },
                                    modifier = Modifier.width(60.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            } else {
                 // If course is selected, maybe just show summary?
                 Text("Pars: ${pars.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    val validPlayers = players.filter { it.isNotBlank() }
                    val finalPlayers = if (validPlayers.isEmpty()) listOf("Player 1") else validPlayers
                    
                    val name = if (roundName.isBlank()) "Round ${System.currentTimeMillis()}" else roundName
                    
                    if (isCreatingNewCourse) {
                        viewModel.addCourse(name, currentHolesCount, pars.toList())
                    }

                    viewModel.createRound(
                        name = name,
                        holes = currentHolesCount,
                        playerNames = finalPlayers,
                        pars = pars.toList(),
                        isMiniGolf = isMiniGolf,
                        onResult = onRoundCreated
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isCreatingNewCourse) "Save Course & Start Round" else "Start Round")
            }
        }
    }
}
