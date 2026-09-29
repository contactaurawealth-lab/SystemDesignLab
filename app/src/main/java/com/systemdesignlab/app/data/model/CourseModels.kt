package com.systemdesignlab.app.data.model

data class CourseModule(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val order: Int,
    val color: String,
    val totalLessons: Int = 0,
    val completedLessons: Int = 0
)

data class RepoReference(
    val filePath: String,
    val sectionHeading: String,
    val diagramFile: String?
)

data class InteractiveQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class LessonExercise(
    val id: String,
    val title: String,
    val type: String,
    val scenario: String,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class Lesson(
    val id: String,
    val title: String,
    val moduleId: String,
    val moduleName: String,
    val order: Int,
    val difficulty: String,
    val estimatedMinutes: Int,
    val problem: String,
    val simpleExplanation: String,
    val technicalExplanation: String,
    val visualExplanation: String,
    val realWorldAnalogy: String,
    val architectureDiagramId: String,
    val repoReference: RepoReference,
    val commonMistakes: String,
    val tradeOffs: String,
    val keyTakeaway: String,
    val relatedConcepts: List<String>,
    val prerequisites: List<String>,
    val nextLessonId: String?,
    val interactiveQuestion: InteractiveQuestion,
    val exercise: LessonExercise,
    val isCompleted: Boolean = false,
    val progressPercent: Int = 0,
    val isBookmarked: Boolean = false
)

data class Concept(
    val id: String,
    val name: String,
    val moduleId: String,
    val summary: String,
    val keyTakeaway: String,
    val difficulty: String,
    val diagramId: String
)

data class KnowledgeNode(
    val id: String,
    val label: String,
    val moduleId: String,
    val order: Int,
    val difficulty: String,
    val prerequisites: List<String>,
    val isUnlocked: Boolean = true,
    val isCompleted: Boolean = false
)

data class KnowledgeEdge(
    val from: String,
    val to: String,
    val relationship: String
)

data class LearningMapData(
    val nodes: List<KnowledgeNode>,
    val edges: List<KnowledgeEdge>
)

data class RepositoryItem(
    val id: String,
    val title: String,
    val slug: String,
    val category: String,
    val markdownFile: String,
    val section: String,
    val diagramFile: String?,
    val whatItTeaches: String,
    val relatedLessonId: String,
    val relatedExerciseId: String
)

data class SimulationItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val initialTraffic: Int = 1000,
    val maxTraffic: Int = 100000,
    val initialServers: Int = 2,
    val minServers: Int = 1,
    val maxServers: Int = 10,
    val learningPoints: List<String>,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0
)

data class InterviewStep(
    val stepNumber: Int,
    val title: String,
    val prompt: String,
    val expectedDecisions: List<String>
)

data class InterviewScenario(
    val id: String,
    val title: String,
    val difficulty: String,
    val traffic: String,
    val storage: String,
    val steps: List<InterviewStep>
)

data class VisualSyncPoint(
    val timestampSeconds: Int,
    val conceptId: String,
    val caption: String
)

data class AudiobookChapter(
    val id: String,
    val chapterNumber: Int,
    val title: String,
    val subtitle: String,
    val durationSeconds: Int,
    val audioFileName: String,
    val audioAssetPath: String,
    val visualSyncPoints: List<VisualSyncPoint>,
    val narration: String,
    val isCompleted: Boolean = false,
    val currentPositionMs: Long = 0L
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val icon: String,
    val isUnlocked: Boolean = false
)
