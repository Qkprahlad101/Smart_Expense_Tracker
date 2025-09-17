# Smart Expense Tracker 📊

A modern Android app for effortless expense tracking. Log expenses, attach receipts, view categorized lists, and generate insightful reports—all in Indian Rupees (₹). Built with Kotlin and Jetpack Compose for a smooth, intuitive experience.

## About the App
Smart Expense Tracker is a personal finance tool designed to help users manage daily expenses efficiently. It supports adding expenses with categories, notes, and receipts; filtering and grouping lists; and visualizing data through graphs. The app is offline-capable with local storage, features visual enhancements like colored categories and animations, and ensures data persistence using Room DB. It's optimized for Indian users with INR formatting and grouping.

Key highlights:
- Secure receipt handling (images/PDFs) with previews.
- Proportional visualizations showing expense contributions.
- Clean, responsive UI with light animations.

## App Libs and Tools Used 🛠️
The app leverages modern Android tools for performance, maintainability, and user experience:

- **Jetpack Compose**: Declarative UI framework—enables fast, reactive interfaces with built-in animations.
- **Room**: Persistent database—provides offline support and type-safe queries for expenses.
- **Koin**: Dependency injection—lightweight and easy to set up for ViewModels and repositories.
- **Coroutines & Flow**: Async operations—handles DB tasks and UI updates efficiently without blocking threads.
- **Coil**: Image loading—quickly renders receipt previews from URIs.
- **FileProvider & OpenDocument**: Secure file handling—manages receipt attachments and permissions.
- **Navigation Component**: Screen navigation—simple routing between entry, list, and report views.

These tools ensure the app is lightweight, scalable, and easy to test.

## App Structuring and Architecture 📂
The app follows **MVVM architecture** for separation of concerns, making it modular and testable:

- **Model**: Data classes (e.g., Expense) and Room entities for data persistence.
- **View**: Jetpack Compose composables for UI (screens like Entry, List, Report).
- **ViewModel**: Manages state and business logic, using Coroutines for async calls.
- **Repository**: Single source for data operations, bridging Room DB and ViewModels.

Project structure:
- `ui/screens`: Feature-specific composables (e.g., ExpenseListScreen.kt).
- `data/database`: Room DB setup (entities, DAO, converters for enums).
- `data/repository`: Data access layer.
- `model`: Enums (Category, DatePreset) for type-safe constants.
- `ui/components`: Reusable UI elements (e.g., CurrencyUtil for INR formatting).
- `navigation`: NavHost for routing.

This structure promotes reusability and clean code.

## How to Use 🚀
1. **Clone the Repository**: `git clone https://github.com/yourusername/smart-expense-tracker.git`
2. **Open in Android Studio**: Import the project and sync Gradle.
3. **Build and Run**: Connect a device or emulator (API 21+), then build and run.

### In-App Usage
- **Add Expense**: Go to Entry screen, fill details, attach receipt, submit.
- **View List**: Filter by date or group by category; tap items for preview popup with receipt view.
- **Reports**: See graphs and totals; export to CSV.
- Navigation: Use back button or bottom buttons to switch screens.

Contributions welcome—fork and PR! For issues, open a ticket. 😊

