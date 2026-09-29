package com.systemdesignlab.app.feature.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.model.LessonExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    exercises: List<LessonExercise>,
    onCompleteExercise: (exerciseId: String, selectedIndex: Int, isCorrect: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val answeredMap = remember { mutableStateMapOf<String, Pair<Int, Boolean>>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Engineering Practice Lab", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Construction, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PRODUCTION DRILLS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentEmerald))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Solve real-world incident simulations and architectural trade-off problems. Earn +20 XP per solved exercise.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }
                }
            }

            items(exercises) { ex ->
                val answerState = answeredMap[ex.id]
                val selectedIdx = answerState?.first
                val isAnswered = answerState != null

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text(
                                text = ex.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (answerState?.second == true) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = ex.scenario, style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = ex.question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface))

                        Spacer(modifier = Modifier.height(12.dp))

                        ex.options.forEachIndexed { optIdx, optText ->
                            val isChosen = selectedIdx == optIdx
                            val isCorrect = optIdx == ex.correctOptionIndex

                            val bg = when {
                                isAnswered && isCorrect -> AccentEmerald.copy(alpha = 0.2f)
                                isAnswered && isChosen && !isCorrect -> AccentRose.copy(alpha = 0.2f)
                                isChosen -> AccentIndigo.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .clickable {
                                        if (!isAnswered) {
                                            val correct = optIdx == ex.correctOptionIndex
                                            answeredMap[ex.id] = Pair(optIdx, correct)
                                            onCompleteExercise(ex.id, optIdx, correct)
                                        }
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A'.code + optIdx).toChar()}.",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnswered && isCorrect) AccentEmerald else Slate400
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }

                        if (isAnswered) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ex.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (selectedIdx == ex.correctOptionIndex) AccentEmerald else AccentRose,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
