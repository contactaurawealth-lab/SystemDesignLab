package com.systemdesignlab.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.systemdesignlab.app.core.database.dao.*
import com.systemdesignlab.app.core.database.entity.*

@Database(
    entities = [
        LessonProgressEntity::class,
        SimulationProgressEntity::class,
        ExerciseAttemptEntity::class,
        AudiobookProgressEntity::class,
        UserProgressEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        SpacedReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun simulationDao(): SimulationDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun audiobookDao(): AudiobookDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun spacedReviewDao(): SpacedReviewDao

    companion object {
        private const val DB_NAME = "system_design_lab.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
