package com.example.studymate2.repository

import com.example.studymate2.data.GamificationDao
import com.example.studymate2.data.GamificationProfile
import com.example.studymate2.data.StudyTask
import com.example.studymate2.data.TaskType
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GamificationRepository(private val dao: GamificationDao) {

    private var activeUserId: Int = DEFAULT_USER_ID

    val profile: Flow<GamificationProfile>
        get() = dao.observeProfile(activeUserId)
            .map { it ?: GamificationProfile(id = activeUserId) }

    fun setActiveUser(userId: Int) {
        activeUserId = userId
    }

    suspend fun recordTaskCompletion(task: StudyTask) {
        val xpGain = when (task.taskType) {
            TaskType.EXAM -> 50
            TaskType.REVISION -> 20
            TaskType.ASSIGNMENT -> 15
        }
        applyProgress(xpGain)
    }

    suspend fun recordFocusSession(minutes: Int) {
        val xpGain = 5 + (minutes / 10)
        applyProgress(xpGain, focusBonus = true)
    }

    private suspend fun applyProgress(xpGain: Int, focusBonus: Boolean = false) {
        val current = dao.getProfileOnce(activeUserId) ?: GamificationProfile(id = activeUserId)
        val today = startOfDay(System.currentTimeMillis())
        val yesterday = today - TimeUnit.DAYS.toMillis(1)

        val newStreak = when (current.lastCompletedDay) {
            today -> current.streakDays
            yesterday -> current.streakDays + 1
            else -> 1
        }

        val newXp = (current.xp + xpGain).coerceAtLeast(0)
        val badgeSet = current.badges.toMutableSet()

        if (current.streakDays == 0 && newStreak >= 1) badgeSet.add(BADGE_FIRST_STEP)
        if (newStreak >= 3) badgeSet.add(BADGE_STREAK_3)
        if (newStreak >= 7) badgeSet.add(BADGE_STREAK_7)
        if (newXp >= 100) badgeSet.add(BADGE_CENTURY_XP)
        if (focusBonus) badgeSet.add(BADGE_FOCUS_MASTER)

        val updated = current.copy(
            xp = newXp,
            streakDays = newStreak,
            lastCompletedDay = today,
            badges = badgeSet.toList()
        )
        dao.upsert(updated)
    }

    private fun startOfDay(time: Long): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    companion object {
        const val DEFAULT_USER_ID = 1
        const val BADGE_FIRST_STEP = "first_step"
        const val BADGE_STREAK_3 = "streak_3"
        const val BADGE_STREAK_7 = "streak_7"
        const val BADGE_CENTURY_XP = "century_xp"
        const val BADGE_FOCUS_MASTER = "focus_master"
    }
}