package com.systemdesignlab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.ai.CompactCourseContext
import com.systemdesignlab.app.feature.assistant.LabAssistantBottomSheet
import com.systemdesignlab.app.feature.audiobook.AudiobookScreen
import com.systemdesignlab.app.feature.exercises.ExercisesScreen
import com.systemdesignlab.app.feature.home.HomeScreen
import com.systemdesignlab.app.feature.interview.InterviewSimulatorScreen
import com.systemdesignlab.app.feature.learn.LearnScreen
import com.systemdesignlab.app.feature.learningmap.LearningMapScreen
import com.systemdesignlab.app.feature.lesson.LessonScreen
import com.systemdesignlab.app.feature.profile.ProfileScreen
import com.systemdesignlab.app.feature.repository.RepositoryExplorerScreen
import com.systemdesignlab.app.feature.settings.SettingsScreen
import com.systemdesignlab.app.feature.simulation.SimulationScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Learn : Screen("learn", "Learn", Icons.Default.MenuBook)
    object Simulate : Screen("simulate", "Simulate", Icons.Default.ViewInAr)
    object Listen : Screen("listen", "Listen", Icons.Default.Headphones)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Fullscreen sub-destinations
    object LessonDetail : Screen("lesson/{lessonId}", "Lesson", Icons.Default.MenuBook)
    object RepositoryExplorer : Screen("repository", "Explorer", Icons.Default.Folder)
    object LearningMap : Screen("learning_map", "Map", Icons.Default.AccountTree)
    object InterviewMode : Screen("interviews", "Interview", Icons.Default.RecordVoiceOver)
    object Exercises : Screen("exercises", "Exercises", Icons.Default.Construction)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SystemDesignLabApp
        val container = app.container

        setContent {
            val themeMode by container.preferencesManager.themeModeFlow.collectAsStateWithLifecycle(initialValue = "system")
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            SystemDesignLabTheme(darkTheme = isDark) {
                MainContent(container = container)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(container: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // State flows from repositories
    val modules by container.courseRepository.getModulesFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val allLessons by container.courseRepository.getAllLessonsFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val userProgress by container.progressRepository.userProgressFlow.collectAsStateWithLifecycle(initialValue = null)
    val learningMap by container.courseRepository.getLearningMapFlow().collectAsStateWithLifecycle(initialValue = com.systemdesignlab.app.data.model.LearningMapData(emptyList(), emptyList()))
    val repoItems by container.courseRepository.getRepositoryItemsFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val simulations by container.courseRepository.getSimulationsFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val audiobookChapters by container.courseRepository.getAudiobookChaptersFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val achievements by container.courseRepository.getAchievementsFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val dueReviews by container.progressRepository.getDueReviewsFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    val interviewScenarios = remember { container.courseRepository.getInterviewScenarios() }

    var selectedModuleId by remember { mutableStateOf("mod-foundations") }
    var showAiAssistantSheet by remember { mutableStateOf(false) }
    var activeAiContext by remember { mutableStateOf(CompactCourseContext()) }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Learn,
        Screen.Simulate,
        Screen.Listen,
        Screen.Profile
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "System Design Lab",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Source of Truth: karanpratapsingh/system-design",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Slate800)
                Spacer(Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    label = { Text("Repository Explorer (43 Diagrams)") },
                    selected = currentRoute == Screen.RepositoryExplorer.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.RepositoryExplorer.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                    label = { Text("Interactive Learning Map") },
                    selected = currentRoute == Screen.LearningMap.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.LearningMap.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null) },
                    label = { Text("Interview Simulator (5 Systems)") },
                    selected = currentRoute == Screen.InterviewMode.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.InterviewMode.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Construction, contentDescription = null) },
                    label = { Text("Engineering Practice Lab") },
                    selected = currentRoute == Screen.Exercises.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Exercises.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Slate800)

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings & BYOK AI") },
                    selected = currentRoute == Screen.Settings.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Settings.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showBottomBar) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "System Design Lab",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Screen.RepositoryExplorer.route) }) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = "Repo Explorer", tint = Slate400)
                            }
                            IconButton(onClick = {
                                activeAiContext = CompactCourseContext(lessonTitle = "System Design Foundations")
                                showAiAssistantSheet = true
                            }) {
                                Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Lab Assistant", tint = AccentIndigo)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                                label = { Text(item.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AccentIndigo,
                                    selectedTextColor = AccentIndigo,
                                    indicatorColor = AccentIndigo.copy(alpha = 0.15f),
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate400
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Home Screen
                composable(Screen.Home.route) {
                    HomeScreen(
                        streakDays = userProgress?.streakDays ?: 1,
                        totalXp = userProgress?.totalXp ?: 0,
                        completedLessonsCount = userProgress?.lessonsCompletedCount ?: 0,
                        completedSimsCount = userProgress?.simulationsCompletedCount ?: 0,
                        completedExercisesCount = userProgress?.exercisesCompletedCount ?: 0,
                        dailyGoalClaimed = userProgress?.dailyGoalCompletedToday ?: false,
                        recentLesson = allLessons.firstOrNull { !it.isCompleted } ?: allLessons.firstOrNull(),
                        onContinueLesson = { lessonId ->
                            navController.navigate("lesson/$lessonId")
                        },
                        onLaunchSimulation = { simId ->
                            navController.navigate(Screen.Simulate.route)
                        },
                        onResumeAudiobook = {
                            navController.navigate(Screen.Listen.route)
                        },
                        onOpenLearningMap = {
                            navController.navigate(Screen.LearningMap.route)
                        },
                        onClaimDailyReward = {
                            scope.launch { container.progressRepository.claimDailyGoalReward() }
                        }
                    )
                }

                // Learn Screen
                composable(Screen.Learn.route) {
                    LearnScreen(
                        modules = modules,
                        lessons = allLessons,
                        selectedModuleId = selectedModuleId,
                        onSelectModule = { selectedModuleId = it },
                        onOpenLesson = { lessonId ->
                            navController.navigate("lesson/$lessonId")
                        },
                        onToggleBookmark = { lesson ->
                            scope.launch {
                                container.courseRepository.toggleBookmark("lesson", lesson.id, lesson.title, lesson.keyTakeaway)
                            }
                        },
                        onSearch = { query ->
                            kotlinx.coroutines.runBlocking { container.courseRepository.searchContent(query) }
                        }
                    )
                }

                // Simulate Screen
                composable(Screen.Simulate.route) {
                    SimulationScreen(
                        simulations = simulations,
                        onCompleteScenario = { simId, score ->
                            scope.launch { container.courseRepository.recordSimulationProgress(simId, score) }
                        }
                    )
                }

                // Listen / Audiobook Screen
                composable(Screen.Listen.route) {
                    AudiobookScreen(
                        chapters = audiobookChapters,
                        audiobookManager = container.audiobookManager,
                        onJumpToLesson = { conceptId ->
                            navController.navigate("lesson/$conceptId")
                        },
                        onJumpToSimulation = { simId ->
                            navController.navigate(Screen.Simulate.route)
                        }
                    )
                }

                // Profile Screen
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        userProgress = userProgress,
                        achievements = achievements,
                        dueReviews = dueReviews,
                        progressRepository = container.progressRepository,
                        onOpenSettings = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

                // Lesson Screen
                composable(
                    route = Screen.LessonDetail.route,
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                    val lesson = allLessons.find { it.id == lessonId }

                    if (lesson != null) {
                        LessonScreen(
                            lesson = lesson,
                            onBack = { navController.popBackStack() },
                            onNextLesson = { nextId ->
                                navController.navigate("lesson/$nextId") {
                                    popUpTo("lesson/$lessonId") { inclusive = true }
                                }
                            },
                            onCompleteLesson = {
                                scope.launch { container.courseRepository.markLessonCompleted(lesson.id) }
                            },
                            onAnswerExercise = { optIdx, isCorrect ->
                                scope.launch { container.courseRepository.recordExerciseAttempt(lesson.exercise.id, optIdx, isCorrect) }
                            },
                            onOpenRepoSection = {
                                navController.navigate(Screen.RepositoryExplorer.route)
                            },
                            onLaunchSim = {
                                navController.navigate(Screen.Simulate.route)
                            },
                            onOpenAiAssistant = {
                                activeAiContext = CompactCourseContext(
                                    lessonTitle = lesson.title,
                                    lessonProblem = lesson.problem,
                                    lessonTradeoffs = lesson.tradeOffs,
                                    lessonTakeaway = lesson.keyTakeaway,
                                    conceptName = lesson.title,
                                    exerciseQuestion = lesson.exercise.question
                                )
                                showAiAssistantSheet = true
                            }
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Lesson not found.")
                        }
                    }
                }

                // Repository Explorer Screen
                composable(Screen.RepositoryExplorer.route) {
                    RepositoryExplorerScreen(
                        items = repoItems,
                        onOpenLesson = { lessonId ->
                            navController.navigate("lesson/$lessonId")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Learning Map Screen
                composable(Screen.LearningMap.route) {
                    LearningMapScreen(
                        mapData = learningMap,
                        onOpenLesson = { lessonId ->
                            navController.navigate("lesson/$lessonId")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Interview Simulator Screen
                composable(Screen.InterviewMode.route) {
                    InterviewSimulatorScreen(
                        scenarios = interviewScenarios,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Exercises Screen
                composable(Screen.Exercises.route) {
                    val allExercises = remember(allLessons) { allLessons.map { it.exercise } }
                    ExercisesScreen(
                        exercises = allExercises,
                        onCompleteExercise = { exId, optIdx, isCorrect ->
                            scope.launch { container.courseRepository.recordExerciseAttempt(exId, optIdx, isCorrect) }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Settings Screen
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        preferencesManager = container.preferencesManager,
                        aiAssistantRepository = container.aiAssistantRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Lab Assistant Modal Bottom Sheet
        if (showAiAssistantSheet) {
            LabAssistantBottomSheet(
                aiRepository = container.aiAssistantRepository,
                context = activeAiContext,
                onDismiss = { showAiAssistantSheet = false },
                onOpenSettings = {
                    showAiAssistantSheet = false
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    }
}
