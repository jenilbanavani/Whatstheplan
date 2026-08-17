package com.example.whatstheplan.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyPlanEntity?>

    @Query("SELECT * FROM daily_plans WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyPlanEntity?

    @Query("SELECT * FROM daily_plans ORDER BY date DESC")
    fun observeAll(): Flow<List<DailyPlanEntity>>

    @Query("SELECT * FROM daily_plans ORDER BY date DESC")
    suspend fun getAll(): List<DailyPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(plan: DailyPlanEntity)

    @Update
    suspend fun update(plan: DailyPlanEntity)

    @Query("DELETE FROM daily_plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<DailyPlanEntity>)
}
