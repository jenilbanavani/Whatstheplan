package com.example.whatstheplan.widgets

import android.content.Context

object WidgetUpdater {
    fun updateAllWidgets(context: Context) {
        TodayPlanWidgetReceiver.updateAll(context)
        QuickCheckInWidgetReceiver.updateAll(context)
        DailySnapshotWidgetReceiver.updateAll(context)
    }
}
