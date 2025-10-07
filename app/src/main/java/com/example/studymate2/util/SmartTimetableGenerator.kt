package com.example.studymate2.util

import com.example.studymate2.data.StudyBlock
import com.example.studymate2.data.StudyTask
import com.example.studymate2.data.TaskType
import java.util.Calendar
import java.util.concurrent.TimeUnit

object SmartTimetableGenerator {
    private const val DAILY_STUDY_CAP_MINUTES = 5 * 60
    private const val BLOCK_MAX_MINUTES = 120

    fun generate(fromTasks: List<StudyTask>): List<StudyBlock> {
        if (fromTasks.isEmpty()) return emptyList()

        val tasks = fromTasks
            .filter { !it.completed }
            .sortedWith(compareBy<StudyTask> { it.taskType != TaskType.EXAM }
                .thenBy { it.dueDate })

        if (tasks.isEmpty()) return emptyList()

        val schedule = mutableListOf<StudyBlock>()
        val allocations = mutableMapOf<Long, MutableList<StudyBlock>>()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        tasks.forEach { task ->
            var remainingMinutes = adjustedDuration(task)
            var targetDay = maxOf(startOfToday, startOfDay(task.dueDate) - TimeUnit.DAYS.toMillis(3))

            while (remainingMinutes > 0) {
                if (targetDay > task.dueDate) {
                    targetDay = startOfDay(task.dueDate)
                }

                val dayBlocks = allocations.getOrPut(targetDay) { mutableListOf() }
                val usedMinutes = dayBlocks.sumOf { it.durationMinutes }
                val availableMinutes = DAILY_STUDY_CAP_MINUTES - usedMinutes

                if (availableMinutes <= 0) {
                    targetDay += TimeUnit.DAYS.toMillis(1)
                    continue
                }

                val blockMinutes = minOf(availableMinutes, remainingMinutes, BLOCK_MAX_MINUTES)
                val startTime = targetDay + TimeUnit.HOURS.toMillis(16) + TimeUnit.MINUTES.toMillis(usedMinutes.toLong())
                val block = StudyBlock(
                    taskId = task.id,
                    title = task.title,
                    subject = task.subject,
                    taskType = task.taskType,
                    startTimeMillis = startTime,
                    durationMinutes = blockMinutes
                )
                dayBlocks += block
                schedule += block
                remainingMinutes -= blockMinutes
            }
        }

        return schedule.sortedBy { it.startTimeMillis }
    }

    private fun startOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun adjustedDuration(task: StudyTask): Int {
        val base = task.durationMinutes.coerceAtLeast(30)
        return when (task.taskType) {
            TaskType.EXAM -> (base * 1.5).toInt()
            TaskType.REVISION -> (base * 1.1).toInt()
            TaskType.ASSIGNMENT -> base
        }
    }
}
