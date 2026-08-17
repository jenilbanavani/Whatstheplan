# What's the Plan?

A quiet, slightly playful, **local-first Android phone companion** that remembers your daily rhythm and helps you choose and follow through on **one realistic intention** without becoming a task manager or productivity dashboard.

> *"Low friction > many features. Before your day gets pulled in every direction, decide on your one thing."*

---

## 📥 Download Latest Release APK

You can download and test the latest signed release APK directly:

* **[⬇️ Download WhatsThePlan-release.apk (Latest v0.2 Release)](./WhatsThePlan-release.apk)**
* **[📦 GitHub Releases Page](https://github.com/jenilbanavani/whatstheplan/releases/latest)**

*Note: The APK is pre-built, signed, and ready to install on any Android device running Android 8.0 (API 26) or higher.*

---

## 🌟 Core Product Direction (v0.2)

Most productivity apps turn into overwhelming chore lists: endless subtasks, streaks, pushy hourly notifications, and guilt-inducing red badges.

**What's the Plan?** is built around one simple, calm loop:

1. **1-Minute Onboarding**: Set your usual wake/sleep rhythm, one fixed daily commitment, and choose your preferred companion tone (**Calm**, **Playful**, or **Direct**).
2. **Morning Intention**: Choose or type your single most important intention for today, along with its **smallest first step** (or pick *"Nothing ambitious today"* on low-energy days).
3. **Main Screen Focus**:
   * Displays today's intention and smallest start.
   * **Primary Action**: `[Start 10 min]` (triggers a focused 10-minute countdown burst).
   * **Secondary Actions**: `[Move it]` and `[Not today]` (with zero-guilt confirmations).
   * **1-Tap Pause**: `[Pause notifications today]` whenever you need uninterrupted focus or rest.
4. **Calm Notifications (Max 2 Prompts/Day)**:
   * **Max 1 morning reminder** at wake time (if intention isn't set).
   * **Max 1 context-aware follow-up** (*"Still realistic?"*) with 3 interactive notification actions: `[⏱️ Start 10 min]`, `[➡️ Move it]`, `[🛑 Not today]`.
   * **Zero hourly nagging**.
5. **Neutral Evening Recovery**:
   * Reviews the day with low-guilt, constructive recovery options: `Done`, `Move to tomorrow`, `Make it smaller`, or `Drop it`.
6. **Transparent Local Memory Inspector**:
   * View everything the companion knows about you.
   * Clearly distinguishes **Added by you** (rhythm, wake/sleep time, fixed commitments, custom notes) from **Learned from your activity** (logged actions, timing feedback).
   * Complete control: `[Edit]`, `[Forget this]`, `[Export data (JSON)]`, and `[Delete all memory]`.

---

## 🎨 Design & Personality

* **Visual Style**: Clean Material 3 design, luminous lavender & deep indigo dark mode, warm off-white light mode, high-contrast readable typography, and large touch targets.
* **Tones Supported**:
  * **Calm**: Gentle, supportive, and unhurried.
  * **Playful**: Light-hearted, curious, and friendly.
  * **Direct**: Clear, concise, and straight to the point.
* **What We Intentionally Avoid**:
  * ❌ No cloud AI or chatbots
  * ❌ No streaks or guilt-based progress mechanics
  * ❌ No analytics dashboards or productivity scores
  * ❌ No social feeds or accounts
  * ❌ No location tracking or continuous screen monitoring

---

## 🔒 100% Offline & Privacy First

Privacy is not an add-on; it is our core architecture:

* **Zero Internet Permissions**: `android.permission.INTERNET` is **NOT** present in `AndroidManifest.xml`. The app cannot connect to the internet, send telemetry, or transmit data off your device.
* **On-Device Storage**: Stored locally in SQLite via Android Room (`whatstheplan.db`) and encrypted preferences via Jetpack DataStore (`user_settings`).
* **Notification Permission**: Requested only when you explicitly enable daily prompts during onboarding or in Settings.
* **Data Portability**: Export your complete memory database to JSON at any time.

---

## 🛠️ Tech Stack

* **Language**: Kotlin (100%)
* **UI**: Jetpack Compose with Material 3
* **Local Storage**: Room SQLite Database (Version 3 with automated migrations)
* **Preferences**: Jetpack DataStore Preferences
* **Background Scheduling**: Android WorkManager & AlarmManager
* **Architecture**: Clean Architecture / Repository pattern with Manual Dependency Injection (`AppContainer`)
* **Target Platforms**: Android 8.0 (API 26) through Android 15 (API 35)

---

## 💻 Development & Build Setup

### Prerequisites
* **Android Studio**: Ladybug / Koala (2024.1+) or newer
* **Java Development Kit (JDK)**: JDK 17
* **Android SDK**: `compileSdk 35`, `minSdk 26`

### Building from Source

```bash
# Clone the repository
git clone https://github.com/jenilbanavani/whatstheplan.git
cd whatstheplan

# Run unit tests
./gradlew test

# Assemble signed release APK
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Install via ADB
```bash
adb install -r WhatsThePlan-release.apk
```

---

## 📂 Project Structure

```
WhatsThePlan/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Permissions & receiver configurations (No internet permission)
│   │   ├── java/com/example/whatstheplan/
│   │   │   ├── MainActivity.kt          # Single-activity navigation host
│   │   │   ├── WhatsThePlanApplication.kt # App lifecycle & channel init
│   │   │   ├── AppContainer.kt          # Dependency injection container
│   │   │   ├── data/local/              # Room database, DAOs, repositories, and BackupManager
│   │   │   ├── domain/model/            # TonePreference, UserSettings, ThemeMode
│   │   │   ├── notifications/           # CheckInWorker, MorningReminderWorker, EveningReflectionWorker, CheckInActionReceiver
│   │   │   ├── ui/
│   │   │   │   ├── screens/             # TodayScreen, SetupScreen, MorningScreen, ReflectionScreen, MemoryScreen, SettingsScreen
│   │   │   │   ├── components/          # Buttons, PillBadges, SectionCards
│   │   │   │   └── theme/               # Material 3 colors, typography, shapes
│   │   │   └── utils/                   # DateUtils & clock formatters
│   │   └── res/                         # Vector drawables, colors, strings
│   └── src/test/                        # Unit test suite (TonePreferenceTest, V02SchedulerTest, etc.)
├── WhatsThePlan-release.apk             # Latest signed release binary
├── README.md                            # Project overview & documentation
├── CONTRIBUTING.md                      # Contribution guidelines
├── SECURITY.md                          # Security & vulnerability reporting policy
└── LICENSE                              # Mozilla Public License 2.0 (MPL-2.0)
```

---

## 🤝 Feedback & Contributing

We are actively testing and gathering feedback on the v0.2 experience:
* Did the morning intention help you clarify your focus?
* Was the single follow-up prompt well-timed or intrusive?
* What tone did you find most natural?

Open an issue in the [GitHub Issues](https://github.com/jenilbanavani/whatstheplan/issues) tab to share your feedback or contribute pull requests.

---

## 📜 License

Licensed under the **Mozilla Public License 2.0 (MPL-2.0)**. See the [LICENSE](./LICENSE) file for terms.
