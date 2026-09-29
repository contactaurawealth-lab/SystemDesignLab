package com.systemdesignlab.app.core.database.dao

import androidx.room.*
import com.systemdesignlab.app.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lesson_progress")
    fun getAllProgressFlow(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getProgress(lessonId: String): LessonProgressEntity?

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    fun getProgressFlow(lessonId: String): Flow<LessonProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: LessonProgressEntity)

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE isCompleted = 1")
    fun getCompletedCountFlow(): Flow<Int>
}

@Dao
interface SimulationDao {
    @Query("SELECT * FROM simulation_progress")
    fun getAllProgressFlow(): Flow<List<SimulationProgressEntity>>

    @Query("SELECT * FROM simulation_progress WHERE simulationId = :simId LIMIT 1")
    suspend fun getProgress(simId: String): SimulationProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: SimulationProgressEntity)

    @Query("SELECT COUNT(*) FROM simulation_progress WHERE isCompleted = 1")
    fun getCompletedCountFlow(): Flow<Int>
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_attempts")
    fun getAllAttemptsFlow(): Flow<List<ExerciseAttemptEntity>>

    @Query("SELECT * FROM exercise_attempts WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getAttempt(exerciseId: String): ExerciseAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ExerciseAttemptEntity)

    @Query("SELECT COUNT(*) FROM exercise_attempts WHERE isCorrect = 1")
    fun getCompletedCountFlow(): Flow<Int>
}

@Dao
interface AudiobookDao {
    @Query("SELECT * FROM audiobook_progress")
    fun getAllProgressFlow(): Flow<List<AudiobookProgressEntity>>

    @Query("SELECT * FROM audiobook_progress WHERE chapterId = :chapterId LIMIT 1")
    suspend fun getProgress(chapterId: String): AudiobookProgressEntity?

    @Query("SELECT * FROM audiobook_progress WHERE chapterId = :chapterId LIMIT 1")
    fun getProgressFlow(chapterId: String): Flow<AudiobookProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: AudiobookProgressEntity)
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgressFlow(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgress(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: UserProgressEntity)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE itemId = :itemId)")
    fun isBookmarkedFlow(itemId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE itemId = :itemId")
    suspend fun deleteByItemId(itemId: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE lessonId = :lessonId ORDER BY updatedAt DESC")
    fun getNotesForLessonFlow(lessonId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface SpacedReviewDao {
    @Query("SELECT * FROM spaced_reviews WHERE nextReviewEpochDays <= :todayEpochDays ORDER BY nextReviewEpochDays ASC")
    fun getDueReviewsFlow(todayEpochDays: Long): Flow<List<SpacedReviewEntity>>

    @Query("SELECT * FROM spaced_reviews WHERE conceptId = :conceptId LIMIT 1")
    suspend fun getReview(conceptId: String): SpacedReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: SpacedReviewEntity)
}
