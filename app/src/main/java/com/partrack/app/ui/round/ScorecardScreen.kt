package com.partrack.app.ui.round

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partrack.app.data.AppDatabase
import com.partrack.app.ui.viewmodels.RoundViewModel
import com.partrack.app.ui.viewmodels.RoundViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardScreen(
    roundId: Long,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: RoundViewModel = viewModel(factory = RoundViewModelFactory(database.roundDao(), roundId))
    val round by viewModel.round.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scorecard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (round == null) {
            Box(modifier = Modifier.padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
            return@Scaffold
        }

        val currentRound = round!!
        val players = currentRound.playerNames
        val pars = currentRound.pars ?: List(currentRound.holes) { 0 }

        Row(Modifier.padding(innerPadding).horizontalScroll(rememberScrollState())) {
            // Player Names Column
            Column {
                ScorecardCell("Player", isHeader = true)
                pars.forEachIndexed { index, _ -> ScorecardCell("Par ${index + 1}", isHeader = true) }
                ScorecardCell("Total", isHeader = true)
            }

            // Score Columns
            players.forEach { player ->
                Column {
                    ScorecardCell(player, isHeader = true)
                    var total = 0
                    (1..currentRound.holes).forEach { hole ->
                        val score = currentRound.scores[player]?.get(hole) ?: 0
                        total += score
                        ScorecardCell(if (score > 0) "$score" else "-")
                    }
                    ScorecardCell("$total", isHeader = true, isTotal = true)
                }
            }
        }
    }
}

@Composable
fun ScorecardCell(text: String, isHeader: Boolean = false, isTotal: Boolean = false) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .padding(4.dp)
            .background(if (isHeader) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
    }
}
