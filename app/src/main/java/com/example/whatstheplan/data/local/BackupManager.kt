package com.example.whatstheplan.data.local

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.data.local.database.entities.CheckInEntity
import com.example.whatstheplan.data.local.database.entities.DailyPlanEntity
import com.example.whatstheplan.data.local.database.entities.DailyReflectionEntity
import com.example.whatstheplan.data.local.database.entities.ScreenTimeSnapshotEntity
import com.example.whatstheplan.data.local.database.entities.UserCorrectionEntity
import com.example.whatstheplan.domain.model.ThemeMode
import com.example.whatstheplan.domain.model.TonePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    suspend fun exportToJson(container: AppContainer): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 3)
        root.put("exported_at", System.currentTimeMillis())

        // 1. Daily Plans
        val plans = container.database.dailyPlanDao().getAll()
        val plansArray = JSONArray()
        for (plan in plans) {
            val obj = JSONObject().apply {
                put("date", plan.date)
                put("text", plan.text)
                put("firstStep", plan.firstStep)
                put("status", plan.status)
                put("createdAt", plan.createdAt)
                put("skipped", plan.skipped)
            }
            plansArray.put(obj)
        }
        root.put("plans", plansArray)

        // 2. User Corrections / Memory
        val corrections = container.database.userCorrectionDao().getAll()
        val correctionsArray = JSONArray()
        for (c in corrections) {
            val obj = JSONObject().apply {
                put("category", c.category)
                put("note", c.note)
                put("source", c.source)
                put("timestamp", c.timestamp)
            }
            correctionsArray.put(obj)
        }
        root.put("corrections", correctionsArray)

        // 3. Check-Ins
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

        // 4. Reflections
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

        // 5. Settings
        val settings = container.settingsRepository.settingsFlow.first()
        val settingsObj = JSONObject().apply {
            put("userName", settings.userName)
            put("wakeTimeMinutes", settings.wakeTimeMinutes)
            put("sleepTimeMinutes", settings.sleepTimeMinutes)
            put("dailyCommitment", settings.dailyCommitment)
            put("tonePreference", settings.tonePreference.name)
            put("morningReminderEnabled", settings.morningReminderEnabled)
            put("followUpEnabled", settings.followUpEnabled)
            put("eveningReflectionEnabled", settings.eveningReflectionEnabled)
            put("quietHoursStartMinutes", settings.quietHoursStartMinutes)
            put("quietHoursEndMinutes", settings.quietHoursEndMinutes)
            put("notificationFeedback", settings.notificationFeedback)
            put("themeMode", settings.themeMode.name)
            put("notificationSound", settings.notificationSound)
        }
        root.put("settings", settingsObj)

        root.toString(2)
    }

    suspend fun importFromJson(container: AppContainer, jsonString: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val root = JSONObject(jsonString)

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
                                firstStep = obj.optString("firstStep", ""),
                                status = obj.optString("status", "ACTIVE"),
                                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                                skipped = obj.optBoolean("skipped", false),
                            ),
                        )
                    }
                    if (plans.isNotEmpty()) {
                        container.database.dailyPlanDao().insertAll(plans)
                    }
                }

                // 2. Corrections / Memory
                if (root.has("corrections")) {
                    val arr = root.getJSONArray("corrections")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        container.database.userCorrectionDao().insert(
                            UserCorrectionEntity(
                                category = obj.optString("category", "PREFERENCE"),
                                note = obj.getString("note"),
                                source = obj.optString("source", "USER"),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            ),
                        )
                    }
                }

                // 3. Check-Ins
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
                    }
                }

                // 4. Reflections
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
                    }
                }

                // 5. Settings
                if (root.has("settings")) {
                    val s = root.getJSONObject("settings")
                    val repo = container.settingsRepository
                    if (s.has("userName")) repo.setUserName(s.getString("userName"))
                    if (s.has("wakeTimeMinutes")) repo.setWakeTimeMinutes(s.getInt("wakeTimeMinutes"))
                    if (s.has("sleepTimeMinutes")) repo.setSleepTimeMinutes(s.getInt("sleepTimeMinutes"))
                    if (s.has("dailyCommitment")) repo.setDailyCommitment(s.getString("dailyCommitment"))
                    if (s.has("tonePreference")) {
                        val tone = TonePreference.entries.firstOrNull { it.name == s.getString("tonePreference") } ?: TonePreference.CALM
                        repo.setTonePreference(tone)
                    }
                    if (s.has("morningReminderEnabled")) repo.setMorningReminderEnabled(s.getBoolean("morningReminderEnabled"))
                    if (s.has("followUpEnabled")) repo.setFollowUpEnabled(s.getBoolean("followUpEnabled"))
                    if (s.has("eveningReflectionEnabled")) repo.setEveningReflectionEnabled(s.getBoolean("eveningReflectionEnabled"))
                    if (s.has("notificationFeedback")) repo.setNotificationFeedback(s.getString("notificationFeedback"))
                    if (s.has("themeMode")) {
                        val mode = ThemeMode.entries.firstOrNull { it.name == s.getString("themeMode") } ?: ThemeMode.SYSTEM
                        repo.setThemeMode(mode)
                    }
                }

                true
            }.getOrDefault(false)
        }

    fun exportToDownloads(context: Context, jsonContent: String): Boolean {
        return runCatching {
            val filename = "whats_the_plan_memory_${System.currentTimeMillis()}.json"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(jsonContent.toByteArray())
                    }
                    true
                } else false
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { it.write(jsonContent.toByteArray()) }
                true
            }
        }.getOrDefault(false)
    }
}
