package com.systemdesignlab.app.feature.lesson

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.core.ui.components.ArchitectureDiagramCanvas
import com.systemdesignlab.app.core.ui.components.CodeViewer
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.model.Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lesson: Lesson,
    onBack: () -> Unit,
    onNextLesson: ((String) -> Unit)?,
    onCompleteLesson: () -> Unit,
    onAnswerExercise: (selectedOption: Int, isCorrect: Boolean) -> Unit,
    onOpenRepoSection: (String) -> Unit,
    onLaunchSim: (String) -> Unit,
    onOpenAiAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    var technicalExpanded by remember { mutableStateOf(false) }
    var selectedQuestionOption by remember { mutableStateOf<Int?>(null) }
    var showQuestionResult by remember { mutableStateOf(false) }

    var selectedExerciseOption by remember { mutableStateOf<Int?>(null) }
    var showExerciseResult by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "${lesson.difficulty} • ${lesson.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAiAssistant) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = "AI Assistant", tint = AccentIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (lesson.isCompleted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lesson Completed", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = onCompleteLesson,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Mark Complete (+30 XP)")
                        }
                    }

                    if (lesson.nextLessonId != null && onNextLesson != null) {
                        OutlinedButton(
                            onClick = { onNextLesson(lesson.nextLessonId) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next Lesson")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Problem Statement Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THE CORE ENGINEERING PROBLEM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentAmber, letterSpacing = 1.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = lesson.problem,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, lineHeight = 22.sp)
                        )
                    }
                }
            }

            // 2. Simple Intuitive Explanation
            item {
                Column {
                    Text(
                        text = "SIMPLE EXPLANATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentSky, letterSpacing = 1.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lesson.simpleExplanation,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground, lineHeight = 24.sp)
                    )
                }
            }

            // 3. Real-World Analogy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "REAL-WORLD ANALOGY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentAmber)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lesson.realWorldAnalogy,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                            )
                        }
                    }
                }
            }

            // 4. Interactive Architecture Diagram (Jetpack Compose Canvas)
            item {
                ArchitectureDiagramCanvas(diagramId = lesson.architectureDiagramId)
            }

            // 5. Deep Technical Explanation (Progressive Disclosure)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { technicalExpanded = !technicalExpanded },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DEEP TECHNICAL SPECIFICATION",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                            Icon(
                                imageVector = if (technicalExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle"
                            )
                        }

                        AnimatedVisibility(visible = technicalExpanded) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                HorizontalDivider(color = Slate800)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = lesson.technicalExplanation,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate400,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 6. Common Mistakes & Architectural Trade-offs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Common Mistakes
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = AccentRose, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Common Pitfalls", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentRose)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = lesson.commonMistakes, style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp))
                        }
                    }

                    // Trade-offs
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Balance, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trade-Offs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentIndigo)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = lesson.tradeOffs, style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp))
                        }
                    }
                }
            }

            // 7. Interactive Check-for-Understanding Question
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CHECK FOR UNDERSTANDING",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentEmerald, letterSpacing = 1.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lesson.interactiveQuestion.question,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        lesson.interactiveQuestion.options.forEachIndexed { idx, opt ->
                            val isSelected = selectedQuestionOption == idx
                            val isCorrect = idx == lesson.interactiveQuestion.correctOptionIndex
                            val bg = when {
                                showQuestionResult && isCorrect -> AccentEmerald.copy(alpha = 0.2f)
                                showQuestionResult && isSelected && !isCorrect -> AccentRose.copy(alpha = 0.2f)
                                isSelected -> AccentIndigo.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .clickable {
                                        if (!showQuestionResult) {
                                            selectedQuestionOption = idx
                                            showQuestionResult = true
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A'.code + idx).toChar()}.",
                                    fontWeight = FontWeight.Bold,
                                    color = if (showQuestionResult && isCorrect) AccentEmerald else Slate400
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }

                        if (showQuestionResult) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = lesson.interactiveQuestion.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (selectedQuestionOption == lesson.interactiveQuestion.correctOptionIndex) AccentEmerald else AccentRose,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // 8. Practical Engineering Exercise
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Construction, contentDescription = null, tint = AccentSky, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENGINEERING EXERCISE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentSky, letterSpacing = 1.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = lesson.exercise.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = lesson.exercise.scenario, style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = lesson.exercise.question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))
                        Spacer(modifier = Modifier.height(12.dp))

                        lesson.exercise.options.forEachIndexed { idx, opt ->
                            val isSelected = selectedExerciseOption == idx
                            val isCorrect = idx == lesson.exercise.correctOptionIndex
                            val bg = when {
                                showExerciseResult && isCorrect -> AccentEmerald.copy(alpha = 0.2f)
                                showExerciseResult && isSelected && !isCorrect -> AccentRose.copy(alpha = 0.2f)
                                isSelected -> AccentIndigo.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .clickable {
                                        if (!showExerciseResult) {
                                            selectedExerciseOption = idx
                                            showExerciseResult = true
                                            onAnswerExercise(idx, isCorrect)
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A'.code + idx).toChar()}.",
                                    fontWeight = FontWeight.Bold,
                                    color = if (showExerciseResult && isCorrect) AccentEmerald else Slate400
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }

                        if (showExerciseResult) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = lesson.exercise.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (selectedExerciseOption == lesson.exercise.correctOptionIndex) AccentEmerald else AccentRose,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // 9. Key Takeaway & Repository Reference Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "KEY ARCHITECTURAL TAKEAWAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentEmerald, letterSpacing = 1.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lesson.keyTakeaway,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Slate800)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenRepoSection(lesson.id) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Source, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "karanpratapsingh/system-design • ${lesson.repoReference.sectionHeading}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                )
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
