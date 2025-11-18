package com.example.studymate2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudyTask::class, GamificationProfile::class], version = 4, exportSchema = false)
@androidx.room.TypeConverters(Converters::class)
abstract class StudyDatabase : RoomDatabase() {

    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun gamificationDao(): GamificationDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_mate_database"
                )
                    // 👇 THIS LINE fixes your crash when schema changes
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
