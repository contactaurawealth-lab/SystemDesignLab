package com.systemdesignlab.app

import android.app.Application
import com.systemdesignlab.app.core.audio.AudiobookManager
import com.systemdesignlab.app.core.database.AppDatabase
import com.systemdesignlab.app.core.datastore.PreferencesManager
import com.systemdesignlab.app.core.security.KeystoreSecretManager
import com.systemdesignlab.app.data.ai.AiAssistantRepository
import com.systemdesignlab.app.data.course.CourseRepository
import com.systemdesignlab.app.data.progress.ProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(private val application: Application) {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(application)
    }

    val keystoreSecretManager: KeystoreSecretManager by lazy {
        KeystoreSecretManager(application)
    }

    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(application)
    }

    val courseRepository: CourseRepository by lazy {
        CourseRepository(application, database)
    }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepository(database)
    }

    val aiAssistantRepository: AiAssistantRepository by lazy {
        AiAssistantRepository(keystoreSecretManager)
    }

    val audiobookManager: AudiobookManager by lazy {
        AudiobookManager(application) { chapterId, pos, dur, isCompleted ->
            courseRepository.recordAudioProgress(chapterId, pos, dur, isCompleted)
        }
    }
}

class SystemDesignLabApp : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Initialize streak and daily tracking
        applicationScope.launch {
            container.progressRepository.checkAndUpdateStreak()
        }
    }
}
