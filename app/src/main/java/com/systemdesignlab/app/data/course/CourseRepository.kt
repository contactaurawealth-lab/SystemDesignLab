package com.systemdesignlab.app.data.course

import android.content.Context
import com.systemdesignlab.app.core.database.AppDatabase
import com.systemdesignlab.app.core.database.entity.*
import com.systemdesignlab.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CourseRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    // In-memory caches parsed from assets for instant zero-latency access
    private var cachedModules: List<CourseModule> = emptyList()
    private var cachedLessons: List<Lesson> = emptyList()
    private var cachedConcepts: List<Concept> = emptyList()
    private var cachedLearningMap: LearningMapData = LearningMapData(emptyList(), emptyList())
    private var cachedRepoItems: List<RepositoryItem> = emptyList()
    private var cachedSimulations: List<SimulationItem> = emptyList()
    private var cachedInterviews: List<InterviewScenario> = emptyList()
    private var cachedAudiobook: List<AudiobookChapter> = emptyList()
    private var cachedAchievements: List<AchievementItem> = emptyList()

    private var isInitialized = false

    private suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        // Load modules
        val modulesJson = loadAsset("course/modules.json")
        val modArray = JSONArray(modulesJson)
        val mods = mutableListOf<CourseModule>()
        for (i in 0 until modArray.length()) {
            val obj = modArray.getJSONObject(i)
            mods.add(
                CourseModule(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.getString("description"),
                    icon = obj.getString("icon"),
                    order = obj.getInt("order"),
                    color = obj.getString("color")
                )
            )
        }
        cachedModules = mods

        // Load lessons
        val lessonsJson = loadAsset("course/lessons.json")
        val lessonArray = JSONArray(lessonsJson)
        val lss = mutableListOf<Lesson>()
        for (i in 0 until lessonArray.length()) {
            val obj = lessonArray.getJSONObject(i)
            val refObj = obj.getJSONObject("repoReference")
            val qObj = obj.getJSONObject("interactiveQuestion")
            val exObj = obj.getJSONObject("exercise")

            val related = mutableListOf<String>()
            val relArr = obj.optJSONArray("relatedConcepts")
            if (relArr != null) {
                for (r in 0 until relArr.length()) related.add(relArr.getString(r))
            }

            val prereqs = mutableListOf<String>()
            val preArr = obj.optJSONArray("prerequisites")
            if (preArr != null) {
                for (p in 0 until preArr.length()) prereqs.add(preArr.getString(p))
            }

            val qOptions = mutableListOf<String>()
            val qOptArr = qObj.getJSONArray("options")
            for (o in 0 until qOptArr.length()) qOptions.add(qOptArr.getString(o))

            val exOptions = mutableListOf<String>()
            val exOptArr = exObj.getJSONArray("options")
            for (o in 0 until exOptArr.length()) exOptions.add(exOptArr.getString(o))

            lss.add(
                Lesson(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    moduleId = obj.getString("moduleId"),
                    moduleName = obj.getString("moduleName"),
                    order = obj.getInt("order"),
                    difficulty = obj.getString("difficulty"),
                    estimatedMinutes = obj.getInt("estimatedMinutes"),
                    problem = obj.getString("problem"),
                    simpleExplanation = obj.getString("simpleExplanation"),
                    technicalExplanation = obj.getString("technicalExplanation"),
                    visualExplanation = obj.getString("visualExplanation"),
                    realWorldAnalogy = obj.getString("realWorldAnalogy"),
                    architectureDiagramId = obj.getString("architectureDiagramId"),
                    repoReference = RepoReference(
                        filePath = refObj.getString("filePath"),
                        sectionHeading = refObj.getString("sectionHeading"),
                        diagramFile = refObj.optString("diagramFile", null)
                    ),
                    commonMistakes = obj.getString("commonMistakes"),
                    tradeOffs = obj.getString("tradeOffs"),
                    keyTakeaway = obj.getString("keyTakeaway"),
                    relatedConcepts = related,
                    prerequisites = prereqs,
                    nextLessonId = obj.optString("nextLessonId", null),
                    interactiveQuestion = InteractiveQuestion(
                        id = qObj.getString("id"),
                        question = qObj.getString("question"),
                        options = qOptions,
                        correctOptionIndex = qObj.getInt("correctOptionIndex"),
                        explanation = qObj.getString("explanation")
                    ),
                    exercise = LessonExercise(
                        id = exObj.getString("id"),
                        title = exObj.getString("title"),
                        type = exObj.getString("type"),
                        scenario = exObj.getString("scenario"),
                        question = exObj.getString("question"),
                        options = exOptions,
                        correctOptionIndex = exObj.getInt("correctOptionIndex"),
                        explanation = exObj.getString("explanation")
                    )
                )
            )
        }
        cachedLessons = lss

        // Load concepts
        val conceptsJson = loadAsset("course/concepts.json")
        val concArray = JSONArray(conceptsJson)
        val concs = mutableListOf<Concept>()
        for (i in 0 until concArray.length()) {
            val obj = concArray.getJSONObject(i)
            concs.add(
                Concept(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    moduleId = obj.getString("moduleId"),
                    summary = obj.getString("summary"),
                    keyTakeaway = obj.getString("keyTakeaway"),
                    difficulty = obj.getString("difficulty"),
                    diagramId = obj.getString("diagramId")
                )
            )
        }
        cachedConcepts = concs

        // Load learning map
        val mapJson = loadAsset("course/learning_map.json")
        val mapObj = JSONObject(mapJson)
        val nodeArr = mapObj.getJSONArray("nodes")
        val edgeArr = mapObj.getJSONArray("edges")
        val nodes = mutableListOf<KnowledgeNode>()
        for (i in 0 until nodeArr.length()) {
            val n = nodeArr.getJSONObject(i)
            val pre = mutableListOf<String>()
            val pArr = n.optJSONArray("prerequisites")
            if (pArr != null) {
                for (p in 0 until pArr.length()) pre.add(pArr.getString(p))
            }
            nodes.add(
                KnowledgeNode(
                    id = n.getString("id"),
                    label = n.getString("label"),
                    moduleId = n.getString("moduleId"),
                    order = n.getInt("order"),
                    difficulty = n.getString("difficulty"),
                    prerequisites = pre
                )
            )
        }
        val edges = mutableListOf<KnowledgeEdge>()
        for (i in 0 until edgeArr.length()) {
            val e = edgeArr.getJSONObject(i)
            edges.add(
                KnowledgeEdge(
                    from = e.getString("from"),
                    to = e.getString("to"),
                    relationship = e.getString("relationship")
                )
            )
        }
        cachedLearningMap = LearningMapData(nodes, edges)

        // Load repository items
        val repoJson = loadAsset("course/repository_explorer.json")
        val rObj = JSONObject(repoJson)
        val filesArr = rObj.getJSONArray("files")
        val rItems = mutableListOf<RepositoryItem>()
        for (i in 0 until filesArr.length()) {
            val item = filesArr.getJSONObject(i)
            rItems.add(
                RepositoryItem(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    slug = item.getString("slug"),
                    category = item.getString("category"),
                    markdownFile = item.getString("markdownFile"),
                    section = item.getString("section"),
                    diagramFile = item.optString("diagramFile", null),
                    whatItTeaches = item.getString("whatItTeaches"),
                    relatedLessonId = item.getString("relatedLessonId"),
                    relatedExerciseId = item.getString("relatedExerciseId")
                )
            )
        }
        cachedRepoItems = rItems

        // Load simulations
        val simJson = loadAsset("course/simulations.json")
        val simArr = JSONArray(simJson)
        val sims = mutableListOf<SimulationItem>()
        for (i in 0 until simArr.length()) {
            val s = simArr.getJSONObject(i)
            val lp = mutableListOf<String>()
            val lpArr = s.optJSONArray("learningPoints")
            if (lpArr != null) {
                for (l in 0 until lpArr.length()) lp.add(lpArr.getString(l))
            }
            sims.add(
                SimulationItem(
                    id = s.getString("id"),
                    title = s.getString("title"),
                    category = s.getString("category"),
                    description = s.getString("description"),
                    initialTraffic = s.optInt("initialTraffic", 1000),
                    maxTraffic = s.optInt("maxTraffic", 100000),
                    initialServers = s.optInt("initialServers", 2),
                    minServers = s.optInt("minServers", 1),
                    maxServers = s.optInt("maxServers", 10),
                    learningPoints = lp
                )
            )
        }
        cachedSimulations = sims

        // Load interviews
        val intJson = loadAsset("course/interviews.json")
        val intArr = JSONArray(intJson)
        val ints = mutableListOf<InterviewScenario>()
        for (i in 0 until intArr.length()) {
            val iv = intArr.getJSONObject(i)
            val stepsArr = iv.getJSONArray("steps")
            val steps = mutableListOf<InterviewStep>()
            for (st in 0 until stepsArr.length()) {
                val stepObj = stepsArr.getJSONObject(st)
                val decArr = stepObj.getJSONArray("expectedDecisions")
                val decisions = mutableListOf<String>()
                for (d in 0 until decArr.length()) decisions.add(decArr.getString(d))
                steps.add(
                    InterviewStep(
                        stepNumber = stepObj.getInt("stepNumber"),
                        title = stepObj.getString("title"),
                        prompt = stepObj.getString("prompt"),
                        expectedDecisions = decisions
                    )
                )
            }
            ints.add(
                InterviewScenario(
                    id = iv.getString("id"),
                    title = iv.getString("title"),
                    difficulty = iv.getString("difficulty"),
                    traffic = iv.getString("traffic"),
                    storage = iv.getString("storage"),
                    steps = steps
                )
            )
        }
        cachedInterviews = ints

        // Load audiobook
        val audioJson = loadAsset("course/audiobook.json")
        val audioArr = JSONArray(audioJson)
        val audios = mutableListOf<AudiobookChapter>()
        for (i in 0 until audioArr.length()) {
            val a = audioArr.getJSONObject(i)
            val vArr = a.getJSONArray("visualSyncPoints")
            val syncs = mutableListOf<VisualSyncPoint>()
            for (v in 0 until vArr.length()) {
                val vo = vArr.getJSONObject(v)
                syncs.add(
                    VisualSyncPoint(
                        timestampSeconds = vo.getInt("timestampSeconds"),
                        conceptId = vo.getString("conceptId"),
                        caption = vo.getString("caption")
                    )
                )
            }
            audios.add(
                AudiobookChapter(
                    id = a.getString("id"),
                    chapterNumber = a.getInt("chapterNumber"),
                    title = a.getString("title"),
                    subtitle = a.getString("subtitle"),
                    durationSeconds = a.getInt("durationSeconds"),
                    audioFileName = a.getString("audioFileName"),
                    audioAssetPath = a.getString("audioAssetPath"),
                    visualSyncPoints = syncs,
                    narration = a.getString("narration")
                )
            )
        }
        cachedAudiobook = audios

        // Load achievements
        val achJson = loadAsset("course/achievements.json")
        val achArr = JSONArray(achJson)
        val achs = mutableListOf<AchievementItem>()
        for (i in 0 until achArr.length()) {
            val a = achArr.getJSONObject(i)
            achs.add(
                AchievementItem(
                    id = a.getString("id"),
                    title = a.getString("title"),
                    description = a.getString("description"),
                    xpReward = a.getInt("xpReward"),
                    icon = a.getString("icon")
                )
            )
        }
        cachedAchievements = achs

        isInitialized = true
    }

    private fun loadAsset(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    fun getModulesFlow(): Flow<List<CourseModule>> = flow {
        ensureInitialized()
        emitAll(
            database.lessonDao().getAllProgressFlow().map { progressList ->
                val progressMap = progressList.associateBy { it.lessonId }
                cachedModules.map { mod ->
                    val modLessons = cachedLessons.filter { it.moduleId == mod.id }
                    val completed = modLessons.count { progressMap[it.id]?.isCompleted == true }
                    mod.copy(
                        totalLessons = modLessons.size,
                        completedLessons = completed
                    )
                }
            }
        )
    }

    fun getAllLessonsFlow(): Flow<List<Lesson>> = flow {
        ensureInitialized()
        emitAll(
            combine(
                database.lessonDao().getAllProgressFlow(),
                database.bookmarkDao().getAllBookmarksFlow()
            ) { progressList, bookmarks ->
                val progressMap = progressList.associateBy { it.lessonId }
                val bookmarkSet = bookmarks.map { it.itemId }.toSet()
                cachedLessons.map { l ->
                    val p = progressMap[l.id]
                    l.copy(
                        isCompleted = p?.isCompleted ?: false,
                        progressPercent = p?.progressPercent ?: 0,
                        isBookmarked = bookmarkSet.contains(l.id)
                    )
                }
            }
        )
    }

    fun getLessonsForModuleFlow(moduleId: String): Flow<List<Lesson>> = flow {
        ensureInitialized()
        emitAll(
            getAllLessonsFlow().map { lessons ->
                lessons.filter { it.moduleId == moduleId }
            }
        )
    }

    fun getLessonFlow(lessonId: String): Flow<Lesson?> = flow {
        ensureInitialized()
        emitAll(
            getAllLessonsFlow().map { lessons ->
                lessons.find { it.id == lessonId }
            }
        )
    }

    fun getLearningMapFlow(): Flow<LearningMapData> = flow {
        ensureInitialized()
        emitAll(
            database.lessonDao().getAllProgressFlow().map { progressList ->
                val completedIds = progressList.filter { it.isCompleted }.map { it.lessonId }.toSet()
                val updatedNodes = cachedLearningMap.nodes.map { node ->
                    val allPrereqsMet = node.prerequisites.all { completedIds.contains(it) }
                    node.copy(
                        isCompleted = completedIds.contains(node.id),
                        isUnlocked = node.prerequisites.isEmpty() || allPrereqsMet
                    )
                }
                LearningMapData(updatedNodes, cachedLearningMap.edges)
            }
        )
    }

    fun getRepositoryItemsFlow(): Flow<List<RepositoryItem>> = flow {
        ensureInitialized()
        emit(cachedRepoItems)
    }

    fun getSimulationsFlow(): Flow<List<SimulationItem>> = flow {
        ensureInitialized()
        emitAll(
            database.simulationDao().getAllProgressFlow().map { progressList ->
                val progressMap = progressList.associateBy { it.simulationId }
                cachedSimulations.map { sim ->
                    val p = progressMap[sim.id]
                    sim.copy(
                        isCompleted = p?.isCompleted ?: false,
                        bestScore = p?.bestScore ?: 0
                    )
                }
            }
        )
    }

    fun getAudiobookChaptersFlow(): Flow<List<AudiobookChapter>> = flow {
        ensureInitialized()
        emitAll(
            database.audiobookDao().getAllProgressFlow().map { progressList ->
                val progressMap = progressList.associateBy { it.chapterId }
                cachedAudiobook.map { ch ->
                    val p = progressMap[ch.id]
                    ch.copy(
                        isCompleted = p?.isCompleted ?: false,
                        currentPositionMs = p?.positionMs ?: 0L
                    )
                }
            }
        )
    }

    fun getAchievementsFlow(): Flow<List<AchievementItem>> = flow {
        ensureInitialized()
        emit(cachedAchievements)
    }

    fun getInterviewScenarios(): List<InterviewScenario> {
        return cachedInterviews
    }

    suspend fun markLessonCompleted(lessonId: String, xpAward: Int = 30) = withContext(Dispatchers.IO) {
        val existing = database.lessonDao().getProgress(lessonId)
        val wasAlreadyCompleted = existing?.isCompleted == true

        database.lessonDao().insertOrUpdate(
            LessonProgressEntity(
                lessonId = lessonId,
                isCompleted = true,
                progressPercent = 100,
                lastAccessedAt = System.currentTimeMillis(),
                completedAt = System.currentTimeMillis()
            )
        )

        // Award XP & streak if newly completed
        if (!wasAlreadyCompleted) {
            val user = database.userProgressDao().getUserProgress() ?: UserProgressEntity()
            database.userProgressDao().insertOrUpdate(
                user.copy(
                    totalXp = user.totalXp + xpAward,
                    lessonsCompletedCount = user.lessonsCompletedCount + 1
                )
            )
        }
    }

    suspend fun recordExerciseAttempt(exerciseId: String, selectedIndex: Int, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        val existing = database.exerciseDao().getAttempt(exerciseId)
        val attempts = (existing?.attemptsCount ?: 0) + 1

        database.exerciseDao().insertOrUpdate(
            ExerciseAttemptEntity(
                exerciseId = exerciseId,
                isCompleted = true,
                selectedOptionIndex = selectedIndex,
                isCorrect = isCorrect,
                attemptsCount = attempts,
                lastAttemptAt = System.currentTimeMillis()
            )
        )

        if (isCorrect && existing?.isCorrect != true) {
            val user = database.userProgressDao().getUserProgress() ?: UserProgressEntity()
            database.userProgressDao().insertOrUpdate(
                user.copy(
                    totalXp = user.totalXp + 20,
                    exercisesCompletedCount = user.exercisesCompletedCount + 1
                )
            )
        }
    }

    suspend fun recordSimulationProgress(simId: String, score: Int) = withContext(Dispatchers.IO) {
        val existing = database.simulationDao().getProgress(simId)
        val best = maxOf(existing?.bestScore ?: 0, score)
        val wasCompleted = existing?.isCompleted == true

        database.simulationDao().insertOrUpdate(
            SimulationProgressEntity(
                simulationId = simId,
                isCompleted = true,
                bestScore = best,
                lastRunAt = System.currentTimeMillis(),
                completedAt = System.currentTimeMillis()
            )
        )

        if (!wasCompleted) {
            val user = database.userProgressDao().getUserProgress() ?: UserProgressEntity()
            database.userProgressDao().insertOrUpdate(
                user.copy(
                    totalXp = user.totalXp + 40,
                    simulationsCompletedCount = user.simulationsCompletedCount + 1
                )
            )
        }
    }

    suspend fun recordAudioProgress(chapterId: String, posMs: Long, durMs: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        database.audiobookDao().insertOrUpdate(
            AudiobookProgressEntity(
                chapterId = chapterId,
                positionMs = posMs,
                totalDurationMs = durMs,
                isCompleted = isCompleted,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleBookmark(itemType: String, itemId: String, title: String, subtitle: String) = withContext(Dispatchers.IO) {
        val exists = database.bookmarkDao().getAllBookmarksFlow().first().any { it.itemId == itemId }
        if (exists) {
            database.bookmarkDao().deleteByItemId(itemId)
        } else {
            database.bookmarkDao().insert(
                BookmarkEntity(
                    id = "bm-$itemId",
                    itemType = itemType,
                    itemId = itemId,
                    title = title,
                    subtitle = subtitle
                )
            )
        }
    }

    fun isBookmarkedFlow(itemId: String): Flow<Boolean> = database.bookmarkDao().isBookmarkedFlow(itemId)

    suspend fun searchContent(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        ensureInitialized()
        if (query.isBlank()) return@withContext emptyList()
        val q = query.lowercase().trim()

        val results = mutableListOf<SearchResult>()
        // Search lessons
        cachedLessons.forEach { l ->
            if (l.title.lowercase().contains(q) || l.keyTakeaway.lowercase().contains(q) || l.simpleExplanation.lowercase().contains(q)) {
                results.add(
                    SearchResult(
                        id = l.id,
                        title = l.title,
                        subtitle = l.keyTakeaway,
                        category = "Lesson",
                        targetId = l.id
                    )
                )
            }
        }
        // Search concepts
        cachedConcepts.forEach { c ->
            if (c.name.lowercase().contains(q) || c.summary.lowercase().contains(q)) {
                results.add(
                    SearchResult(
                        id = "c-${c.id}",
                        title = c.name,
                        subtitle = c.summary,
                        category = "Concept",
                        targetId = c.id
                    )
                )
            }
        }
        // Search repo items
        cachedRepoItems.forEach { r ->
            if (r.title.lowercase().contains(q) || r.whatItTeaches.lowercase().contains(q)) {
                results.add(
                    SearchResult(
                        id = r.id,
                        title = r.title,
                        subtitle = r.whatItTeaches,
                        category = "Repository File",
                        targetId = r.relatedLessonId
                    )
                )
            }
        }
        results
    }
}

data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val targetId: String
)
