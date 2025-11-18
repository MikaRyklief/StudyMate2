package com.example.studymate2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gamification_profile")
data class GamificationProfile(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val streakDays: Int = 0,
    val lastCompletedDay: Long = 0L,
    val badges: List<String> = emptyList()
)