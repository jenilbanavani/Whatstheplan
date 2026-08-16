package com.example.whatstheplan.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_reflections",
    indices = [Index(value = ["date"], unique = true)],
)
data class DailyReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mood: String,
    val completion: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
