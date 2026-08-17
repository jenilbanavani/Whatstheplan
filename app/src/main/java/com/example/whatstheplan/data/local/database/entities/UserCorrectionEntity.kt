package com.example.whatstheplan.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_corrections")
data class UserCorrectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "RHYTHM", "COMMITMENT", "PREFERENCE", "INTERACTION", "FEEDBACK"
    val note: String,
    val source: String = "USER", // "USER" (Added by you) or "LEARNED" (Learned from activity)
    val timestamp: Long = System.currentTimeMillis(),
)
