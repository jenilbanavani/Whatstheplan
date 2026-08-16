package com.example.whatstheplan

import android.content.Context
import com.example.whatstheplan.data.local.database.WhatsThePlanDatabase
import com.example.whatstheplan.data.local.repository.CheckInRepository
import com.example.whatstheplan.data.local.repository.DailyPlanRepository
import com.example.whatstheplan.data.local.repository.DailyReflectionRepository
import com.example.whatstheplan.data.local.repository.FunFactRepository
import com.example.whatstheplan.data.local.repository.ScreenTimeRepository
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.usage.UsageStatsReader
import com.example.whatstheplan.widgets.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: WhatsThePlanDatabase = WhatsThePlanDatabase.getInstance(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val dailyPlanRepository = DailyPlanRepository(database.dailyPlanDao(), appContext)
    val checkInRepository = CheckInRepository(database.checkInDao(), appContext)
    val dailyReflectionRepository = DailyReflectionRepository(database.dailyReflectionDao(), appContext)
    val funFactRepository = FunFactRepository(settingsRepository)
    val screenTimeRepository = ScreenTimeRepository(database.screenTimeDao(), appContext)
    val usageStatsReader = UsageStatsReader(appContext)

    suspend fun resetAllLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            settingsRepository.resetToDefaults()
            WidgetUpdater.updateAllWidgets(appContext)
        }
    }
}
