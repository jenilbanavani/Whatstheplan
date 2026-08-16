package com.example.whatstheplan.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.whatstheplan.MainActivity
import com.example.whatstheplan.R
import com.example.whatstheplan.WhatsThePlanApplication
import com.example.whatstheplan.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TodayPlanWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val application = context.applicationContext as? WhatsThePlanApplication ?: return
        val container = application.container

        CoroutineScope(Dispatchers.IO).launch {
            val todayStr = DateUtils.todayString()
            val plan = container.dailyPlanRepository.observeToday().firstOrNull()
            val checkIns = container.checkInRepository.observeToday().firstOrNull().orEmpty()

            val planText = when {
                plan == null -> "No plan set yet. Tap to set your plan."
                plan.skipped -> "Taking today as it comes."
                else -> plan.text
            }
            val checkInCountText = "✓ ${checkIns.size} check-in${if (checkIns.size == 1) "" else "s"}"

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_today_plan)
                views.setTextViewText(R.id.widget_plan_text, planText)
                views.setTextViewText(R.id.widget_checkin_count, checkInCountText)

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TodayPlanWidgetReceiver::class.java),
            )
            if (ids.isNotEmpty()) {
                val intent = Intent(context, TodayPlanWidgetReceiver::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
