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

    suspend fun getTodayPlan(): DailyPlanEntity? = dao.getByDate(DateUtils.todayString())

    fun observeAll(): Flow<List<DailyPlanEntity>> = dao.observeAll()

    suspend fun savePlan(
        text: String,
        firstStep: String = "",
        skipped: Boolean = false,
        date: String = DateUtils.todayString(),
    ) {
        val existing = dao.getByDate(date)
        val plan = existing?.copy(
            text = text.trim(),
            firstStep = firstStep.trim(),
            skipped = skipped,
            status = if (skipped) "DROPPED" else "ACTIVE",
        ) ?: DailyPlanEntity(
            date = date,
            text = text.trim(),
            firstStep = firstStep.trim(),
            status = if (skipped) "DROPPED" else "ACTIVE",
            skipped = skipped,
        )
        dao.save(plan)
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }

    suspend fun updateStatus(status: String, date: String = DateUtils.todayString()) {
        val plan = dao.getByDate(date) ?: return
        dao.update(plan.copy(status = status))
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }

    suspend fun startTimer(date: String = DateUtils.todayString()) {
        val plan = dao.getByDate(date) ?: return
        dao.update(plan.copy(status = "IN_PROGRESS", startedAt = System.currentTimeMillis()))
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }

    suspend fun updatePlan(plan: DailyPlanEntity) {
        dao.update(plan)
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }

    suspend fun deletePlan(id: Long) {
        dao.deleteById(id)
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }
}
