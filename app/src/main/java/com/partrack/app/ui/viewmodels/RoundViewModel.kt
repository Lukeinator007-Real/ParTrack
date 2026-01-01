package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.Round
import com.partrack.app.data.RoundDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RoundViewModel(
    private val roundDao: RoundDao,
    private val roundId: Long
) : ViewModel() {

    val round: Flow<Round?> = roundDao.getRoundByIdFlow(roundId)

    fun updateScore(playerName: String, holeNumber: Int, score: Int) {
        viewModelScope.launch {
            val currentRound = roundDao.getRoundById(roundId) ?: return@launch
            val currentScores = currentRound.scores.toMutableMap()
            val playerScores = currentScores[playerName]?.toMutableMap() ?: mutableMapOf()
            
            playerScores[holeNumber] = score
            currentScores[playerName] = playerScores
            
            val updatedRound = currentRound.copy(scores = currentScores)
            roundDao.updateRound(updatedRound)
        }
    }

    fun updateTotalHoles(newHoles: Int) {
        viewModelScope.launch {
            val currentRound = roundDao.getRoundById(roundId) ?: return@launch
            val updatedRound = currentRound.copy(holes = newHoles)
            roundDao.updateRound(updatedRound)
        }
    }

    fun finishRound() {
        viewModelScope.launch {
            val currentRound = roundDao.getRoundById(roundId) ?: return@launch
            val updatedRound = currentRound.copy(isFinished = true)
            roundDao.updateRound(updatedRound)
        }
    }

    fun getPlayerOrderForHole(round: Round, holeNumber: Int): List<String> {
        if (holeNumber == 1) return round.playerNames
        
        // Sort players based on previous hole(s) performance
        // If holeNumber is N, we look at scores for N-1.
        // If tie, look at N-2, etc.
        
        return round.playerNames.sortedWith { p1, p2 ->
            comparePlayers(p1, p2, holeNumber - 1, round.scores)
        }
    }

    private fun comparePlayers(p1: String, p2: String, holeToCheck: Int, scores: Map<String, Map<Int, Int>>): Int {
        if (holeToCheck < 1) {
            // If we went all the way back and still tied, preserve original order (or any stable order)
            // Original order is not explicitly stored as index in playerNames list here inside the comparator easily unless we pass indices.
            // But since sortedWith is stable, if we return 0, original order is preserved.
            // However, we want "player who started first beating the one who started after on hole 1"
            // This implies the original order in playerNames list is the tie breaker at the end.
            // Since sortedWith is stable, returning 0 should work if the input list is in original order.
            // But the input list to sortedWith might not be in original order if we just passed round.playerNames which IS in original order.
            return 0 
        }

        val s1 = scores[p1]?.get(holeToCheck) ?: 0
        val s2 = scores[p2]?.get(holeToCheck) ?: 0

        if (s1 != s2) {
            return s1 - s2 // Ascending score is better (lower is better in golf)
        } else {
            return comparePlayers(p1, p2, holeToCheck - 1, scores)
        }
    }
}

class RoundViewModelFactory(
    private val roundDao: RoundDao,
    private val roundId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoundViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoundViewModel(roundDao, roundId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
