package com.example.whatstheplan.data.local.repository

import android.content.Context
import com.example.whatstheplan.data.local.database.dao.DailyReflectionDao
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.utils.DateUtils
import com.example.whatstheplan.widgets.WidgetUpdater
import kotlinx.coroutines.flow.Flow

class DailyReflectionRepository(
    private val dao: DailyReflectionDao,
    private val context: Context? = null,
) {
    fun observeToday(): Flow<DailyReflectionEntity?> = dao.observeByDate(DateUtils.todayString())

    fun observeAll(): Flow<List<DailyReflectionEntity>> = dao.observeAll()

    suspend fun saveReflection(mood: String, completion: String, note: String) {
        dao.save(
            DailyReflectionEntity(
                date = DateUtils.todayString(),
                mood = mood,
                completion = completion,
                note = note.trim(),
            ),
        )
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }
}
