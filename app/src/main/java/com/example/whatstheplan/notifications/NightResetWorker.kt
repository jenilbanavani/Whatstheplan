package com.example.whatstheplan.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatstheplan.WhatsThePlanApplication
import com.example.whatstheplan.data.local.repository.SettingsRepository
import com.example.whatstheplan.domain.model.EngagementLevel
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.flow.first

class NightResetWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? WhatsThePlanApplication
        val container = app?.container
        val settings = container?.settingsRepository?.settingsFlow?.first()
            ?: SettingsRepository(applicationContext).settingsFlow.first()

        val todayDate = DateUtils.todayString()

        // 1. Archive open intention loops without guilt
        val todayPlan = container?.dailyPlanRepository?.observeToday()?.first()
        if (todayPlan != null && todayPlan.status != "DONE") {
            // Auto-archive open loop quietly — never resurface as "overdue"
            container.dailyPlanRepository.updateStatus("ARCHIVED")
            container.userCorrectionRepository.addCorrection(
                category = "NIGHT_RESET",
                note = "Open intention loop archived quietly at midnight without guilt",
                source = "LEARNED",
            )
        }

        // 2. Reset unlock counts and consecutive dismissals
        container?.settingsRepository?.resetUnlocks(todayDate)
        container?.settingsRepository?.resetDismissals()
        container?.settingsRepository?.setDormantUntilDate("")
        container?.settingsRepository?.setDelayMiddayUntil(0L)

        // 3. Reset Level 1 & Level 2 back to Level 0 for a fresh morning start
        if (settings.engagementLevel == EngagementLevel.LEVEL_1_DISTRACTED || settings.engagementLevel == EngagementLevel.LEVEL_2_STRESSED) {
            container?.settingsRepository?.setEngagementLevel(EngagementLevel.LEVEL_0_NORMAL)
        }

        return Result.success()
    }
}
