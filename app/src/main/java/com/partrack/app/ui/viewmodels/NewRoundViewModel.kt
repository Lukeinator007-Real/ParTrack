package com.partrack.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partrack.app.data.AppSettings
import com.partrack.app.data.Course
import com.partrack.app.data.CourseDao
import com.partrack.app.data.Player
import com.partrack.app.data.PlayerDao
import com.partrack.app.data.Round
import com.partrack.app.data.RoundDao
import com.partrack.app.data.SettingsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewRoundViewModel(
    private val courseDao: CourseDao,
    private val roundDao: RoundDao,
    private val playerDao: PlayerDao,
    private val settingsDao: SettingsDao
) : ViewModel() {

    val courses: Flow<List<Course>> = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val players: Flow<List<Player>> = playerDao.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: Flow<AppSettings> = settingsDao.getSettings().filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun createRound(
        name: String,
        holes: Int,
        playerNames: List<String>,
        pars: List<Int>?,
        isMiniGolf: Boolean,
        onResult: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newRound = Round(
                name = name,
                date = System.currentTimeMillis(),
                holes = holes,
                playerNames = playerNames,
                scores = emptyMap(),
                pars = pars,
                isMiniGolf = isMiniGolf
            )
            val id = roundDao.insertRound(newRound)
            onResult(id)
        }
    }

    fun addCourse(name: String, holes: Int, pars: List<Int>) {
        viewModelScope.launch {
            val newCourse = Course(
                name = name,
                holes = holes,
                pars = pars
            )
            courseDao.insertCourse(newCourse)
        }
    }

    fun addPlayer(name: String) {
        viewModelScope.launch {
            playerDao.insertPlayer(Player(name = name))
        }
    }
}

class NewRoundViewModelFactory(
    private val courseDao: CourseDao,
    private val roundDao: RoundDao,
    private val playerDao: PlayerDao,
    private val settingsDao: SettingsDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewRoundViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewRoundViewModel(courseDao, roundDao, playerDao, settingsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
