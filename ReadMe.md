# CaptainsLog

A modern, native Android application built in Kotlin to manage and receive reliable daily reminders for important dates like birthdays, anniversaries, and holidays.


## ✨ Features

*   **Full CRUD Operations:** Easily add, view, update, and delete events.
*   **Event Categorization:** Assign a type (🎂 Birthday, ❤️ Anniversary, 🎉 Holiday) to each event.
*   **Dynamic UI Text:** Displays smart text like "Turning 30 this year," "Celebrated 5 years," or "50 today!" based on the current date.
*   **Dual Visualization:** Switch between a compact "All Months" view and a focused "Single Month" view with navigation.
*   **Powerful Filtering & Search:**
    *   Filter events by type using Material Design chips.
    *   Search for events by name.
*   **Reliable Daily Notifications:** Uses `WorkManager` to send a daily summary of the day's events, grouped by type.
*   **Home Screen Widget:** A clean, resizable widget that displays all events happening on the current day.
*   **Data Portability:** Import and export your entire event list using the CSV format via the Storage Access Framework.
*   **Modern UX:**
    *   Asks for notification permissions on startup.
    *   Safe delete with a confirmation dialog.
    *   Polished UI with a calming Teal/Cyan theme and custom app icon.

## 🛠 Tech Stack & Architecture

This project is a showcase of modern Android development practices.

*   **Language:** **Kotlin**
*   **Architecture:** **MVVM (Model-View-ViewModel)** to separate UI logic from business logic.
*   **Asynchronous Programming:** **Kotlin Coroutines** (`viewModelScope`, `lifecycleScope`) for background operations.
*   **UI Toolkit:** **Android Views XML** with `ViewBinding`.
*   **Database:** **Room** (SQLite abstraction) for local data persistence, including the use of DAOs, Entities, and `TypeConverters` for enums.
*   **Background Processing:** **WorkManager** for guaranteed, battery-efficient daily tasks.
*   **Navigation:** **Jetpack Navigation Component** with Safe Args for type-safe argument passing.
*   **File Handling:** **Storage Access Framework (SAF)** for secure, user-managed file access.
*   **Widget:** `AppWidgetProvider` and `RemoteViewsService` for a dynamic home screen experience.
*   **Dependency Management:** **Gradle** with Kotlin KTS.

## 🚀 Setup and Build

1.  Clone the repository: `git clone <your-repository-url>`
2.  Open the project in the latest stable version of Android Studio.
3.  Let Gradle sync and download the required dependencies.
4.  Build and run on an emulator or a physical device.

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.