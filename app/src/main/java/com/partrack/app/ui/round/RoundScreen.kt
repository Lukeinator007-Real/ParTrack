package com.partrack.app.ui.round

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partrack.app.data.AppDatabase
import com.partrack.app.ui.viewmodels.RoundViewModel
import com.partrack.app.ui.viewmodels.RoundViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoundScreen(
    roundId: Long,
    onNavigateUp: () -> Unit,
    onFinishRound: () -> Unit,
    onScorecardClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: RoundViewModel = viewModel(factory = RoundViewModelFactory(database.roundDao(), roundId))
    val round by viewModel.round.collectAsState(initial = null)
    
    // We need round to be loaded
    if (round == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
        return
    }

    val currentRound = round!!
    val totalHoles = currentRound.holes
    val pagerState = rememberPagerState(pageCount = { totalHoles })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(currentRound.name)
                        Text("Hole ${pagerState.currentPage + 1}", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onScorecardClick) {
                        Icon(Icons.Filled.List, contentDescription = "Scorecard")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Edit Round Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val holeNumber = page + 1
                // Determine player order for this hole
                val playerOrder = viewModel.getPlayerOrderForHole(currentRound, holeNumber)
                val par = currentRound.pars?.getOrNull(page)
                
                HoleContent(
                    holeNumber = holeNumber,
                    par = par,
                    isMiniGolf = currentRound.isMiniGolf,
                    players = playerOrder,
                    scores = currentRound.scores,
                    onScoreChange = { player, score ->
                        viewModel.updateScore(player, holeNumber, score)
                    },
                    onParChange = { newPar ->
                        viewModel.updateParForHole(page, newPar)
                    }
                )
            }
            
            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { 
                         scope.launch { 
                             if (pagerState.currentPage > 0) pagerState.animateScrollToPage(pagerState.currentPage - 1) 
                         } 
                    },
                    enabled = pagerState.currentPage > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Previous")
                }

                if (pagerState.currentPage < totalHoles - 1) {
                    Button(
                        onClick = { 
                             scope.launch { 
                                 pagerState.animateScrollToPage(pagerState.currentPage + 1) 
                             } 
                        }
                    ) {
                        Text("Next Hole")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.finishRound()
                            onFinishRound()
                        }
                    ) {
                        Text("Finish Round")
                    }
                }
            }
        }
    }
}

@Composable
fun HoleContent(
    holeNumber: Int,
    par: Int?,
    isMiniGolf: Boolean,
    players: List<String>,
    scores: Map<String, Map<Int, Int>>,
    onScoreChange: (String, Int) -> Unit,
    onParChange: (Int) -> Unit
) {
    var showParDialog by remember { mutableStateOf(false) }

    if (showParDialog) {
        EditParDialog(
            currentPar = par ?: if (isMiniGolf) 2 else 4,
            onDismiss = { showParDialog = false },
            onConfirm = { newPar ->
                onParChange(newPar)
                showParDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$holeNumber",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showParDialog = true }
                        .padding(8.dp)
                ) {
                    Icon(
                         imageVector = Icons.Filled.Star, 
                         contentDescription = null,
                         modifier = Modifier.size(16.dp),
                         tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Par ${par ?: "-"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Par",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Hitting Order Tag
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                     Text(
                        text = "Hitting Order",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        itemsIndexed(players) { index, player ->
            val playerScore = scores[player]?.get(holeNumber) ?: 0
            val totalScore = scores[player]?.values?.sum() ?: 0
            
            PlayerScoreCard(
                playerName = player,
                score = playerScore,
                par = par,
                isMiniGolf = isMiniGolf,
                totalScore = totalScore,
                isFirst = index == 0,
                onScoreChange = { newScore -> onScoreChange(player, newScore) }
            )
        }
    }
}

@Composable
fun PlayerScoreCard(
    playerName: String,
    score: Int,
    par: Int?,
    isMiniGolf: Boolean,
    totalScore: Int,
    isFirst: Boolean,
    onScoreChange: (Int) -> Unit
) {
    var showScorePicker by remember { mutableStateOf(false) }

    if (showScorePicker) {
        ScorePickerDialog(
            currentScore = score,
            onScoreSelected = { newScore ->
                onScoreChange(newScore)
                showScorePicker = false
            },
            onDismiss = { showScorePicker = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = if(isFirst) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFirst) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1", 
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = playerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total: $totalScore",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (par != null && score > 0) {
                        val diff = score - par
                        val diffText = if (diff > 0) "+$diff" else if (diff == 0) "E" else "$diff"
                        val color = if (diff > 0) Color.Red else if (diff < 0) Color(0xFF006400) else Color.Gray 
                        
                        if (isMiniGolf && score == 1) {
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(
                                 text = "Hole-in-One!",
                                 style = MaterialTheme.typography.bodySmall,
                                 color = Color(0xFFD4AF37),
                                 fontWeight = FontWeight.Bold
                             )
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "($diffText)",
                                style = MaterialTheme.typography.bodySmall,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Score Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick decrement
                IconButton(
                    onClick = { if (score > 0) onScoreChange(score - 1) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease Score", modifier = Modifier.size(16.dp))
                }
                
                Spacer(modifier = Modifier.width(8.dp))

                // Score Box (Clickable)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(12.dp))
                        .clickable { showScorePicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (score > 0) "$score" else "-",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                
                // Quick increment
                IconButton(
                    onClick = { onScoreChange(score + 1) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase Score", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ScorePickerDialog(
    currentScore: Int,
    onScoreSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Score", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..3).forEach { score ->
                        ScoreButton(score, currentScore, onScoreSelected)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (4..6).forEach { score ->
                        ScoreButton(score, currentScore, onScoreSelected)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ScoreButton(
    score: Int,
    currentScore: Int,
    onClick: (Int) -> Unit
) {
    val isSelected = score == currentScore
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick(score) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score",
            style = MaterialTheme.typography.titleLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun EditHolesDialog(
    currentHoles: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentHoles.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Number of Holes") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                label = { Text("Holes") }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val holes = text.toIntOrNull()
                if (holes != null && holes > 0) {
                    onConfirm(holes)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditParDialog(
    currentPar: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentPar.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Par") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                label = { Text("Par") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val par = text.toIntOrNull()
                if (par != null && par > 0) {
                    onConfirm(par)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
