package com.example.whatstheplan.data.local.repository

import android.content.Context
import com.example.whatstheplan.data.local.database.dao.ScreenTimeDao
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import com.example.whatstheplan.utils.DateUtils
import com.example.whatstheplan.widgets.WidgetUpdater
import kotlinx.coroutines.flow.Flow

class ScreenTimeRepository(
    private val screenTimeDao: ScreenTimeDao,
    private val context: Context? = null,
) {
    fun observeToday(): Flow<ScreenTimeSnapshotEntity?> =
        screenTimeDao.observeByDate(DateUtils.todayString())

    fun observeByDate(date: String): Flow<ScreenTimeSnapshotEntity?> =
        screenTimeDao.observeByDate(date)

    fun observeAll(): Flow<List<ScreenTimeSnapshotEntity>> =
        screenTimeDao.observeAll()

    suspend fun saveTodaySnapshot(totalMillis: Long) {
        val today = DateUtils.todayString()
        val existing = screenTimeDao.getByDate(today)
        val entity = ScreenTimeSnapshotEntity(
            id = existing?.id ?: 0,
            date = today,
            totalMillis = totalMillis,
            capturedAt = System.currentTimeMillis(),
        )
        screenTimeDao.save(entity)
        context?.let { WidgetUpdater.updateAllWidgets(it) }
    }
}
