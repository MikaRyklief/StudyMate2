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

##  Overview

**StudyMate** is a productivity and wellness app for students that helps manage academic tasks, exams, and schedules efficiently.  
It combines **Google Calendar integration**, **gamification**, **analytics**, and **Pomodoro-based focus tools** to support academic success.

Developed for the **PROG7314 POE (Part 2)** at **The Independent Institute of Education (IIE)**.

---

##  Features

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

| **Link to YouTube video showing these features: https://youtu.be/qWRgg0yIot8?si=cRk33FpJJLTJPrcd
---

##  Design Considerations
* Made sure that text and background use contrasting colours which go together well to ensure that all text is visible and fits the design of the application
* Contains a settings tab which allows users to choose their preferred language, ensuring that our app is inclusive and universal ![WhatsApp Image 2025-10-07 at 21 54 43_05f50fca](https://github.com/user-attachments/assets/7ef74e77-6ebf-4455-a292-b111b6721f79)
* Allows users to change their time, allowing for users in different time zones to make use of the app
* Made use of push notifications so that user doesn't have to go into the app to be reminded about tests or study time![WhatsApp Image 2025-10-07 at 21 54 44_be22a37f](https://github.com/user-attachments/assets/028ba56f-36e7-4b99-96e6-8e2fe50e5141)



##  Tech Stack

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

##  Project Structure

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

Click Run in Android Studio.

Log in with Google. (Registered logins: ![WhatsApp Image 2025-10-07 at 21 59 11_8fccc41a](https://github.com/user-attachments/assets/5f5f139e-9395-4a68-8897-2d7ec54058c2)

Explore features through the bottom navigation bar:

 Dashboard

 Planner

 Analytics

 Wellness

 Key Components
Class	Purpose
DashboardFragment	Displays overall progress and analytics charts
PlannerFragment	Handles task CRUD, Google Calendar sync, timetable generation
StudyTaskViewModel	Central ViewModel for managing tasks
SmartTimetableGenerator	Automatically creates study sessions
StudyNotificationScheduler	Sends local notifications and reminders

 Common Errors & Fixes
Issue	Cause	Fix
Unresolved reference: position	Using property instead of setter	Use setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
'val' cannot be reassigned	Shadowed parameter name	Rename function variable or use this.
Google Sign-In fails	Missing OAuth credentials	Check Firebase OAuth client and google-services.json
Calendar not updating	Permissions not granted	Grant calendar permission at runtime

Utilization of GitHub Actions
GitHub Actions was used to conduct the unit testing for the application to ensure everything runs well. Find screenshots below

 API Overview
Firebase REST-style Functions:

Endpoint	Method	Description
/tasks	GET, POST, PUT, DELETE	Manage tasks and exams
/streaks	GET, POST	Track XP and streaks
/analytics	GET, POST	Store and fetch study statistics
/pomodoro	GET, POST	Log completed focus sessions📺 Demo Video: Watch on YouTube 

 Acknowledgements

MPAndroidChart

Firebase Android SDK

Material Design 3

IIE Programming 3D Module Manual (2025)

“Study smarter, live better — your StudyMate for success.”
