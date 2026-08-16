package com.example.whatstheplan.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fun_facts")
data class FunFactEntity(
    @PrimaryKey val id: Int,
    val category: String,
    val text: String,
)
