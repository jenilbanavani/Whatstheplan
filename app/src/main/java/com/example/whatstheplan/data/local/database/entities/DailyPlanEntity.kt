package com.example.whatstheplan.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_plans",
    indices = [Index(value = ["date"], unique = true)],
)
data class DailyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val text: String,
    val skipped: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
