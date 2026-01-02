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

    fun updateParForHole(holeIndex: Int, newPar: Int) {
        viewModelScope.launch {
            val currentRound = roundDao.getRoundById(roundId) ?: return@launch
            val currentPars = currentRound.pars?.toMutableList() ?: return@launch
            
            if (holeIndex >= 0 && holeIndex < currentPars.size) {
                currentPars[holeIndex] = newPar
                val updatedRound = currentRound.copy(pars = currentPars)
                roundDao.updateRound(updatedRound)
            }
        }
    }

    fun updateRoundDetails(name: String, holes: Int) {
        viewModelScope.launch {
            val currentRound = roundDao.getRoundById(roundId) ?: return@launch
            val updatedRound = currentRound.copy(name = name, holes = holes)
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
        return round.playerNames.sortedWith { p1, p2 ->
            comparePlayers(p1, p2, holeNumber - 1, round.scores)
        }
    }

    private fun comparePlayers(p1: String, p2: String, holeToCheck: Int, scores: Map<String, Map<Int, Int>>): Int {
        if (holeToCheck < 1) {
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
