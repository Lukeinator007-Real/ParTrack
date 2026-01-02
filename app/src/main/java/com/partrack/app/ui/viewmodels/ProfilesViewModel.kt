package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.Player
import com.partrack.app.data.PlayerDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProfilesViewModel(private val playerDao: PlayerDao) : ViewModel() {

    val players: Flow<List<Player>> = playerDao.getAllPlayers()

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
}

class ProfilesViewModelFactory(private val playerDao: PlayerDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfilesViewModel(playerDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
