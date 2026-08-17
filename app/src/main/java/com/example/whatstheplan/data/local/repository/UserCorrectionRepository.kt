package com.example.whatstheplan.data.local.repository

import com.example.whatstheplan.data.local.database.dao.UserCorrectionDao
import com.example.whatstheplan.data.local.database.entities.UserCorrectionEntity
import kotlinx.coroutines.flow.Flow

class UserCorrectionRepository(private val dao: UserCorrectionDao) {
    fun observeAll(): Flow<List<UserCorrectionEntity>> = dao.observeAll()

    suspend fun getAll(): List<UserCorrectionEntity> = dao.getAll()

    suspend fun addCorrection(
        category: String,
        note: String,
        source: String = "USER",
    ): Long = dao.insert(
        UserCorrectionEntity(
            category = category,
            note = note.trim(),
            source = source,
        ),
    )

    suspend fun updateCorrection(correction: UserCorrectionEntity) =
        dao.update(correction)

    suspend fun deleteCorrection(id: Long) =
        dao.deleteById(id)

    suspend fun deleteAll() =
        dao.deleteAll()
}
