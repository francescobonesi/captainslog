# Captain's Log

A modern, native Android application built in Kotlin to help you log, categorize, and analyze your personal events and activities.

## ✨ Features

*   **Event Logging:** Easily add and manage your daily activities and events.
*   **Categorization:** Organize events with custom categories or tags.
*   **Statistical Insights:** View summaries and statistics of your logged events, including yearly and monthly breakdowns, daily counts, and streaks.
*   **Data Visualization:** Understand your habits and patterns through clear statistical representations.
*   **Modern UX:** A clean and intuitive user interface for a smooth logging experience.

## 🖖 Star Trek Inspiration

The name "Captain's Log" is a playful nod to Star Trek, where star dates are used to log events. In this application, we also compute and display a "Stardate" for each event. The formula used to calculate the Stardate is:

`1000 * (Year - 1900) + (1000 * DayOfYear / DaysInYear)`

This formula provides a value that increases throughout the year, similar to how Stardates are presented in Star Trek: The Next Generation.

## 🛠 Tech Stack & Architecture

This project is a showcase of modern Android development practices.

*   **Language:** **Kotlin**
*   **Architecture:** **MVVM (Model-View-ViewModel)** to separate UI logic from business logic.
*   **Asynchronous Programming:** **Kotlin Coroutines** (`viewModelScope`, `lifecycleScope`) for background operations.
*   **UI Toolkit:** **Android Views XML** with `ViewBinding`.
*   **Database:** **Room** (SQLite abstraction) for local data persistence, including the use of DAOs, Entities, and `TypeConverters`.
*   **Dependency Management:** **Gradle** with Kotlin KTS.

## 🚀 Setup and Build

1.  Clone the repository: `git clone <repository-url>`
2.  Open the project in the latest stable version of Android Studio.
3.  Let Gradle sync and download the required dependencies.
4.  Build and run on an emulator or a physical device.

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
