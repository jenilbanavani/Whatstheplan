package com.example.whatstheplan.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screen_time_snapshots",
    indices = [Index(value = ["date"], unique = true)],
)
data class ScreenTimeSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val totalMillis: Long,
    val capturedAt: Long = System.currentTimeMillis(),
)
