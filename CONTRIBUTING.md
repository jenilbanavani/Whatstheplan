# Contributing to What's the Plan? ☀️

Thank you for your interest in helping improve **What's the Plan?**! We are currently in our **Beta Testing Phase** and are actively looking for feedback, testing on different Android devices, and code contributions.

---

## 🧪 How You Can Help

### 1. Test the App & Provide Feedback
- Install the latest [`WhatsThePlan-release.apk`](./WhatsThePlan-release.apk) on your personal device.
- Use it for a few days during your daily routine.
- Submit feedback via [GitHub Issues](https://github.com/issues) using the Feedback template.

### 2. Report Bugs
If you run into any crashes, missing notifications on specific OEM skins (MIUI, OneUI, ColorOS), or layout glitches, please file a [Bug Report](https://github.com/issues).

---

## 💻 Local Development Setup

### Prerequisites
- **Android Studio** (Koala / Ladybug or newer)
- **JDK 17**
- **Android SDK Platform 35** and **Build-Tools 34+**

### Building from Source
```bash
# Clone the repository
git clone https://github.com/your-username/WhatsThePlan.git
cd WhatsThePlan

# Run unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug

# Assemble signed release APK
./gradlew assembleRelease
```

---

## 🔒 Core Architecture Rules

When contributing to this project, please adhere to our core product principles:
1. **100% Offline-First**: Zero external networking, analytics, or cloud dependencies. The app must never require `android.permission.INTERNET`.
2. **Intentional & Calm**: Avoid aggressive nag screens, intrusive badges, or guilt-inducing copy.
3. **Speed & Ergonomics**: Check-ins must remain sub-5-second interactions with minimal user friction.

---

## 📄 License & Proprietary Rights
 
All code, assets, and documentation are proprietary and copyrighted by Jenil Banavani. All Rights Reserved. See [LICENSE](./LICENSE).
