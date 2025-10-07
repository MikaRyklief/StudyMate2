📘 StudyMate2 – Smart Student Planner App

Developed by:

Mika Ryklief (ST10318273)

Steven Bomela (ST10304166)

Jesse Weeder (ST10320806)

Daniel van Loggerenberg (ST10310998)

🧩 Overview

StudyMate2 is an Android application designed to help students manage their academic life efficiently. It combines productivity, motivation, and wellness through features such as:

Task and Exam Management

Smart Timetable Generation

Google Calendar Integration

Gamification and Analytics

Pomodoro Timer and Wellness Tools

This app is developed as part of the PROG7314 POE module at The Independent Institute of Education (IIE).

🚀 Core Features
Category	Features
Authentication	Google Sign-In via Firebase, optional biometric login
Academic Organisation	Create, edit, and track tasks, assignments, and exams
Calendar Integration	Sync study sessions and exams to Google Calendar
Smart Timetable Generator	Automatically allocates study sessions based on deadlines
Analytics Dashboard	Displays charts of study trends and task completion
Gamification	XP, streaks, and badges to motivate consistency
Wellness Tools	Pomodoro timer and motivational wellness nudges
Offline Support	Uses Room database and syncs with Firestore when online
Multilingual Support	English + isiZulu/Afrikaans
🧱 Technologies Used

Language: Kotlin

IDE: Android Studio (Hedgehog or newer)

Architecture: MVVM (Model–View–ViewModel)

Database: Firebase Firestore + Room (for offline sync)

Authentication: Firebase Auth (Google SSO + BiometricPrompt API)

Charts & Analytics: MPAndroidChart

UI Library: Material Design 3

Notifications: Android AlarmManager + Firebase Messaging

Calendar Integration: Android CalendarContract Provider

📂 Project Structure
StudyMate2/
├── app/
│   ├── src/main/java/com/example/studymate2/
│   │   ├── ui/            # Fragments (Dashboard, Planner, Wellness, Analytics)
│   │   ├── data/          # Data models (StudyTask, StudyBlock)
│   │   ├── util/          # Utilities (SmartTimetableGenerator, ColorProvider)
│   │   ├── notification/  # Notification logic
│   │   └── viewmodel/     # ViewModels + Factories
│   └── res/               # Layouts, drawables, and XML resources
└── build.gradle

⚙️ Installation & Setup
1. Prerequisites

Android Studio 2023.3+

Minimum SDK: 24 (Android 7.0)

Target SDK: 34

Firebase Project: Linked via google-services.json

2. Clone the repository
git clone https://github.com/<yourusername>/StudyMate2.git
cd StudyMate2

3. Open in Android Studio

Launch Android Studio → Open an Existing Project.

Select the StudyMate2 directory.

Allow Gradle to sync automatically.

4. Configure Firebase

Go to Firebase Console
.

Add your Android app using your package name:

com.example.studymate2


Download the google-services.json file and place it in:

app/google-services.json


Ensure Firebase Authentication and Firestore Database are enabled.

5. Add Google Calendar Permission

Ensure your AndroidManifest.xml includes:

<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />

▶️ Running the App

Connect an Android device or start an emulator.

Click Run ▶ in Android Studio.

Sign in with Google when prompted.

Use the bottom navigation bar to explore:

Dashboard: Overview of progress and quick stats

Planner: Manage tasks, exams, and generate timetable

Analytics: Visual charts using MPAndroidChart

Wellness: Pomodoro timer and motivational features

📊 API Endpoints

Firebase Functions (REST-style):

Endpoint	Method	Description
/tasks	GET, POST, PUT, DELETE	Manage tasks and exams
/streaks	GET, POST	Track XP and streaks
/analytics	GET, POST	Manage study metrics
/pomodoro	GET, POST	Record focus sessions
🧠 Key Classes
Class	Description
DashboardFragment	Displays progress and analytics overview
PlannerFragment	Handles task CRUD, timetable generation, and calendar sync
StudyTaskViewModel	Central ViewModel managing task data
SmartTimetableGenerator	Automatically allocates study sessions
StudyNotificationScheduler	Triggers local study reminders
🧩 Troubleshooting
Issue	Fix
Unresolved reference: position	Use setPosition() instead of assigning directly
'val' cannot be reassigned	Rename shadowed variables or use var
Google sign-in fails	Check google-services.json and OAuth Client ID
Calendar not updating	Ensure calendar permissions are granted at runtime
🧾 License

This project was developed for educational purposes as part of the PROG7314 module at IIE.
It is not intended for commercial release.
