package com.example.whatstheplan.data.local.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import kotlinx.coroutines.flow.Flow

data class ActivityCount(
    @ColumnInfo(name = "activity") val activity: String,
    @ColumnInfo(name = "total") val total: Int,
)

data class CheckInCount(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "total") val total: Int,
)

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE date = :date ORDER BY timestamp DESC")
    fun observeByDate(date: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CheckInEntity>>

    @Query("SELECT activity, COUNT(*) AS total FROM check_ins WHERE date = :date GROUP BY activity ORDER BY total DESC")
    fun observeActivityCounts(date: String): Flow<List<ActivityCount>>

    @Query("SELECT date, COUNT(*) AS total FROM check_ins GROUP BY date ORDER BY date DESC")
    fun observeCountsByDate(): Flow<List<CheckInCount>>

    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC")
    suspend fun getAll(): List<CheckInEntity>

    @Insert
    suspend fun insert(checkIn: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkIns: List<CheckInEntity>)
}
