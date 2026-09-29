package com.systemdesignlab.app.data.progress

import com.systemdesignlab.app.core.database.AppDatabase
import com.systemdesignlab.app.core.database.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ProgressRepository(
    private val database: AppDatabase
) {
    val userProgressFlow: Flow<UserProgressEntity?> = database.userProgressDao().getUserProgressFlow()

    suspend fun checkAndUpdateStreak() = withContext(Dispatchers.IO) {
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val user = database.userProgressDao().getUserProgress() ?: UserProgressEntity()

        if (user.lastActiveDateEpochDays == today) {
            // Already active today
            return@withContext
        }

        val newStreak = if (user.lastActiveDateEpochDays == today - 1) {
            user.streakDays + 1
        } else {
            1
        }

        database.userProgressDao().insertOrUpdate(
            user.copy(
                streakDays = newStreak,
                lastActiveDateEpochDays = today,
                dailyGoalCompletedToday = false
            )
        )
    }

    suspend fun claimDailyGoalReward(): Boolean = withContext(Dispatchers.IO) {
        val user = database.userProgressDao().getUserProgress() ?: return@withContext false
        if (user.dailyGoalCompletedToday) return@withContext false

        database.userProgressDao().insertOrUpdate(
            user.copy(
                totalXp = user.totalXp + 50,
                dailyGoalCompletedToday = true
            )
        )
        true
    }

    // Spaced repetition SM-2
    fun getDueReviewsFlow(): Flow<List<SpacedReviewEntity>> {
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        return database.spacedReviewDao().getDueReviewsFlow(today)
    }

    suspend fun recordReviewResult(conceptId: String, quality: Int) = withContext(Dispatchers.IO) {
        // quality: 0 (blackout) to 5 (perfect recall)
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val current = database.spacedReviewDao().getReview(conceptId) ?: SpacedReviewEntity(
            conceptId = conceptId,
            intervalDays = 1,
            repetitions = 0,
            easeFactor = 2.5f,
            nextReviewEpochDays = today + 1,
            lastReviewedEpochDays = today
        )

        var newRep = current.repetitions
        var newInterval = current.intervalDays
        var newEase = current.easeFactor

        if (quality >= 3) {
            newRep += 1
            newInterval = when (newRep) {
                1 -> 1
                2 -> 3
                3 -> 7
                else -> (newInterval * newEase).toInt().coerceAtLeast(newInterval + 1)
            }
        } else {
            newRep = 0
            newInterval = 1
        }

        newEase = (newEase + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))).coerceAtLeast(1.3f)

        database.spacedReviewDao().insertOrUpdate(
            current.copy(
                intervalDays = newInterval,
                repetitions = newRep,
                easeFactor = newEase,
                nextReviewEpochDays = today + newInterval,
                lastReviewedEpochDays = today
            )
        )
    }

    // Export progress as JSON
    suspend fun exportProgressJson(): String = withContext(Dispatchers.IO) {
        val user = database.userProgressDao().getUserProgress() ?: UserProgressEntity()
        val root = JSONObject().apply {
            put("version", 1)
            put("timestamp", System.currentTimeMillis())
            put("totalXp", user.totalXp)
            put("streakDays", user.streakDays)
            put("lessonsCompleted", user.lessonsCompletedCount)
            put("simulationsCompleted", user.simulationsCompletedCount)
            put("exercisesCompleted", user.exercisesCompletedCount)
        }
        root.toString(2)
    }

    // Import progress from JSON
    suspend fun importProgressJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            val xp = root.optInt("totalXp", 0)
            val streak = root.optInt("streakDays", 1)
            val lessons = root.optInt("lessonsCompleted", 0)
            val sims = root.optInt("simulationsCompleted", 0)
            val exs = root.optInt("exercisesCompleted", 0)

            val current = database.userProgressDao().getUserProgress() ?: UserProgressEntity()
            database.userProgressDao().insertOrUpdate(
                current.copy(
                    totalXp = xp,
                    streakDays = streak,
                    lessonsCompletedCount = lessons,
                    simulationsCompletedCount = sims,
                    exercisesCompletedCount = exs
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}
