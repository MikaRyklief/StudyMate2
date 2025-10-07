package com.example.studymate2.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.example.studymate2.data.StudyTask
import java.util.*

object CalendarHelper {

    fun getUpcomingEvents(context: Context, daysAhead: Int = 7): List<StudyTask> {
        val events = mutableListOf<StudyTask>()
        val cr: ContentResolver = context.contentResolver
        val startMillis = Calendar.getInstance().timeInMillis
        val endMillis = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysAhead)
        }.timeInMillis

        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection =
            "(( ${CalendarContract.Events.DTSTART} >= ? ) AND ( ${CalendarContract.Events.DTSTART} <= ? ))"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())

        val cursor: Cursor? = cr.query(
            uri,
            arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART
            ),
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val title = it.getString(1) ?: "Untitled Event"
                val start = it.getLong(2)
                events.add(
                    StudyTask(
                        id = 0, // keep Room id dummy; use externalId for uniqueness
                        externalId = "event_$id",
                        title = title,
                        subject = "Google Calendar",
                        dueDate = start,
                        durationMinutes = 60,
                        completed = false
                    )
                )
            }
        }
        return events
    }
}
