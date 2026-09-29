package com.systemdesignlab.app.feature.interview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.model.InterviewScenario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewSimulatorScreen(
    scenarios: List<InterviewScenario>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScenarioId by remember { mutableStateOf(scenarios.firstOrNull()?.id ?: "") }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var selectedDecisions by remember { mutableStateOf(setOf<Int>()) }
    var stepFeedbackSubmitted by remember { mutableStateOf(false) }

    val activeScenario = scenarios.find { it.id == selectedScenarioId } ?: scenarios.firstOrNull()
    val activeStep = activeScenario?.steps?.getOrNull(currentStepIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("System Design Interview", fontWeight = FontWeight.Bold)
                        Text(activeScenario?.title ?: "", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                    }
                },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Scenario Selection Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scenarios) { sc ->
                    val isSelected = sc.id == selectedScenarioId
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedScenarioId = sc.id
                            currentStepIndex = 0
                            selectedDecisions = emptySet()
                            stepFeedbackSubmitted = false
                        },
                        label = { Text(sc.title.replace("Design a ", "").take(18) + "...", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentIndigo,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Constraints & SLA Banner
                activeScenario?.let { sc ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("CONSTRAINTS & SCALE", style = MaterialTheme.typography.labelSmall.copy(color = AccentSky, fontWeight = FontWeight.Bold))
                                    Text(sc.difficulty.uppercase(), color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Traffic: ${sc.traffic}", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
                                Text("Storage: ${sc.storage}", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
                            }
                        }
                    }
                }

                // Step Progress Indicator
                item {
                    val totalSteps = activeScenario?.steps?.size ?: 1
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "STEP ${currentStepIndex + 1} OF $totalSteps: ${activeStep?.title?.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentEmerald, letterSpacing = 1.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (currentStepIndex + 1).toFloat() / totalSteps },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentEmerald,
                            trackColor = Slate800
                        )
                    }
                }

                // Interviewer Prompt Card
                activeStep?.let { step ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "INTERVIEWER PROMPT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentIndigo)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = step.prompt,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "SELECT ARCHITECTURAL DECISIONS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate400)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                step.expectedDecisions.forEachIndexed { idx, decision ->
                                    val isSelected = selectedDecisions.contains(idx)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) AccentIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                selectedDecisions = if (isSelected) selectedDecisions - idx else selectedDecisions + idx
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isSelected) AccentIndigo else Slate400,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = decision, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val total = activeScenario?.steps?.size ?: 1
                                        if (currentStepIndex < total - 1) {
                                            currentStepIndex++
                                            selectedDecisions = emptySet()
                                        } else {
                                            stepFeedbackSubmitted = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    val isLast = currentStepIndex == (activeScenario?.steps?.size ?: 1) - 1
                                    Text(if (isLast) "Finish Interview Design" else "Confirm & Next Step")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                if (stepFeedbackSubmitted) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("INTERVIEW ASSESSMENT: PASSED", fontWeight = FontWeight.Bold, color = AccentEmerald)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Excellent architectural composition! You addressed high QPS, storage partitioning, caching layers, and fault-tolerance SLAs systematically.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
