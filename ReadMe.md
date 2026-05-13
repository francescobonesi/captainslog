# Captain's Log

A modern, native Android application built in Kotlin to help you meticulously log, describe, and analyze your most... *personal* daily events. Because every great captain needs a log of their daily missions, even the ones in the porcelain office. 🚽💩

## ✨ Features

*   **Event Logging:** Effortlessly log your daily 'deposits' and other significant bodily functions. 🚽
*   **Categorization:** Organize your logs with custom descriptions, like 'Morning Ritual' or 'Spicy Food Aftermath'. 🌶️
*   **Statistical Insights:** Gain profound insights into your digestive habits! View summaries of frequency, and timing. Are you regular? The data will tell! 📊
*   **Data Visualization:** Understand your patterns through clear statistical representations. Spot trends and become a true connoisseur of your gut health. 📈
*   **Modern UX:** A clean and intuitive user interface for a smooth and discreet logging experience. Your secrets are safe with CaptainsLog. 😉

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
