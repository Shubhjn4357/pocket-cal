# Pocket Cal 🗓️ (Modern Edition)

Pocket Cal is a highly polished, offline-first personal scheduler and planner built using **Modern Android Development (MAD)** practices. Featuring a visual design with full **Material Design 3 (M3)** adaptive aesthetics, it enables seamless scheduling, local persistence, health metrics correlation, and flexible alarm offsets.

---

## ✨ Features

### 1. Dual Aesthetic Themes
*   **Cosmic Dark Theme**: A deep space interface utilizing sophisticated indigo and violet accents, perfect for low-light execution and high-contrast accessibility.
*   **Twilight Light Theme**: A bright, clean, paper-white canvas with gentle shadows, soft grey outlines, and calming blue highlights.
*   *Automatic Accent Contrast*: The Floating Action Button (FAB) dynamically colors its foreground content (`onPrimary`) to stand out perfectly against its container color in both light and dark modes.

### 2. Intelligent Slot-Based Chronological Agenda
*   Interactive day-by-day ribbon slider with smooth swipe gestures.
*   Unified activity feed that displays tasks, calendar items, and active workout logs sequentially rather than using a standard empty list.
*   Seamless item state progression (Swipe-to-Dismiss to delete or mark tasks as completed).

### 3. Google Services Sync Integration (Simulation)
*   **Combined Dashboard**: Single-click authorization flow to securely bridge local schedules with simulated accounts.
*   **Google Calendar Sync**: Imports live appointments directly into appropriate hour blocks.
*   **Google Fit Sync**: Combines activity durations, target progress, and physical records into the calendar flow so physical health and calendar events stay aligned.

### 4. Custom Advanced Alarms & Notifications
*   Interactive configuration allows setting reminder alarm offsets (uniquely customizable to at event start, 5, 10, 15, or 30 minutes in advance).
*   Built-in local test trigger pipeline to verify individual notification delivery immediately inside the OS.

---

## 🛠️ Architecture & Tech Stack

This application is built entirely in **100% Kotlin** and adheres to clean architecture principles:

*   **UI Framework**: Jetpack Compose (using pure Material 3 components).
*   **Data Persistence**: Room Database with robust KSP auto-generation for local SQLite query operations.
*   **Theme Engine**: CENTRALIZED `Theme.kt` with dynamic Material 3 shape, typography, and light/dark color mappings.
*   **Testing**: Robolectric (Local JVM Android environment simulation) & Roborazzi (automated screenshot regression tests).

---

## 📦 Build & Installation

### Build Requirements
*   Android SDK 34 (Upside Down Cake or higher)
*   Gradle 8+ with Kotlin DSL
*   Java JDK 17+

### Generating the Applet APK
To build the app locally or within the development platform:
```bash
gradle :app:assembleDebug
```

### Automated Asset Pipeline
The project is configured with a custom Gradle task pipeline that triggers automatically upon a successful build:
1.  Compiles the Android codebase under the `:app:assembleDebug` configuration.
2.  Executes the custom `copyApkToAssets` Gradle task.
3.  Copies and renames the generated debug binary:
    *   **Source**: `app/build/outputs/apk/debug/app-debug.apk`
    *   **Destination**: `assets/pocket-cal.apk`

*You can always find the latest installable build artifact pre-staged under `/assets/pocket-cal.apk`.*

---

## 🧪 Testing State

All local JVM and UI screenshot tests compile and pass successfully. To execute unit and visual regression checks:

```bash
# Run Unit and Robolectric Tests
gradle :app:testDebugUnitTest

# Record Reference Screenshot Tests (when updating UI)
gradle :app:recordRoborazziDebug

# Validate visual consistency
gradle :app:verifyRoborazziDebug
```
