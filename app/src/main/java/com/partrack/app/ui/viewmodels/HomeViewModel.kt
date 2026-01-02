package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.AppSettings
import com.partrack.app.data.Round
import com.partrack.app.data.RoundDao
import com.partrack.app.data.SettingsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val roundDao: RoundDao,
    private val settingsDao: SettingsDao
) : ViewModel() {

    private val allRounds: Flow<List<Round>> = roundDao.getAllRounds()
    private val settings: Flow<AppSettings> = settingsDao.getSettings().filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val uiState = combine(allRounds, settings) { rounds, appSettings ->
        UiState(
            rounds = rounds,
            showTabs = appSettings.showTabsOnHome
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun deleteRound(round: Round) {
        viewModelScope.launch {
            roundDao.deleteRound(round)
        }
    }

    data class UiState(
        val rounds: List<Round> = emptyList(),
        val showTabs: Boolean = true
    )
}

class HomeViewModelFactory(
    private val roundDao: RoundDao,
    private val settingsDao: SettingsDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(roundDao, settingsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
