package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.Round
import com.partrack.app.data.RoundDao
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(private val roundDao: RoundDao) : ViewModel() {
    val rounds = roundDao.getAllRounds()

    fun deleteRound(round: Round) {
        viewModelScope.launch {
            roundDao.deleteRound(round)
        }
    }
}

class HomeViewModelFactory(private val roundDao: RoundDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(roundDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
