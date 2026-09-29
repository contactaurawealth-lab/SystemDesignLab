package com.systemdesignlab.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val isCompleted: Boolean,
    val progressPercent: Int, // 0 to 100
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "simulation_progress")
data class SimulationProgressEntity(
    @PrimaryKey val simulationId: String,
    val isCompleted: Boolean,
    val bestScore: Int,
    val lastRunAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "exercise_attempts")
data class ExerciseAttemptEntity(
    @PrimaryKey val exerciseId: String,
    val isCompleted: Boolean,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val attemptsCount: Int,
    val lastAttemptAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audiobook_progress")
data class AudiobookProgressEntity(
    @PrimaryKey val chapterId: String,
    val positionMs: Long,
    val totalDurationMs: Long,
    val isCompleted: Boolean,
    val lastPlayedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 0,
    val streakDays: Int = 1,
    val lastActiveDateEpochDays: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24),
    val lessonsCompletedCount: Int = 0,
    val simulationsCompletedCount: Int = 0,
    val exercisesCompletedCount: Int = 0,
    val totalListeningMinutes: Int = 0,
    val dailyGoalCompletedToday: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val itemType: String, // "lesson", "concept", "diagram", "exercise"
    val itemId: String,
    val title: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "spaced_reviews")
data class SpacedReviewEntity(
    @PrimaryKey val conceptId: String,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f, // Standard SM-2 ease factor
    val nextReviewEpochDays: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24) + 1,
    val lastReviewedEpochDays: Long = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
)
