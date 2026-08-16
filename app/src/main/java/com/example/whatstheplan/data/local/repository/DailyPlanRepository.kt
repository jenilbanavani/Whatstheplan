package com.example.whatstheplan.data.local.repository

import android.content.Context
import com.example.whatstheplan.data.local.database.dao.DailyPlanDao
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.utils.DateUtils
import com.example.whatstheplan.widgets.WidgetUpdater
import kotlinx.coroutines.flow.Flow

class DailyPlanRepository(
    private val dao: DailyPlanDao,
    private val context: Context? = null,
) {
    fun observeToday(): Flow<DailyPlanEntity?> = observeByDate(DateUtils.todayString())

    fun observeByDate(date: String): Flow<DailyPlanEntity?> = dao.observeByDate(date)

    fun observeAll(): Flow<List<DailyPlanEntity>> = dao.observeAll()

    suspend fun savePlan(text: String, skipped: Boolean = false, date: String = DateUtils.todayString()) {
        dao.save(
            DailyPlanEntity(
                date = date,
                text = text.trim(),
                skipped = skipped,
            ),
        )
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }
}
