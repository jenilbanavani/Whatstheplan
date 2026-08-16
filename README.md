# What's the Plan? ☀️

<div align="center">

[![Status](https://img.shields.io/badge/Status-Testing%20Phase-orange.svg?style=for-the-badge)](#-we-want-your-feedback)
[![License: Proprietary](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg?style=for-the-badge)](./LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](#)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline%20%7C%20Zero%20Tracking-blueviolet.svg?style=for-the-badge)](#-privacy--offline-first)
[![Built with](https://img.shields.io/badge/Built%20with-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](#-tech-stack)

<br />

**A quiet, thoughtful digital-wellbeing companion that transforms your relationship with your phone.**

> *"Before you use your phone, decide what you're going to use it for."*

<br />

[📥 Download Latest APK (v1.0.0-beta)](#-how-to-install--test) • [💬 Share Feedback / Suggest Ideas](https://github.com/issues) • [🐛 Report a Bug](https://github.com/issues)

</div>

---

## 🌟 Why "What's the Plan?"

Most screen-time apps treat you like a child—locking apps, showing scary red warnings, or making you feel guilty. 

**What's the Plan?** takes a fundamentally different approach rooted in **intentionality and gentle self-awareness**:

1. **☀️ Morning Intention**: Before your morning starts pulling you into mindless scrolling, set a single clear intention for your day.
2. **👋 Periodic Check-In**: Quick, gentle prompts during your active hours: *"What are you doing right now?"* Answer in under 5 seconds with zero friction.
3. **🌙 Evening Reflection**: Close the loop on your day. Rate your follow-through and mood with calm simplicity.
4. **📊 Visual Insights**: Understand your patterns through elegant, local charts—not sterile corporate spreadsheets.
5. **🔒 100% Offline & Private**: Zero accounts, zero ads, zero analytics, zero external network requests. Your data never leaves your device.

---

## 📸 Key Features

<table>
  <tr>
    <td width="50%">
      <h3>☀️ Morning Planning Flow</h3>
      <ul>
        <li><b>First-Unlock Detection</b>: Prompts you naturally on your first phone interaction of the morning.</li>
        <li><b>Intention Chips</b>: Fast selection (<code>Study</code>, <code>Deep Work</code>, <code>Exercise</code>, <code>Build</code>, <code>Relax</code>).</li>
        <li><b>Calm Rewards</b>: Delightful offline fun facts after setting your plan.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>⚡ 5-Second Check-Ins</h3>
      <ul>
        <li><b>Pastel Bento Grid</b>: Rapid 1-tap logging across 8 core activity categories.</li>
        <li><b>Lockscreen Actions</b>: Log directly from your notification shade or Quick Settings tile without unlocking.</li>
        <li><b>Focus Mode</b>: Quick-pause check-ins for 30m, 1h, 2h, or 4h during deep work.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>📊 Visual Graphs & Insights</h3>
      <ul>
        <li><b>Weekly Check-Ins</b>: 7-day interactive bar graph with current day highlighted.</li>
        <li><b>Activity Breakdown</b>: Proportional color-coded category distribution.</li>
        <li><b>Plan Follow-Through Tracker</b>: 7-day capsule streak.</li>
        <li><b>Screen Time Trend</b>: 7-day on-device screen duration trend.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>📱 Home-Screen Widgets</h3>
      <ul>
        <li><b>Today's Plan Widget (2x2)</b>: Your active plan & check-in count at a glance.</li>
        <li><b>Quick Check-In Widget (2x1)</b>: 1-tap direct launcher.</li>
        <li><b>Daily Snapshot Widget (3x2)</b>: Plan, check-ins, and screen time summary.</li>
      </ul>
    </td>
  </tr>
</table>

---

## 🧪 WE WANT YOUR FEEDBACK! (Testing Phase)

We are currently in an active **Beta Testing Phase** and refining the app before our public Google Play release. Your thoughts and experiences directly shape the product!

### 🎯 What We'd Love to Hear From You:
- **Tone & Feel**: Does the app feel calm and encouraging? Or does any part feel annoying or demanding?
- **Notification Timing**: Do check-ins arrive at natural times? Does the morning prompt catch you at the right moment?
- **Speed & Usability**: Can you complete a check-in within 5 seconds?
- **Visuals & Aesthetic**: How does the dark/light theme look on your device screen?
- **Battery & Reliability**: Are check-in reminders delivered reliably on your phone brand (Samsung, Xiaomi, Pixel, OnePlus)?

### 💬 How to Share Feedback:
1. **GitHub Issues**: Open a [New Feedback Issue](https://github.com/issues/new?template=feedback.md) or [Bug Report](https://github.com/issues/new?template=bug_report.md).
2. **GitHub Discussions**: Join our discussions tab to share screenshots and ideas.

---

## 📲 How to Install & Test

### Option 1: Direct Download (APK)
1. Download the signed **[`WhatsThePlan-release.apk`](./WhatsThePlan-release.apk)** from this repository.
2. Open the file on your Android device and select **Install** *(Enable "Install from unknown sources" if prompted)*.

### Option 2: ADB (Over USB)
```bash
adb install -r WhatsThePlan-release.apk
```

---

## 🔒 Privacy & Offline-First

We believe digital-wellbeing apps shouldn't harvest your data to tell you you're using your phone too much.

```
┌──────────────────────────────────────────────────────────┐
│                   YOUR ANDROID DEVICE                    │
│                                                          │
│   [ UI (Compose) ]  ──>  [ Local Room Database ]         │
│   [ Notifications]  ──>  [ DataStore Preferences ]       │
│                                                          │
│                  ❌ NO INTERNET PERMISSION               │
│                  ❌ NO EXTERNAL SERVERS                  │
│                  ❌ NO TRACKERS / ANALYTICS              │
└──────────────────────────────────────────────────────────┘
```

- **Zero Network Permissions**: The `android.permission.INTERNET` permission is completely absent from `AndroidManifest.xml`.
- **Local SQLite Database**: All plans, check-ins, reflections, and metrics are saved on your phone.
- **Export & Backup**: Full JSON backup and restore supported locally from the Settings screen.

---

## 🛠 Tech Stack

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose (Material 3 with custom Design System)
- **Local Persistence**: Room SQLite Database + Jetpack DataStore Preferences
- **Background Scheduling**: WorkManager + AlarmManager Exact Alarms (Doze-safe)
- **Telemetry**: Android `UsageStatsManager` (100% on-device)
- **Architecture**: Clean MVVM with Repository Pattern, Kotlin Coroutines, and Reactive StateFlow

---

## 🤝 Contributing
 
We welcome community contributions, testing feedback, and suggestions! Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for details on setting up your local development environment.

---

## 📄 License & Legal Notice

Copyright © 2026 Jenil Banavani. **All Rights Reserved.**

This repository and its contents are strictly for evaluation and beta testing purposes. Unauthorized commercial use, redistribution, monetization, re-branding, or publishing to app stores is strictly prohibited. See the [LICENSE](./LICENSE) file for complete terms.

---

<div align="center">
  <sub>Built with care for intentional living. If you find this project helpful, please consider starring ⭐ the repository!</sub>
</div>
