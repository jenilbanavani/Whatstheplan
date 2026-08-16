# What's the Plan? — Project Status Report

**Generated Date:** August 16, 2026  
**Application:** What's the Plan? (Android Native / Jetpack Compose)  
**Package:** `com.example.whatstheplan`  

---

## 1. Executive Summary

**"What's the Plan?"** is an offline-first, privacy-focused Android digital wellbeing application built with Kotlin and Jetpack Compose. It encourages intentional phone usage through daily morning planning, periodic daytime check-ins, evening reflections, optional on-device screen-time insights, and offline fun-fact micro-rewards.

All primary core user flows (Setup, Morning Planning, Today Dashboard, Check-In, Reflection, History, Settings, and Privacy) are implemented with Room and DataStore persistence.

---

## 2. What Has Been Completed (Done)

### 📱 User Interface & Flows (Jetpack Compose)
1. **Onboarding / Setup Flow ([`SetupScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/SetupScreen.kt)):**
   - Welcomes the user with the privacy-first proposition (no account, 100% offline).
   - Requests Android 13+ `POST_NOTIFICATIONS` runtime permission.
   - Provides launcher action for `PACKAGE_USAGE_STATS` (Usage Access settings).
   - Persists setup completion state and guides user directly into Morning Planning.

2. **Morning Planning ([`MorningScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/MorningScreen.kt)):**
   - Quick-intent filter chips (*Study, Work, Exercise, Build something, Relax, Other*).
   - Freeform multiline text input for the day's core plan.
   - Skip option for low-friction planning.
   - Interstitial offline fun-fact reward screen on submission.

3. **Today Dashboard ([`TodayScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/TodayScreen.kt)):**
   - Displays friendly formatted date and current plan summary.
   - Real-time count and categorical breakdown of today's check-ins.
   - On-device screen time summary (top 5 used apps, category labels, total duration).
   - Evening reflection summary status with dynamic reflection button.
   - Dynamic encouraging message based on check-in frequency.

4. **Periodic Check-In Flow ([`CheckInScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/CheckInScreen.kt)):**
   - Quick selection across 8 activity categories (*Productive, Social Media, Entertainment, Work, Study, Chore, Break, Other*).
   - Optional contextual note input.
   - Interstitial fun-fact reward upon check-in completion.

5. **Evening Reflection ([`ReflectionScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/ReflectionScreen.kt)):**
   - Mood tracker (*Great, Pretty good, Average, Messy*).
   - Plan fulfillment tracker (*Yes, Partially, No*).
   - Freeform note for blockers and takeaways.

6. **History & Insights ([`HistoryScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/HistoryScreen.kt)):**
   - Reverse-chronological timeline of past days.
   - Daily cards aggregating saved plan, total check-in count, and reflection completion status.

7. **Settings & Privacy Management ([`SettingsScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/SettingsScreen.kt), [`PrivacyScreen.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/ui/screens/PrivacyScreen.kt)):**
   - Reminder toggles: Morning reminder toggle, hourly check-in toggle, interval stepper (in 15m increments), active hours window (start/end in 30m steps), notification sound toggle.
   - App customization: Fun facts toggle and theme selector (System, Light, Dark).
   - Screen-time permissions and toggle.
   - Database reset dialog clearing Room tables and DataStore preferences.
   - Dedicated offline transparency screen.

---

### ⚙️ Architecture & Background Services
1. **Local Persistence (Room + DataStore):**
   - Room Database ([`WhatsThePlanDatabase.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/data/local/database/WhatsThePlanDatabase.kt)) with entities and DAOs for daily plans, check-ins, and reflections.
   - DataStore repository ([`SettingsRepository.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/data/local/repository/SettingsRepository.kt)) handling user preferences and recent fun-fact tracking.
   - Clean Dependency Container ([`AppContainer.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/AppContainer.kt)).

2. **Background Work & Notifications:**
   - WorkManager periodic job scheduling ([`CheckInScheduler.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/notifications/CheckInScheduler.kt)) calculating active window intervals (including overnight windows).
   - Coroutine worker ([`CheckInWorker.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/notifications/CheckInWorker.kt)).
   - Notification channel helper ([`NotificationHelper.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/notifications/NotificationHelper.kt)) with deep-link intent routing directly to the check-in screen.

3. **OS Usage Statistics Reader ([`UsageStatsReader.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/usage/UsageStatsReader.kt)):**
   - Queries Android `UsageStatsManager` for today's foreground package times.
   - Resolves human-readable application labels and categories.

4. **Offline Fun-Facts Engine ([`FunFactDataSource.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/data/local/FunFactDataSource.kt), [`FunFactRepository.kt`](file:///c:/Users/banav/Documents/ChatGPT/Whats/app/src/main/java/com/example/whatstheplan/data/local/repository/FunFactRepository.kt)):**
   - 110 curated facts across 11 categories (Space, Science, Tech, Animals, Psychology, History, Geography, Human body, Weird facts, Nature, Food, Math).
   - Recency de-duplication queue (prevents repeating the last 20 facts).

---

## 3. What Is Pending / Incomplete

### ⚠️ Build & Project Infrastructure
1. **Missing Gradle Wrapper:**
   - Root project has `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties`, but is missing `gradlew`, `gradlew.bat`, and `gradle/wrapper/` (`gradle-wrapper.jar` and `gradle-wrapper.properties`).
   - *Impact:* Cannot run `./gradlew assembleDebug` or automated CI builds without an existing local Gradle installation.

### 🧩 Unused / Incomplete Database Schemas
2. **Unused `FunFactEntity` & `FunFactDao`:**
   - Declared in Room database, but facts are stored in static `FunFactDataSource` and tracked via DataStore.
3. **Unused `ScreenTimeSnapshotEntity` & `ScreenTimeDao`:**
   - Declared in Room database, but screen-time is only queried dynamically for "today" from Android `UsageStatsManager` and never persisted for historical review.

### 🔔 Missing Notification Schedulers
4. **Morning Planning Notification Worker:**
   - The settings toggle `morningReminderEnabled` exists in `UserSettings` and `SettingsScreen`, but there is currently no scheduler or worker to trigger a morning notification reminding the user to plan their day.
5. **Evening Reflection Notification:**
   - No optional evening reminder notification is scheduled to prompt the user to complete their evening reflection.

### 📊 History & Detail Expansion
6. **Detailed Check-In & Screen Time History:**
   - `HistoryScreen` only displays aggregate check-in counts. Users cannot expand past days to view individual check-in timestamps, notes, or past screen-time usage.

### 🧪 Quality Assurance & Assets
7. **Automated Unit & UI Tests:**
   - No unit tests in `src/test` (for `DateUtils`, `CheckInScheduler`, `SettingsRepository`, DAOs).
   - No Compose UI instrumentation tests in `src/androidTest`.
8. **Data Export / Backup:**
   - Because data is stored strictly locally, there is currently no JSON/CSV export or import feature for users migrating phones or backing up logs.

---

## 4. Implementation Status

All roadmap items have been implemented:

| Status | Feature / Task | Details |
|---|---|---|
| ✅ **Done** | **Focus Mode (Pause Check-Ins)** | Added pause chips (30m, 1h, 2h, 4h), active status banner, and worker suppression. |
| ✅ **Done** | **Morning & Evening Schedulers** | `MorningReminderWorker` & `EveningReflectionWorker` scheduled via WorkManager. |
| ✅ **Done** | **Screen Time Persistence** | `ScreenTimeRepository` persisting daily snapshots to Room for historical tracking. |
| ✅ **Done** | **History Detail & Patterns** | Expandable day cards with check-in logs, notes, and 7-day observational pattern card. |
| ✅ **Done** | **Local Offline Backup / Restore** | `BackupManager` with SAF JSON export and import in Settings. |
| ✅ **Done** | **Gradle Wrapper** | `gradle-wrapper.properties`, `gradlew`, and `gradlew.bat` added. |
| ✅ **Done** | **Unit Tests** | Tests for `DateUtils` and active window scheduling in `app/src/test`. |
