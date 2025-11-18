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
import kotlin.random.Random

class WellnessNudgeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!StudyNotificationScheduler.hasNotificationPermission(applicationContext)) {
            return Result.success()
        }

        val nudges = applicationContext.resources.getStringArray(R.array.wellness_nudges)
        if (nudges.isEmpty()) return Result.success()

        val message = nudges[Random.nextInt(nudges.size)]
        postNotification(message)
        return Result.success()
    }

    private fun postNotification(message: String) {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, StudyNotificationScheduler.WELLNESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_book)
            .setContentTitle(applicationContext.getString(R.string.wellness_nudge_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(WELLNESS_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val WELLNESS_NOTIFICATION_ID = 4300
    }
}