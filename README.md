# 📘 StudyMate – Smart Student Planner App

GitHub Link: https://github.com/VCCT-PROG7314-2025-G2/StudyMate_Part2

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

## References
* https://www.pubnub.com/tutorials/mobile-push-notifications-android-kotlin/
* https://developer.android.com/develop/ui/views/notifications/build-notification
* https://developer.android.com/identity/sign-in/credential-manager-siwg
* https://firebase.google.com/docs/auth/android/google-signin
* https://developer.android.com/codelabs/biometric-login#0

  
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

## Utilization of GitHub Actions
GitHub Actions was used to conduct the unit testing for the application to ensure everything runs well. Find screenshots below
<img width="1276" height="877" alt="image" src="https://github.com/user-attachments/assets/807cb5ea-bded-4eca-bd74-262bd703f3ee" />


## Utilization of AI
* ChatGPT was used to troubleshoot our application crashing, as well as to fix an error we had where we were unable to access our repository. Prompt and solution below:
![WhatsApp Image 2025-10-07 at 22 46 46_1d2079ec](https://github.com/user-attachments/assets/8e6016bd-bf6c-4cad-879b-f5f652d1b8e4)
![WhatsApp Image 2025-10-07 at 22 46 47_186f9e36](https://github.com/user-attachments/assets/a6cd6ca4-dcd0-4630-8b6a-d96a13261aa7)
![WhatsApp Image 2025-11-18 at 23 03 28_576d795c](https://github.com/user-attachments/assets/479824c2-162a-4e53-a30b-cb96735e4e70)
![WhatsApp Image 2025-11-18 at 23 03 35_fcda8e17](https://github.com/user-attachments/assets/c9f7d180-0fa0-43f8-9464-0c0020bb95d8)



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

📢Release Notes
The final version of StudyMate represents a major evolution from the initial prototype. While the prototype demonstrated only basic task creation and navigation, the completed release introduces a full suite of academic, motivational, and wellness features designed to support students holistically.

Below is a complete list of everything that has been added, improved, or redesigned since the first prototype.

✅ 1. Core Feature Enhancements (Prototype → Final Version)
1.1 Authentication and Security
Added since prototype:
✅ Google Single Sign-On (SSO) using Firebase Authentication.
✅ Biometric Authentication (Fingerprint/FaceID) via Android BiometricPrompt API.
Improved secure session handling and auto-login experience.

1.2 Task & Exam Management
Added since prototype:
✅ Full CRUD (create, read, update, delete) for tasks and exams.
✅ Subject categorisation for organizing all academic activities.
✅ Exam countdowns automatically calculated and displayed.
Improved UI for adding deadlines, priorities, and notes.

1.3 Notifications & Reminders
Added since prototype:
✔ Daily reminders (customisable time).
✔ Exam countdown notifications.
✔ Task completion alerts.
✔ Wellness nudges (optional hydration/stretch reminders).

1.4 Offline Mode
Added since prototype:
✔ Local Room database for offline task/exam creation.
✔ Auto-sync with Firestore once online.
✔ Network-aware workers (WorkManager) for background sync.

1.5 Multilingual Support
Added since prototype:
English (default)
Afrikaans
isiZulu
Includes:
Dynamic string switching
Activity recreation without crashing
Settings-based language selector

⭐ 2. Innovative Features Added 

2.1 Smart Timetable Generator
Auto-creates revision sessions based on:
Upcoming deadlines
Subject difficulty
Exam proximity
Workload distribution

2.2 Gamification System
XP points for completing tasks
Streak tracking
Badge unlocking
Progress indicators

2.3 Analytics Dashboard
Weekly study time graph
Task completion rates
Subject-focused time distribution
Insightful study tips based on behaviour

2.4 Wellness Tools 
Pomodoro timer
Break reminders
Hydration & wellbeing nudges

2.5 Calendar Visualisation
Weekly & monthly views
Colour-coded by subject
Upcoming tasks and exams integrated directly into calendar
Drag-and-drop timetable editing (where supported)

🎨 3. UI/UX Improvements Since Prototype
Completely redesigned visual style using Material 3.
New dashboard layout showing:
Today’s tasks
Upcoming exams
Mini-calendar
Cleaner navigation using the bottom navigation bar.
Animations and smooth transitions added to improve usability.
Updated settings screen with toggle controls, language selector, and reminder configuration.

🔌 4. Backend & Infrastructure Upgrades Since Prototype
Firebase Firestore backend integrated for all core data.
Serverless REST-like operations via Firestore listeners.
Offline sync implemented using Room + WorkManager.
Secure data access via Firebase Auth rules (per-user read/write).
Error handling & resilience added for low connectivity.

🛠 5. Stability & Performance Enhancements
Fixed crashes related to:
Activity recreation during language switching
View detachment issues
Firestore permission mismatches
Reduced unnecessary Firestore reads/writes.
Optimised Worker scheduling to avoid battery drain.
Implemented lifecycle-safe UI updates.

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



 API Overview
Firebase REST-style Functions:

Endpoint	Method	Description
/tasks	GET, POST, PUT, DELETE	Manage tasks and exams
/streaks	GET, POST	Track XP and streaks
/analytics	GET, POST	Store and fetch study statistics
/pomodoro	GET, POST	Log completed focus sessions📺 Demo Video: Watch on YouTube https://www.youtube.com/watch?v=qWRgg0yIot8

 Acknowledgements

MPAndroidChart

Firebase Android SDK

Material Design 3

IIE Programming 3D Module Manual (2025)

“Study smarter, live better — your StudyMate for success.”
