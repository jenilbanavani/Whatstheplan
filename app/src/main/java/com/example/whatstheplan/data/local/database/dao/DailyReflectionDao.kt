package com.example.whatstheplan.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReflectionDao {
    @Query("SELECT * FROM daily_reflections WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyReflectionEntity?>

    @Query("SELECT * FROM daily_reflections ORDER BY date DESC")
    fun observeAll(): Flow<List<DailyReflectionEntity>>

    @Query("SELECT * FROM daily_reflections ORDER BY date DESC")
    suspend fun getAll(): List<DailyReflectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(reflection: DailyReflectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reflections: List<DailyReflectionEntity>)
}
