package com.example.whatstheplan

import android.app.Application
import com.example.whatstheplan.notifications.AlarmScheduler
import com.example.whatstheplan.notifications.CheckInScheduler
import com.example.whatstheplan.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WhatsThePlanApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannels(this)

        appScope.launch {
            val settings = container.settingsRepository.settingsFlow.first()
            CheckInScheduler.schedule(this@WhatsThePlanApplication, settings)
            if (settings.exactAlarmsEnabled) {
                AlarmScheduler.scheduleNextExactCheckIn(this@WhatsThePlanApplication, settings)
            }
        }
    }
}
