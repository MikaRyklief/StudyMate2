# 📘 StudyMate – Smart Student Planner App

> **A Kotlin Android app for academic organization, productivity, and wellness.**

---

## 👥 Team Members

| Name | Student Number |
|------|----------------|
| Mika Ryklief | ST10318273 |
| Steven Bomela | ST10304166 |
| Jesse Weeder | ST10320806 |
| Daniel van Loggerenberg | ST10310998 |

---

## 📖 Overview

**StudyMate2** is a productivity and wellness app for students that helps manage academic tasks, exams, and schedules efficiently.  
It combines **Google Calendar integration**, **gamification**, **analytics**, and **Pomodoro-based focus tools** to support academic success.

Developed for the **PROG7314 POE (Part 2)** at **The Independent Institute of Education (IIE)**.

---

## 🚀 Features

| Category | Description |
|-----------|-------------|
| **Authentication** | Google Sign-In via Firebase Authentication + Biometric Login |
| **Task Management** | Add, edit, delete, and view assignments, exams, and study sessions |
| **Smart Timetable Generator** | Automatically creates a balanced study schedule |
| **Google Calendar Sync** | Exports study sessions and exams to your Google Calendar |
| **Analytics Dashboard** | Displays charts and trends using MPAndroidChart |
| **Gamification** | Earn XP, streaks, and badges for consistency |
| **Pomodoro Timer** | Focus timer with study/break cycles and wellness nudges |
| **Offline Support** | Local Room database syncs with Firestore when online |
| **Multilingual Support** | English and isiZulu/Afrikaans options |

---

## 🧱 Tech Stack

| Component | Technology |
|------------|-------------|
| **Language** | Kotlin |
| **IDE** | Android Studio (Hedgehog or later) |
| **Architecture** | MVVM (Model–View–ViewModel) |
| **Database** | Firebase Firestore + Room |
| **Authentication** | Firebase Auth (Google SSO + BiometricPrompt API) |
| **Charts & Analytics** | MPAndroidChart |
| **UI Library** | Material Design 3 (Material Components) |
| **Notifications** | AlarmManager + Firebase Messaging |
| **Calendar Integration** | Android `CalendarContract` Provider |

---

## 📂 Project Structure

StudyMate2/
│
├── app/
│ ├── src/main/java/com/example/studymate2/
│ │ ├── ui/ # Fragments (Dashboard, Planner, Analytics, Wellness)
│ │ ├── data/ # Data models (StudyTask, StudyBlock, etc.)
│ │ ├── util/ # Utilities (SmartTimetableGenerator, ColorProvider)
│ │ ├── notification/ # Notification scheduling and triggers
│ │ └── viewmodel/ # ViewModels and factories
│ └── res/ # Layouts, drawables, values, etc.
│
├── build.gradle
└── README.md
---

## ⚙️ Setup Instructions

### 1️⃣ Prerequisites
- Android Studio **2023.3 (Hedgehog)** or newer  
- Android SDK 24+  
- A Firebase Project  
- Google account for Calendar integration  

### 2️⃣ Clone the Repository
```bash
git clone https://github.com/<your-username>/StudyMate2.git
cd StudyMate2
3️⃣ Open in Android Studio

Open Android Studio → File → Open...

Select the cloned StudyMate2 project folder.

Let Gradle sync automatically.

4️⃣ Connect to Firebase

Visit Firebase Console

Create a new project → Add Android App with package name:

com.example.studymate2


Download the google-services.json file and place it in:

app/google-services.json


Enable:

Firebase Authentication (Google Sign-In)

Cloud Firestore

5️⃣ Grant Calendar Permissions

Ensure your AndroidManifest.xml includes:

<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />

6️⃣ Build & Run

Connect an Android device or launch an emulator.

Click Run ▶️ in Android Studio.

Log in with Google.

Explore features through the bottom navigation bar:

🏠 Dashboard

📅 Planner

📊 Analytics

🧘 Wellness

🧠 Key Components
Class	Purpose
DashboardFragment	Displays overall progress and analytics charts
PlannerFragment	Handles task CRUD, Google Calendar sync, timetable generation
StudyTaskViewModel	Central ViewModel for managing tasks
SmartTimetableGenerator	Automatically creates study sessions
StudyNotificationScheduler	Sends local notifications and reminders
🧩 Common Errors & Fixes
Issue	Cause	Fix
Unresolved reference: position	Using property instead of setter	Use setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
'val' cannot be reassigned	Shadowed parameter name	Rename function variable or use this.
Google Sign-In fails	Missing OAuth credentials	Check Firebase OAuth client and google-services.json
Calendar not updating	Permissions not granted	Grant calendar permission at runtime
📊 API Overview

Firebase REST-style Functions:

Endpoint	Method	Description
/tasks	GET, POST, PUT, DELETE	Manage tasks and exams
/streaks	GET, POST	Track XP and streaks
/analytics	GET, POST	Store and fetch study statistics
/pomodoro	GET, POST	Log completed focus sessions📺 Demo Video: Watch on YouTube 

❤️ Acknowledgements

MPAndroidChart

Firebase Android SDK

Material Design 3

IIE Programming 3D Module Manual (2025)

“Study smarter, live better — your StudyMate for success.”
