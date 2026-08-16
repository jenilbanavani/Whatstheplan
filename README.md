# What's the Plan?

A quiet, offline-first Android digital-wellbeing companion designed to help you stay aware of what you intend to do during the day before phone use becomes unconscious.

> *"Before you use your phone, decide what you're going to use it for."*

---

## Why This Exists

Most screen-time and wellbeing applications focus on restriction: blocking apps, setting hard time limits, and sending negative alerts after hours of mindless scrolling have already happened.

**What's the Plan?** approaches digital wellbeing through **proactive awareness rather than punishment**:

- **Decide First**: Set a brief intention for the day before phone notifications pull your attention in a dozen directions.
- **Gentle Check-Ins**: Periodic, sub-5-second prompts ask *"What are you doing right now?"* to foster self-awareness without judgment.
- **Reflect & Close the Loop**: Evening reflection lets you rate your follow-through and mood in a few taps.
- **100% Private**: Your daily thoughts, activities, and screen habits stay on your personal device.

---

## Features

### Current Features (Implemented & Functional)
- **☀️ Morning Planning Flow**: Set a daily intention with custom text or quick intention chips (`Study`, `Deep Work`, `Exercise`, `Build`, `Relax`).
- **☀️ First-Unlock Detection**: Detects your first morning phone unlock (`ACTION_USER_PRESENT`) to prompt intention setting at the right moment.
- **⚡ 5-Second Activity Check-Ins**: Fast 2-column pastel bento grid to log activities (`Study`, `Work`, `Building`, `Scrolling`, `Gaming`, `Watching`, `Break`, `Outside`, `Other`) with optional quick notes.
- **⚡ Notification Actions**: Log activities directly from the notification shade without unlocking or opening the app.
- **⚡ Android Quick Settings Tile**: "Check In" tile accessible from the status bar pull-down.
- **🌙 Evening Reflection**: End-of-day reflection recording mood, plan follow-through status (`Yes`, `Partially`, `No`), and notes.
- **📊 Visual Insights & Charts**:
  - 7-day interactive **Weekly Check-Ins** bar chart.
  - Proportional **Activity Breakdown** distribution bar.
  - 7-day **Plan Follow-Through** streak capsule tracker.
  - 7-day **Screen Time Trend** chart (on-device).
- **📖 History Journal**: Chronological visual feed of past days with expandable logs, timestamps, and notes.
- **📱 Home-Screen Widgets**:
  - *Today's Plan Widget (2x2)*: Active plan and check-in count.
  - *Quick Check-In Widget (2x1)*: One-tap check-in launcher.
  - *Daily Snapshot Widget (3x2)*: Plan summary, status, check-in count, and screen time.
- **🔕 Focus Mode**: Quick-pause check-in reminders for 30m, 1h, 2h, or 4h during deep work.
- **⏱️ Flexible Background Scheduling**:
  - Battery-friendly WorkManager periodic reminders.
  - Strict / Exact alarms option via `AlarmManager.setExactAndAllowWhileIdle()`.
- **💡 Offline Fun Facts**: Delightful educational trivia displayed after logging plans or check-ins (with repetition prevention).
- **🎨 Modern Design System**: Deep indigo & luminous lavender dark theme, warm off-white light theme, and pastel category accents.
- **💾 Local Data Backup & Restore**: Export and import complete offline database state via JSON files.

### Experimental / In-Development Features
- **Strict Alarm Mode**: Android 12+ exact alarm scheduling fine-tuning for aggressive OEM battery savers.
- **Dynamic Lockscreen Tile Integration**: Testing quick tile behavior across various Android OEM skins (OneUI, MIUI, OxygenOS).

### Planned Features
- [ ] Multi-week historical trend analysis (30-day and 90-day views).
- [ ] Customizable activity categories and emoji tags.
- [ ] Material You dynamic color theming (Android 12+ monet palette support).
- [ ] Configurable evening reflection notification sound and schedule windows.

---

## Screenshots / Demo

<!-- Add screenshots here -->
| Home / Today Dashboard | Morning Intention Flow | Quick Check-In | Visual Insights |
|:---:|:---:|:---:|:---:|
| *<!-- Screenshot 1 Placeholder -->* | *<!-- Screenshot 2 Placeholder -->* | *<!-- Screenshot 3 Placeholder -->* | *<!-- Screenshot 4 Placeholder -->* |

---

## Download

Pre-built signed release APKs are provided via GitHub Releases:

> **[Download the latest APK](../../releases/latest)**

*Note: Only install APK binaries published officially under this repository's Releases page.*

---

## Permissions & Privacy

Privacy is a core design requirement of this application. **The app requests zero network permissions.**

### Requested Android Permissions
| Permission | Type | Why It Is Needed | Required / Optional |
|---|---|---|---|
| `POST_NOTIFICATIONS` | Runtime | To send morning planning reminders, periodic check-in prompts, and evening reflections on Android 13+ (API 33+). | **Optional** (App functions without notifications) |
| `PACKAGE_USAGE_STATS` | Special AppOps | To calculate on-device screen time duration and top foreground apps for personal awareness charts. | **Optional** (Screen time charts hide gracefully if denied) |
| `RECEIVE_BOOT_COMPLETED` | Normal | To reschedule WorkManager background workers and restore alarm schedules after the device restarts. | **Required for background reminders** |
| `BIND_QUICK_SETTINGS_TILE` | System Service | To provide the optional "Check In" Quick Settings tile in the notification shade. | **Optional** |

### Data Storage & Network Audit
- **Internet Access**: `android.permission.INTERNET` is **NOT** present in `AndroidManifest.xml`. The app cannot connect to the internet, send telemetry, or transmit data off the device.
- **Local Storage**: Data is stored locally in SQLite using Android Room (`whatstheplan.db`) and encrypted preferences via Jetpack DataStore (`user_settings`).
- **Analytics & Third-Party SDKs**: Zero third-party analytics SDKs, trackers, crash reporters, or advertising networks are included.

### Privacy Policy
`TODO: Add a complete privacy policy before public distribution on the Google Play Store.`

---

## Development & Build Setup

### Prerequisites
- **Android Studio**: Koala (2024.1) or newer recommended
- **Java Development Kit (JDK)**: JDK 17
- **Android SDK**:
  - `compileSdk`: 35 (Android 15)
  - `targetSdk`: 35
  - `minSdk`: 26 (Android 8.0 Oreo)
  - Build-Tools: 34.0.0 or 35.0.0

### Building from Command Line
```bash
# Clone the repository
git clone https://github.com/jenilbanavani/whatstheplan.git
cd whatstheplan

# Run unit test suite
./gradlew test

# Assemble debug build
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Assemble signed release build
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Installing on Device via ADB
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## Project Structure

```
WhatsThePlan/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Permissions, components, receivers & widgets
│   │   ├── java/com/example/whatstheplan/
│   │   │   ├── MainActivity.kt          # Single-activity navigation container
│   │   │   ├── WhatsThePlanApplication.kt # Application lifecycle & scheduler init
│   │   │   ├── AppContainer.kt          # Manual Dependency Injection container
│   │   │   ├── data/local/              # Room database, entities, DAOs, repositories & backup
│   │   │   ├── domain/model/            # Domain models (UserSettings, ActivityType, FunFact)
│   │   │   ├── notifications/           # WorkManager workers, AlarmScheduler, NotificationHelper
│   │   │   ├── ui/
│   │   │   │   ├── components/          # Reusable Compose UI (Bento cards, charts, buttons)
│   │   │   │   ├── screens/             # TodayScreen, MorningScreen, CheckInScreen, HistoryScreen, etc.
│   │   │   │   └── theme/               # Color palette (Dark/Light), typography, and shapes
│   │   │   ├── usage/                   # Android UsageStatsReader
│   │   │   └── widgets/                 # Android Home-Screen Widget providers & updater
│   │   └── res/                         # XML layouts, drawables, app widget metadata, strings
│   └── src/test/                        # JVM Unit tests
├── build.gradle.kts                     # Root build configuration
└── settings.gradle.kts                  # Gradle settings & plugin repositories
```

---

## Roadmap

- [x] Core offline architecture with Room SQLite & DataStore
- [x] Morning intention setting with first-unlock detection
- [x] Sub-5-second activity check-ins with notification actions & Quick Settings tile
- [x] Evening reflection flow with mood ratings and notes
- [x] Visual charts (weekly check-ins, activity distribution, plan follow-through, screen time)
- [x] Native Android home-screen widgets (Today's Plan, Quick Check-in, Daily Snapshot)
- [x] Local JSON backup and restore
- [x] Deep indigo / luminous lavender design system with dark & light modes
- [ ] Configurable activity tags and custom categories
- [ ] Extended 30-day and 90-day historical analytics
- [ ] Google Play Store initial beta release

---

## Known Limitations

1. **OEM Battery Optimization**: On certain Android devices (Samsung OneUI, Xiaomi MIUI, OnePlus OxygenOS), aggressive background task killing may delay WorkManager check-in reminders unless battery optimizations are disabled in Settings.
2. **Force-Stop State**: If the app is force-stopped via Android system settings, the OS places the app in a stopped state, pausing WorkManager jobs until the app is opened again by the user (standard Android platform behavior).
3. **Usage Access**: Screen time statistics require the user to manually grant Usage Access in Android Settings (`Settings.ACTION_USAGE_ACCESS_SETTINGS`).

---

## Contributing

We welcome community feedback, issue reports, and pull requests! Please review our [CONTRIBUTING.md](./CONTRIBUTING.md) guide before submitting changes.

---

## Support / Contact

For bug reports, feature suggestions, or general questions, please open an issue in the [GitHub Issues](https://github.com/jenilbanavani/whatstheplan/issues) tab.

---

## License

This project is licensed under the **Mozilla Public License 2.0 (MPL-2.0)**. See the [LICENSE](./LICENSE) file for details.
