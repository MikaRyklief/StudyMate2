package com.example.studymate2.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studymate2.MainActivity
import com.example.studymate2.R
import com.example.studymate2.data.StudyDatabase
import com.example.studymate2.data.StudyTaskRepository
import com.example.studymate2.data.TaskType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class TaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!StudyNotificationScheduler.hasNotificationPermission(applicationContext)) {
            return@withContext Result.success()
        }

        val repository = StudyTaskRepository(StudyDatabase.getDatabase(applicationContext).studyTaskDao())
        val tasks = repository.getPendingTasks()
        if (tasks.isEmpty()) {
            return@withContext Result.success()
        }

        val now = System.currentTimeMillis()
        val dayMillis = TimeUnit.DAYS.toMillis(1)
        val showDaily = inputData.getBoolean(KEY_SHOW_DAILY, true)
        val nextTask = tasks.minByOrNull { it.dueDate }
        if (showDaily && nextTask != null) {
            val daysUntil = ((nextTask.dueDate - now) / dayMillis).coerceAtLeast(0)
            postNotification(
                DAILY_NOTIFICATION_ID,
                StudyNotificationScheduler.DAILY_CHANNEL_ID,
                applicationContext.getString(R.string.notification_daily_title),
                applicationContext.getString(R.string.notification_daily_body, nextTask.title, daysUntil)
            )
        }

        val assignmentsDueSoon = tasks.filter {
            it.taskType != TaskType.EXAM && it.dueDate in now..(now + 2 * dayMillis)
        }.take(2)
        assignmentsDueSoon.forEachIndexed { index, task ->
            val daysUntil = ((task.dueDate - now) / dayMillis).coerceAtLeast(0)
            postNotification(
                ASSIGNMENT_NOTIFICATION_ID + index,
                StudyNotificationScheduler.DEADLINE_CHANNEL_ID,
                applicationContext.getString(R.string.notification_assignment_title),
                applicationContext.getString(R.string.notification_assignment_body, task.title, daysUntil)
            )
        }

        val upcomingExams = tasks.filter { task ->
            task.taskType == TaskType.EXAM && task.dueDate in now..(now + 7 * dayMillis)
        }.take(3)
        upcomingExams.forEachIndexed { index, exam ->
            val daysUntil = ((exam.dueDate - now) / dayMillis).coerceAtLeast(0)
            postNotification(
                EXAM_NOTIFICATION_ID + index,
                StudyNotificationScheduler.DEADLINE_CHANNEL_ID,
                applicationContext.getString(R.string.notification_exam_countdown, daysUntil, exam.subject),
                exam.title
            )
        }

        Result.success()
    }

    private fun postNotification(id: Int, channelId: String, title: String, message: String) {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_book)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }

    companion object {
        const val KEY_SHOW_DAILY = "show_daily"

        private const val DAILY_NOTIFICATION_ID = 4001
        private const val ASSIGNMENT_NOTIFICATION_ID = 4100
        private const val EXAM_NOTIFICATION_ID = 4200
    }
}
