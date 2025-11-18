package com.example.studymate2.worker

import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.util.*

class CalendarSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result {
        val user = auth.currentUser ?: run {
            Log.w("CalendarSyncWorker", "No user signed in; skipping sync")
            return Result.success()
        }

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )

        val calendarEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 30)
        }.timeInMillis

        val selection = "(${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(calendarEnd.toString())

        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )

        val events = mutableListOf<Map<String, Any>>()

        cursor?.use {
            val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
            val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val endIndex = it.getColumnIndex(CalendarContract.Events.DTEND)
            while (it.moveToNext()) {
                val title = it.getString(titleIndex) ?: "Untitled Event"
                val start = it.getLong(startIndex)
                val end = it.getLong(endIndex)
                events.add(
                    mapOf(
                        "title" to title,
                        "start" to start,
                        "end" to end
                    )
                )
            }
        }
        cursor?.close()

        Log.d("CalendarSyncWorker", "Collected ${events.size} events")

        try {
            val calendarRef = db.collection("users")
                .document(user.uid)
                .collection("calendar")
                .document("latest")

            calendarRef.set(mapOf("events" to events, "syncedAt" to System.currentTimeMillis()))
                .await()

            Log.d("CalendarSyncWorker", "Uploaded ${events.size} events to Firestore")
            return Result.success()
        } catch (e: Exception) {
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Log.e("CalendarSyncWorker", "Sync failed due to permissions; not retrying", e)
                return Result.failure()
            }
            Log.e("CalendarSyncWorker", "Sync failed", e)
            return Result.retry()
        }
    }
}
