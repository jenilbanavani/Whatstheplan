package com.example.whatstheplan.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenTimeDao {
    @Query("SELECT * FROM screen_time_snapshots WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<ScreenTimeSnapshotEntity?>

    @Query("SELECT * FROM screen_time_snapshots WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): ScreenTimeSnapshotEntity?

    @Query("SELECT * FROM screen_time_snapshots ORDER BY date DESC")
    fun observeAll(): Flow<List<ScreenTimeSnapshotEntity>>

    @Query("SELECT * FROM screen_time_snapshots ORDER BY date DESC")
    suspend fun getAll(): List<ScreenTimeSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(snapshot: ScreenTimeSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<ScreenTimeSnapshotEntity>)

    @Query("DELETE FROM screen_time_snapshots")
    suspend fun clearAll()
}
