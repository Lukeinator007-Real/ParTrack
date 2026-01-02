package com.partrack.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val defaultGameModeIsMiniGolf: Boolean = false,
    val showTabsOnHome: Boolean = true // New setting for home screen tabs
)
