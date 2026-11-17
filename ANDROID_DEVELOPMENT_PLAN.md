# QuizDuel Native Android Application Development Plan

## 1. Executive Summary

This plan outlines the architecture, implementation phases, and technical decisions for delivering a fully functional native Android application for the QuizDuel system. The Android app connects directly to the existing Spring Boot backend (`springboot-backend`) running on port 5000, sharing the same H2 file database and business rules with the existing web client.

## 2. Architecture & Tech Stack

### 2.1 Android Architecture
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture separation
- **UI Framework**: Jetpack Compose with Material Design 3 (Material 3)
- **Language**: Kotlin 2.0+ / Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose with role-based routing (Student vs Teacher)
- **Networking**: Retrofit 2 + OkHttp 4 (with logging interceptor, token authentication interceptor)
- **Serialization**: Kotlinx Serialization (or Gson) applied consistently across all DTOs
- **Local Persistence & Security**:
  - Jetpack DataStore (Preferences) for app preferences (e.g. backend base URL, current section)
  - AndroidX Security Crypto (`EncryptedSharedPreferences`) or secure token manager for JWT storage
- **Dependency Injection**: Manual DI via a clean `AppContainer` / ServiceLocator (avoiding complex Hilt annotation processor overhead while maintaining full testability and decoupling)

### 2.2 Directory & Package Structure
The Android project is housed under `QuizDuel-Android/` at the repository root.
```
QuizDuel-Android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/quizduel/android/
│   │   │   │   ├── QuizDuelApp.kt
│   │   │   │   ├── core/
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── ApiClient.kt
│   │   │   │   │   │   ├── AuthInterceptor.kt
│   │   │   │   │   │   └── NetworkResult.kt
│   │   │   │   │   ├── security/
│   │   │   │   │   │   └── TokenManager.kt
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   └── navigation/
│   │   │   │   │       ├── Screen.kt
│   │   │   │   │       └── NavGraph.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/ (AuthDto, SectionDto, QuestionDto, BattleDto, InsightDto)
│   │   │   │   │   ├── remote/ (QuizDuelApiService)
│   │   │   │   │   └── repository/ (AuthRepo, SectionRepo, QuestionRepo, BattleRepo, InsightRepo)
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/ (Domain models & mapped UI entities)
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── auth/ (LoginScreen, RegisterScreen, AuthViewModel)
│   │   │   │   │   ├── student/
│   │   │   │   │   │   ├── StudentDashboardScreen.kt
│   │   │   │   │   │   ├── StudentBattlesScreen.kt
│   │   │   │   │   │   ├── QuizPlayScreen.kt
│   │   │   │   │   │   └── StudentViewModel.kt
│   │   │   │   │   ├── teacher/
│   │   │   │   │   │   ├── TeacherDashboardScreen.kt
│   │   │   │   │   │   ├── SectionsScreen.kt
│   │   │   │   │   │   ├── QuestionBankScreen.kt
│   │   │   │   │   │   ├── TeacherBattlesScreen.kt
│   │   │   │   │   │   ├── AnalyticsScreen.kt
│   │   │   │   │   │   ├── TeacherViewModel.kt
│   │   │   │   │   │   └── QuestionsViewModel.kt
│   │   │   │   │   ├── common/
│   │   │   │   │   │   ├── LeaderboardScreen.kt
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   └── Components.kt (StatBox, Notice, Dialogs)
│   │   │   │   └── di/
│   │   │   │       └── AppContainer.kt
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 3. Implementation Phases

- **Phase 1: Analysis & Documentation (Completed)**
  - Detailed review of backend controllers, DTOs, security, database schema, demo data, and frontend React pages.
  - Creation of `ANDROID_DEVELOPMENT_PLAN.md`, `FEATURE_PARITY_CHECKLIST.md`, and `API_INTEGRATION_REFERENCE.md`.

- **Phase 2: Git Setup & Android Studio Project Scaffolding**
  - Branch: `feature/native-android-app`.
  - Android project created inside `QuizDuel-Android/` with Gradle Kotlin DSL, Android Gradle Plugin, targetSdk 35/36/37, Jetpack Compose Material 3, Retrofit, OkHttp, Coroutines, Navigation Compose, Security Crypto.

- **Phase 3: Core Network, Security, & Architecture Setup**
  - Configure Retrofit API interface with exact Spring Boot DTOs and endpoints.
  - OkHttp token interceptor reading JWT from `TokenManager`.
  - Base URL configuration supporting emulator (`10.0.2.2:5000`), local LAN IP, or custom URL.
  - `AppContainer` providing singletons for ApiService, TokenManager, Preferences, and Repositories.

- **Phase 4: Authentication & Role-Based Navigation**
  - Login and Register screens supporting both Student and Teacher roles.
  - Token persistence and automatic session restoration via `GET /api/auth/me`.
  - Role-aware home routing: Teachers navigate to Teacher Dashboard & Management tools; Students navigate to Student Dashboard, Active Battles, Quiz Player, and Leaderboard.

- **Phase 5: Student Experience Implementation**
  - Student Dashboard: Active battles, joined sections, points, streak, join class code button.
  - Join Section dialog: Enter join code (`POST /api/sections/join`).
  - Active Battles list: View today's battles (`GET /api/battles/me/today`).
  - Interactive Quiz Screen:
    - Load battle questions (`GET /api/battles/{id}`).
    - Interactive 4-choice question flow with per-question timer / response tracker.
    - Submit answers (`POST /api/battles/{id}/answers`).
    - Display results, score, and rank update.
  - Class Leaderboard & Badges (`GET /api/leaderboard`, `GET /api/badges`).

- **Phase 6: Teacher Experience Implementation**
  - Teacher Dashboard: Real-time stats (Active Battles, Students, Average Score, Sections), Topic Mastery visualization, Today's Battles.
  - Sections Management: List sections, student counts, join code display, Create Section, View Roster, Add Students by email, Remove Student, Delete Section.
  - Question Bank: Section & topic selector, Topic list (create, rename, copy), Question list (create question with choices A-D, difficulty, correct answer, edit, delete).
  - Battles Management: Generate Today's Battles with time picker & weekend toggle, Today's battles table, Battle Results Log.
  - Analytics & Settings: Section battle time settings, weekend toggle, Mastery overview.

- **Phase 7: Build, Test, Debug, & Deliver**
  - Gradle debug build and APK generation.
  - End-to-end integration testing against the live Spring Boot backend.
  - Verification of data synchronization with the web frontend.
  - Documentation and deliverable packaging.
