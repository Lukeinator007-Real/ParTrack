package com.partrack.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds")
data class Round(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: Long,
    val holes: Int,
    val playerNames: List<String>, // Stored as a JSON string or comma-separated list via TypeConverter
    val isFinished: Boolean = false,
    val scores: Map<String, Map<Int, Int>>, // PlayerName -> (HoleNumber -> Score)
    val pars: List<Int>? = null, // List of par values for each hole. Nullable for backward compatibility.
    val isMiniGolf: Boolean = false // Flag for Mini Golf vs Standard Golf
)
