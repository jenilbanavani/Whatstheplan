# Contributing to What's the Plan?

Thank you for your interest in contributing to **What's the Plan?**! We appreciate community involvement in testing, refining, and extending this project.

---

## 📋 Before Contributing

1. **Read the [README.md](./README.md)** to understand the application's design principles and offline-first architecture.
2. **Check [Existing Issues](https://github.com/jenilbanavani/whatstheplan/issues)** to see if your bug or feature request has already been reported.
3. **Discuss Major Changes**: For significant architectural changes, new database tables, or workflow overhauls, please open an issue for discussion first before writing extensive code.

---

## 🐛 Reporting Bugs

When submitting a bug report, please include:
- **Device Model**: (e.g., Google Pixel 8, Samsung Galaxy S23)
- **Android Version**: (e.g., Android 14, OneUI 6.0)
- **App Version / Commit**: (e.g., v1.0.0 or commit SHA)
- **Steps to Reproduce**: Clear numbered steps.
- **Expected Behavior**: What you expected to happen.
- **Actual Behavior**: What actually happened.
- **Logs / Screenshots**: Include relevant logcat snippets if available.
  > ⚠️ **Important**: Remove any sensitive personal details, file paths, or private information from logs before posting.

---

## 💡 Proposing Feature Requests

When requesting a feature, please explain:
- **The Problem**: What limitation or friction are you experiencing?
- **Proposed Solution**: How should the app handle it?
- **Why It Matters**: How does this align with the calm, offline-first mission of the app?

---

## 💻 Development & Pull Request Workflow

### 1. Fork & Clone
```bash
# Fork the repository on GitHub, then clone your fork:
git clone https://github.com/YOUR_USERNAME/whatstheplan.git
cd whatstheplan

# Add the upstream remote:
git remote add upstream https://github.com/jenilbanavani/whatstheplan.git
```

### 2. Create a Branch
```bash
git checkout -b feature/your-feature-name
```

### 3. Make Changes & Test Locally
- Follow modern Android & Jetpack Compose best practices.
- Ensure all existing unit tests pass before submitting:
  ```bash
  ./gradlew test
  ```
- Verify debug and release builds assemble cleanly:
  ```bash
  ./gradlew assembleDebug
  ```

### 4. Commit & Push
```bash
git commit -m "feat: describe your change cleanly"
git push origin feature/your-feature-name
```

### 5. Open a Pull Request
- Provide a clear summary of what was changed and why.
- Reference any related issues (e.g., `Fixes #12`).
- Keep PRs focused on a single topic (avoid mixing unrelated refactoring).
- Ensure no accidental secrets, local properties, or temporary build files are included.

---

## 🔒 Core Architecture Guidelines

All contributions must adhere to these foundational rules:
1. **100% Offline-First**: No network permissions (`android.permission.INTERNET`) may be introduced. All user data, metrics, and insights must remain strictly on-device.
2. **Calm & Non-Intrusive**: Avoid aggressive badges, guilt-inducing notifications, or coercive design patterns.
3. **Ergonomic Speed**: Daytime check-in interactions must remain fast (< 5 seconds).

---

## 📄 License Notice

By contributing to **What's the Plan?**, you agree that your contributions will be licensed under the project's [Mozilla Public License 2.0](./LICENSE).
