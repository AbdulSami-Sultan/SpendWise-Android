# SpendWise 💳📊

**SpendWise** is a modern, privacy-first Android personal finance and daily expense tracking application built with **Jetpack Compose** and **Material Design 3**. It combines fast, fluid transaction logging with real-time visual charts to help users uncover their spending habits, stick to category budgets, and reach their financial goals.

The app supports a seamless **Dual Persistence Architecture**:
- 🛡️ **Guest Mode (Offline-First)**: Powered by local **Room SQLite** on-device storage. No account, sign-in, or internet connection required.
- ☁️ **Cloud Sync & Backup**: Powered by **Firebase Firestore** with zero-trust security rules and secure **Google Sign-In** via Android Jetpack `CredentialManager`.

---

## 📱 App Screenshots

SpendWise features dynamic light and dark theme styling following Material 3 guidelines.

### 🌓 Dark Theme vs. Light Theme Showcase

| Feature / Screen | Dark Mode | Light Mode |
| :--- | :---: | :---: |
| **Dashboard Overview**<br>Current balance, period filter chips, daily spending bar chart, and category breakdown. | <img src="screenshots/screenshot_1.png" width="320" alt="Dashboard Dark Mode" /> | <img src="screenshots/screenshot_6.png" width="320" alt="Dashboard Light Mode" /> |
| **Category Donut Chart & Analytics**<br>Interactive spending donut chart, spending habits insights, and recent transactions. | <img src="screenshots/screenshot_2.png" width="320" alt="Category Donut Dark Mode" /> | <img src="screenshots/screenshot_7.png" width="320" alt="Category Donut Light Mode" /> |
| **Expenses & Transactions Tab**<br>Search by merchant/note, filter by category chip, and chronological grouped expenses with day totals. | <img src="screenshots/screenshot_3.png" width="320" alt="Expenses Tab Dark Mode" /> | <img src="screenshots/screenshot_8.png" width="320" alt="Expenses Tab Light Mode" /> |
| **Budgets & Spending Habits**<br>Monthly budget targets, habit analysis (daily avg, top category, peak expense), and progress bars. | <img src="screenshots/screenshot_4.png" width="320" alt="Budgets Tab Dark Mode" /> | <img src="screenshots/screenshot_9.png" width="320" alt="Budgets Tab Light Mode" /> |
| **Add Daily Expense Sheet**<br>Quick +$5 / +$10 / +$20 pills, preset merchant chips, category tags, date selector, and payment methods. | <img src="screenshots/screenshot_5.png" width="320" alt="Add Expense Modal Dark Mode" /> | <img src="screenshots/screenshot_10.png" width="320" alt="Add Expense Modal Light Mode" /> |

---

## ✨ Key Features

1. **📊 Visual Charts & Habit Analysis**:
   - **Daily Spending Trend**: Custom Canvas-rendered bar chart displaying the last 7 days of spending with average benchmark lines and active day highlights.
   - **Category Donut Chart**: Arc-drawn category distribution breakdown with dynamic percentages and colored category legends.
   - **Spending Habits Insights**: Automatic calculation of daily averages, top spending category, peak transaction amount, and transaction velocity.

2. **💰 Balance & Budget Management**:
   - **Current Balance Card**: Prominent summary of remaining funds relative to custom monthly budgets.
   - **Custom Category Budgets**: Set monthly limits for Food & Dining, Shopping, Transportation, Entertainment, and more with animated progress indicators and over-budget warnings.

3. **⚡ Rapid Transaction Logging**:
   - Quick increment buttons (`+$5`, `+$10`, `+$20`, `+$50`, `+$100`).
   - Frequent merchant tags (`Coffee`, `Groceries`, `Lunch`, `Dinner`, `Uber Ride`, `Gas`, `Netflix`).
   - Category tags with distinctive color coding and two-letter badge icons.
   - Quick date selector (`Today`, `Yesterday`, or custom `YYYY-MM-DD`).
   - Payment method selection (`Credit Card`, `Debit Card`, `Cash`).

4. **🔒 Dual Persistence & Privacy**:
   - **Offline Guest Mode**: Local SQLite database via **Room** (`ExpenseDao`, `BudgetDao`, `AppDatabase`).
   - **Cloud Sync**: Real-time snapshot listener on **Firebase Firestore** with zero-trust owner security rules.
   - **Google Sign-In**: Credential Manager integration for frictionless authentication without passwords.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Language**: Kotlin 2.0+ (100% Coroutines & Flow)
- **State Management**: MVVM with `ViewModel`, `StateFlow`, `collectAsStateWithLifecycle`
- **Local Persistence**: [Android Room](https://developer.android.com/training/data-storage/room) with KSP (Kotlin Symbol Processing)
- **Cloud Backend**: [Firebase Firestore](https://firebase.google.com/docs/firestore) & [Firebase Authentication](https://firebase.google.com/docs/auth)
- **Identity**: Android Jetpack `CredentialManager` (`GetSignInWithGoogleOption`)
- **Testing**: Robolectric, JUnit 4, and Firebase Local Rules Test Suite

---

## 🚀 How to Build and Run the App

### 1. Prerequisites

Make sure your development machine has the following installed:
- **Android Studio** Ladybug (2024.2.1+) or newer
- **JDK 17** or **JDK 21** configured as the Gradle JDK
- **Android SDK Platform 36** (API Level 36) with Android Build Tools 36.0.0
- **Android Emulator** or physical device running Android 7.0 (API 24) or higher

---

### 2. Clone the Repository

```bash
git clone https://github.com/your-username/spendwise.git
cd spendwise
```

---

### 3. Environment & Configuration Setup

1. **Environment Variables**:
   Copy the `.env.example` file to `.env`:
   ```bash
   cp .env.example .env
   ```
   *(Optional)* If using cloud Gemini AI features, configure your key in `.env`:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

2. **Firebase Configuration** *(Optional for Cloud Sync; not needed for Guest Mode)*:
   - For Google Sign-In and cloud Firestore sync, register your app package `com.aistudio.expensetracker.mkvq` in the [Firebase Console](https://console.firebase.google.com/).
   - Download `google-services.json` and place it in the `app/` directory:
     ```
     app/google-services.json
     ```
   - Ensure the SHA-1 and SHA-256 fingerprints from your debug keystore (`./debug.keystore`) are added to the Firebase project settings.
   - Deploy the included security rules:
     ```bash
     firebase deploy --only firestore:rules
     ```

---

### 4. Build Using Gradle CLI

You can build and package the APK directly from the terminal using Gradle:

#### Assemble Debug APK
```bash
gradle assembleDebug
```
*The compiled APK will be generated at:*  
`app/build/outputs/apk/debug/app-debug.apk`

#### Assemble Release APK
```bash
gradle assembleRelease
```
*The signed release APK will be generated at:*  
`app/build/outputs/apk/release/app-release.apk`

---

### 5. Run Unit & Robolectric Tests

Run all unit tests and database integration tests:

```bash
gradle :app:testDebugUnitTest
```

To run specifically the repository and rule integration tests:
```bash
gradle :app:testDebugUnitTest --tests "com.example.ExpenseRepositoryRuleTest"
```

To run the Firebase Firestore security rules test suite:
```bash
node --test firestore.test.js
```

---

### 6. Install and Run on Device or Emulator

#### Via Android Studio:
1. Open Android Studio and select **Open** -> choose the `spendwise` project root.
2. Allow Gradle sync to complete.
3. Select an Android emulator (e.g., Pixel 8 with API 34 or 35) or connect a physical Android device with USB debugging enabled.
4. Click **Run** (`Shift + F10`) or click the green Play icon in the top toolbar.

#### Via ADB Command Line:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.aistudio.expensetracker.mkvq/com.example.MainActivity
```

---

## 📂 Project Directory Structure

```
├── app/
│   ├── build.gradle.kts           # App module Gradle configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt               # Main entry point & session management
│       │   │   ├── data/
│       │   │   │   ├── local/                    # Room Database (Expense, LocalBudget, DAOs)
│       │   │   │   ├── model/                    # Data models (Expense, Budget, Category)
│       │   │   │   └── repository/               # Dual-mode Local & Firestore Repository
│       │   │   └── ui/
│       │   │       ├── MainScreen.kt             # Dashboard, Transactions, and Budget tabs
│       │   │       ├── ExpenseViewModel.kt       # Habits analytics and UI state flow
│       │   │       ├── auth/                     # Google Sign-In & AuthScreen
│       │   │       ├── components/               # Canvas Charts, Dialogs, BottomSheet
│       │   │       └── theme/                    # Material 3 ColorScheme & ChartColors
│       │   └── res/                              # Adaptive icons, values, and themes
│       └── test/                                 # Robolectric & JVM unit tests
├── firestore.rules                               # Zero-Trust Firestore security rules
├── firestore.test.js                             # Emulator tests for security rules
├── screenshots/                                  # Showcase screenshot assets
│   ├── screenshot_1.png                          # Dashboard Dark Mode
│   ├── screenshot_2.png                          # Category Donut Dark Mode
│   ├── screenshot_3.png                          # Expenses Tab Dark Mode
│   ├── screenshot_4.png                          # Budgets Tab Dark Mode
│   ├── screenshot_5.png                          # Add Daily Expense Sheet Dark Mode
│   ├── screenshot_6.png                          # Dashboard Light Mode
│   ├── screenshot_7.png                          # Category Donut Light Mode
│   ├── screenshot_8.png                          # Expenses Tab Light Mode
│   ├── screenshot_9.png                          # Budgets Tab Light Mode
│   └── screenshot_10.png                         # Add Daily Expense Sheet Light Mode
├── build.gradle.kts                              # Root build file
├── settings.gradle.kts                           # Module & project definitions
└── README.md                                     # Project documentation
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
