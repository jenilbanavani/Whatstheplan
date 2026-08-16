package com.example.whatstheplan.data.local.repository

import android.content.Context
import com.example.whatstheplan.data.local.database.dao.ActivityCount
import com.example.whatstheplan.data.local.database.dao.CheckInCount
import com.example.whatstheplan.data.local.database.dao.CheckInDao
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import com.example.whatstheplan.domain.model.ActivityType
import com.example.whatstheplan.utils.DateUtils
import com.example.whatstheplan.widgets.WidgetUpdater
import kotlinx.coroutines.flow.Flow

class CheckInRepository(
    private val dao: CheckInDao,
    private val context: Context? = null,
) {
    fun observeToday(): Flow<List<CheckInEntity>> = dao.observeByDate(DateUtils.todayString())

    fun observeByDate(date: String): Flow<List<CheckInEntity>> = dao.observeByDate(date)

    fun observeAll(): Flow<List<CheckInEntity>> = dao.observeAll()

    fun observeTodayActivityCounts(): Flow<List<ActivityCount>> =
        dao.observeActivityCounts(DateUtils.todayString())

    fun observeCountsByDate(): Flow<List<CheckInCount>> = dao.observeCountsByDate()

    suspend fun saveCheckIn(activity: ActivityType, note: String) {
        dao.insert(
            CheckInEntity(
                date = DateUtils.todayString(),
                activity = activity.name,
                note = note.trim(),
            ),
        )
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }
}
