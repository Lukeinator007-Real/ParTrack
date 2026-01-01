package com.partrack.app.ui.round

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partrack.app.data.AppDatabase
import com.partrack.app.data.Round
import com.partrack.app.ui.viewmodels.RoundViewModel
import com.partrack.app.ui.viewmodels.RoundViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoundScreen(
    roundId: Long,
    onNavigateUp: () -> Unit,
    onFinishRound: () -> Unit
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
    
    var showEditHolesDialog by remember { mutableStateOf(false) }

    if (showEditHolesDialog) {
        EditHolesDialog(
            currentHoles = totalHoles,
            onDismiss = { showEditHolesDialog = false },
            onConfirm = { newHoles ->
                viewModel.updateTotalHoles(newHoles)
                showEditHolesDialog = false
            }
        )
    }

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
                    IconButton(onClick = { showEditHolesDialog = true }) {
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
            // Hole Tabs (Scrollable if many holes)
            // A simple scrollable row of buttons for holes could be better than TabRow if many holes
            /* 
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp
            ) {
                for (i in 0 until totalHoles) {
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(i) }
                        },
                        text = { Text("${i + 1}") }
                    )
                }
            }
            */

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val holeNumber = page + 1
                // Determine player order for this hole
                val playerOrder = viewModel.getPlayerOrderForHole(currentRound, holeNumber)
                
                HoleContent(
                    holeNumber = holeNumber,
                    players = playerOrder,
                    scores = currentRound.scores,
                    onScoreChange = { player, score ->
                        viewModel.updateScore(player, holeNumber, score)
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
                    enabled = pagerState.currentPage > 0
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
    players: List<String>,
    scores: Map<String, Map<Int, Int>>,
    onScoreChange: (String, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Hole $holeNumber",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Hitting Order",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        itemsIndexed(players) { index, player ->
            val playerScore = scores[player]?.get(holeNumber) ?: 0
            val totalScore = scores[player]?.values?.sum() ?: 0
            
            PlayerScoreCard(
                playerName = player,
                score = playerScore,
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
    totalScore: Int,
    isFirst: Boolean,
    onScoreChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isFirst) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total: $totalScore",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (score > 0) onScoreChange(score - 1) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease Score")
                }
                
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onScoreChange(score + 1) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase Score")
                }
            }
        }
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
