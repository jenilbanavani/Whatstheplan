package com.example.whatstheplan.data.local

import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import com.example.whatstheplan.domain.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    suspend fun exportToJson(container: AppContainer): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exported_at", System.currentTimeMillis())

        // 1. Daily Plans
        val plans = container.database.dailyPlanDao().getAll()
        val plansArray = JSONArray()
        for (plan in plans) {
            val obj = JSONObject().apply {
                put("date", plan.date)
                put("text", plan.text)
                put("createdAt", plan.createdAt)
                put("skipped", plan.skipped)
            }
            plansArray.put(obj)
        }
        root.put("plans", plansArray)

        // 2. Check-Ins
        val checkIns = container.database.checkInDao().getAll()
        val checkInsArray = JSONArray()
        for (checkIn in checkIns) {
            val obj = JSONObject().apply {
                put("date", checkIn.date)
                put("activity", checkIn.activity)
                put("note", checkIn.note)
                put("timestamp", checkIn.timestamp)
            }
            checkInsArray.put(obj)
        }
        root.put("check_ins", checkInsArray)

        // 3. Reflections
        val reflections = container.database.dailyReflectionDao().getAll()
        val reflectionsArray = JSONArray()
        for (reflection in reflections) {
            val obj = JSONObject().apply {
                put("date", reflection.date)
                put("mood", reflection.mood)
                put("completion", reflection.completion)
                put("note", reflection.note)
                put("createdAt", reflection.createdAt)
            }
            reflectionsArray.put(obj)
        }
        root.put("reflections", reflectionsArray)

        // 4. Screen Times
        val screenTimes = container.database.screenTimeDao().getAll()
        val screenTimesArray = JSONArray()
        for (st in screenTimes) {
            val obj = JSONObject().apply {
                put("date", st.date)
                put("totalMillis", st.totalMillis)
                put("capturedAt", st.capturedAt)
            }
            screenTimesArray.put(obj)
        }
        root.put("screen_times", screenTimesArray)

        // 5. Settings
        val settings = container.settingsRepository.settingsFlow.first()
        val settingsObj = JSONObject().apply {
            put("morningReminderEnabled", settings.morningReminderEnabled)
            put("checkInsEnabled", settings.checkInsEnabled)
            put("checkInIntervalMinutes", settings.checkInIntervalMinutes)
            put("activeStartMinutes", settings.activeStartMinutes)
            put("activeEndMinutes", settings.activeEndMinutes)
            put("funFactsEnabled", settings.funFactsEnabled)
            put("screenTimeInsightsEnabled", settings.screenTimeInsightsEnabled)
            put("themeMode", settings.themeMode.name)
            put("notificationSound", settings.notificationSound)
        }
        root.put("settings", settingsObj)

        root.toString(2)
    }

    suspend fun importFromJson(container: AppContainer, jsonString: String): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val root = JSONObject(jsonString)
                var totalImported = 0

                // 1. Plans
                if (root.has("plans")) {
                    val arr = root.getJSONArray("plans")
                    val plans = mutableListOf<DailyPlanEntity>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        plans.add(
                            DailyPlanEntity(
                                date = obj.getString("date"),
                                text = obj.getString("text"),
                                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                                skipped = obj.optBoolean("skipped", false),
                            ),
                        )
                    }
                    if (plans.isNotEmpty()) {
                        container.database.dailyPlanDao().insertAll(plans)
                        totalImported += plans.size
                    }
                }

                // 2. Check-Ins
                if (root.has("check_ins")) {
                    val arr = root.getJSONArray("check_ins")
                    val checkIns = mutableListOf<CheckInEntity>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        checkIns.add(
                            CheckInEntity(
                                date = obj.getString("date"),
                                activity = obj.getString("activity"),
                                note = obj.optString("note", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            ),
                        )
                    }
                    if (checkIns.isNotEmpty()) {
                        container.database.checkInDao().insertAll(checkIns)
                        totalImported += checkIns.size
                    }
                }

                // 3. Reflections
                if (root.has("reflections")) {
                    val arr = root.getJSONArray("reflections")
                    val reflections = mutableListOf<DailyReflectionEntity>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        reflections.add(
                            DailyReflectionEntity(
                                date = obj.getString("date"),
                                mood = obj.getString("mood"),
                                completion = obj.getString("completion"),
                                note = obj.optString("note", ""),
                                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            ),
                        )
                    }
                    if (reflections.isNotEmpty()) {
                        container.database.dailyReflectionDao().insertAll(reflections)
                        totalImported += reflections.size
                    }
                }

                // 4. Screen Times
                if (root.has("screen_times")) {
                    val arr = root.getJSONArray("screen_times")
                    val screenTimes = mutableListOf<ScreenTimeSnapshotEntity>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        screenTimes.add(
                            ScreenTimeSnapshotEntity(
                                date = obj.getString("date"),
                                totalMillis = obj.getLong("totalMillis"),
                                capturedAt = obj.optLong("capturedAt", System.currentTimeMillis()),
                            ),
                        )
                    }
                    if (screenTimes.isNotEmpty()) {
                        container.database.screenTimeDao().insertAll(screenTimes)
                        totalImported += screenTimes.size
                    }
                }

                // 5. Settings
                if (root.has("settings")) {
                    val s = root.getJSONObject("settings")
                    val repo = container.settingsRepository
                    if (s.has("morningReminderEnabled")) repo.setMorningReminderEnabled(s.getBoolean("morningReminderEnabled"))
                    if (s.has("checkInsEnabled")) repo.setCheckInsEnabled(s.getBoolean("checkInsEnabled"))
                    if (s.has("checkInIntervalMinutes")) repo.setCheckInIntervalMinutes(s.getInt("checkInIntervalMinutes"))
                    if (s.has("activeStartMinutes")) repo.setActiveStartMinutes(s.getInt("activeStartMinutes"))
                    if (s.has("activeEndMinutes")) repo.setActiveEndMinutes(s.getInt("activeEndMinutes"))
                    if (s.has("funFactsEnabled")) repo.setFunFactsEnabled(s.getBoolean("funFactsEnabled"))
                    if (s.has("screenTimeInsightsEnabled")) repo.setScreenTimeInsightsEnabled(s.getBoolean("screenTimeInsightsEnabled"))
                    if (s.has("themeMode")) {
                        val mode = ThemeMode.entries.firstOrNull { it.name == s.getString("themeMode") } ?: ThemeMode.SYSTEM
                        repo.setThemeMode(mode)
                    }
                    if (s.has("notificationSound")) repo.setNotificationSound(s.getBoolean("notificationSound"))
                }

                totalImported
            }
        }
}
