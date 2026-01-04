package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.Player
import com.partrack.app.data.PlayerDao
import com.partrack.app.data.RoundDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val playerDao: PlayerDao,
    roundDao: RoundDao
) : ViewModel() {

    val players: Flow<List<Player>> = playerDao.getAllPlayers()
    
    val stats = roundDao.getAllRounds().map { rounds ->
        val totalRounds = rounds.size
        val totalHoles = rounds.sumOf { it.scores.values.sumOf { scores -> scores.size } }
        val holesInOne = rounds.sumOf { round ->
            if (round.isMiniGolf) {
                round.scores.values.sumOf { scores -> scores.count { it.value == 1 } }
            } else 0
        }
        Stats(totalRounds, totalHoles, holesInOne)
    }

    fun addPlayer(name: String) {
        viewModelScope.launch {
            playerDao.insertPlayer(Player(name = name))
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            playerDao.deletePlayer(player)
        }
    }

    data class Stats(val totalRounds: Int, val totalHoles: Int, val holesInOne: Int)
}

class ProfilesViewModelFactory(
    private val playerDao: PlayerDao,
    private val roundDao: RoundDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfilesViewModel(playerDao, roundDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
