package com.partrack.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Query("SELECT * FROM rounds ORDER BY date DESC")
    fun getAllRounds(): Flow<List<Round>>

    @Query("SELECT * FROM rounds WHERE id = :id")
    suspend fun getRoundById(id: Long): Round?

    @Query("SELECT * FROM rounds WHERE id = :id")
    fun getRoundByIdFlow(id: Long): Flow<Round?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: Round): Long

    @Update
    suspend fun updateRound(round: Round)

    @Delete
    suspend fun deleteRound(round: Round)
}
