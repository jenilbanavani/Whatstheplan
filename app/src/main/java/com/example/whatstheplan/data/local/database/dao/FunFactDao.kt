package com.example.whatstheplan.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whatstheplan.data.local.database.entities.FunFactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FunFactDao {
    @Query("SELECT * FROM fun_facts ORDER BY id")
    fun observeAll(): Flow<List<FunFactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(facts: List<FunFactEntity>)
}
