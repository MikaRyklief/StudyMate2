package com.example.studymate2.util

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.studymate2.R
import kotlin.math.abs

object SubjectColorProvider {
    private val colorRes = listOf(
        R.color.subject_blue,
        R.color.subject_green,
        R.color.subject_amber,
        R.color.subject_purple,
        R.color.subject_teal,
        R.color.subject_red
    )

    @ColorInt
    fun colorForSubject(context: Context, subject: String): Int {
        if (subject.isBlank()) {
            return ContextCompat.getColor(context, R.color.subject_blue)
        }
        val index = abs(subject.lowercase().hashCode()) % colorRes.size
        return ContextCompat.getColor(context, colorRes[index])
    }

    fun colorStateListForSubject(context: Context, subject: String): ColorStateList {
        return ColorStateList.valueOf(colorForSubject(context, subject))
    }
}
