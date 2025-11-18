package com.example.studymate2.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

object StudyNotificationScheduler {
    const val DAILY_CHANNEL_ID = "study_daily_channel"
    const val DEADLINE_CHANNEL_ID = "study_deadline_channel"
    const val WELLNESS_CHANNEL_ID = "study_wellness_channel"

    private const val DAILY_WORK_NAME = "daily_study_reminder"
    private const val IMMEDIATE_WORK_NAME = "upcoming_task_ping"
    private const val WELLNESS_WORK_NAME = "wellness_nudges"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val dailyChannel = NotificationChannel(
                DAILY_CHANNEL_ID,
                context.getString(com.example.studymate2.R.string.notification_channel_daily_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(com.example.studymate2.R.string.notification_channel_daily_desc)
            }
            val deadlineChannel = NotificationChannel(
                DEADLINE_CHANNEL_ID,
                context.getString(com.example.studymate2.R.string.notification_channel_deadline_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(com.example.studymate2.R.string.notification_channel_deadline_desc)
            }
            val wellnessChannel = NotificationChannel(
                WELLNESS_CHANNEL_ID,
                context.getString(com.example.studymate2.R.string.notification_channel_wellness_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(com.example.studymate2.R.string.notification_channel_wellness_desc)
            }
            manager.createNotificationChannel(dailyChannel)
            manager.createNotificationChannel(deadlineChannel)
            manager.createNotificationChannel(wellnessChannel)
        }
    }

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(DAILY_WORK_NAME)
            return
        }
        if (!hasNotificationPermission(context)) {
            workManager.cancelUniqueWork(DAILY_WORK_NAME)
            return
        }

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        val delayMillis = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<TaskReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(TaskReminderWorker.KEY_SHOW_DAILY to true))
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun triggerImmediateRefresh(context: Context) {
        if (!hasNotificationPermission(context)) return
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(workDataOf(TaskReminderWorker.KEY_SHOW_DAILY to false))
            .build()
        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleWellnessNudges(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled || !hasNotificationPermission(context)) {
            workManager.cancelUniqueWork(WELLNESS_WORK_NAME)
            return
        }

        val constraints = androidx.work.Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<WellnessNudgeWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WELLNESS_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
