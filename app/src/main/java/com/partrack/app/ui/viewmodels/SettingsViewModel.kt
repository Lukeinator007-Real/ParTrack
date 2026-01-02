package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.AppSettings
import com.partrack.app.data.SettingsDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsDao: SettingsDao) : ViewModel() {

    val settings = settingsDao.getSettings().filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setDefaultGameMode(isMiniGolf: Boolean) {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsDao.saveSettings(currentSettings.copy(defaultGameModeIsMiniGolf = isMiniGolf))
        }
    }

    fun setShowTabsOnHome(showTabs: Boolean) {
        viewModelScope.launch {
            val currentSettings = settings.value
            settingsDao.saveSettings(currentSettings.copy(showTabsOnHome = showTabs))
        }
    }
}

class SettingsViewModelFactory(private val settingsDao: SettingsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
