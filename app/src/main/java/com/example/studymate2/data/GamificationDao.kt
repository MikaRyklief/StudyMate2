package com.example.studymate2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GamificationDao {
    @Query("SELECT * FROM gamification_profile WHERE id = :userId LIMIT 1")
    fun observeProfile(userId: Int): Flow<GamificationProfile?>

    @Query("SELECT * FROM gamification_profile WHERE id = :userId LIMIT 1")
    suspend fun getProfileOnce(userId: Int): GamificationProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: GamificationProfile)
}