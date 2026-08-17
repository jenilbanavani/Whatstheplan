package com.example.whatstheplan.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.whatstheplan.data.local.database.entities.UserCorrectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCorrectionDao {
    @Query("SELECT * FROM user_corrections ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<UserCorrectionEntity>>

    @Query("SELECT * FROM user_corrections ORDER BY timestamp DESC")
    suspend fun getAll(): List<UserCorrectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(correction: UserCorrectionEntity): Long

    @Update
    suspend fun update(correction: UserCorrectionEntity)

    @Query("DELETE FROM user_corrections WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM user_corrections")
    suspend fun deleteAll()
}
