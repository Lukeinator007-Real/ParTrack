package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.partrack.app.data.PlayerDao
import com.partrack.app.data.RoundDao
import kotlinx.coroutines.flow.combine

class PlayerDetailsViewModel(
    playerDao: PlayerDao,
    roundDao: RoundDao,
    playerId: Long
) : ViewModel() {

    val player = playerDao.getPlayerById(playerId)

    val stats = combine(player, roundDao.getAllRounds()) { currentPlayer, rounds ->
        if (currentPlayer == null) {
            ProfilesViewModel.Stats(0, 0, 0)
        } else {
            val playerRounds = rounds.filter { it.playerNames.contains(currentPlayer.name) }
            val totalRounds = playerRounds.size
            val totalHoles = playerRounds.sumOf { it.scores[currentPlayer.name]?.size ?: 0 }
            val holesInOne = playerRounds.sumOf { round ->
                if (round.isMiniGolf) {
                    round.scores[currentPlayer.name]?.count { it.value == 1 } ?: 0
                } else 0
            }
            ProfilesViewModel.Stats(totalRounds, totalHoles, holesInOne)
        }
    }
}

class PlayerDetailsViewModelFactory(
    private val playerDao: PlayerDao,
    private val roundDao: RoundDao,
    private val playerId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerDetailsViewModel(playerDao, roundDao, playerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
