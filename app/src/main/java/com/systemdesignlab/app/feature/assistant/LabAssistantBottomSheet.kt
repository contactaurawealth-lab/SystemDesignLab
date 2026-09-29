package com.systemdesignlab.app.feature.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.ai.AiAssistantRepository
import com.systemdesignlab.app.data.ai.AiProviderType
import com.systemdesignlab.app.data.ai.CompactCourseContext
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user", "assistant", "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabAssistantBottomSheet(
    aiRepository: AiAssistantRepository,
    context: CompactCourseContext,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var userMessageInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "assistant",
                text = "Hello! I am Lab Assistant. I am loaded with context for '${context.lessonTitle ?: "System Design Foundations"}' from Karan Pratap Singh's repository. How can I help you architect?"
            )
        )
    }

    val quickActions = listOf(
        "Explain simply",
        "Explain technically",
        "Give a real-world analogy",
        "Give me a hint (no spoilers)",
        "Quiz me on this concept",
        "Analyze potential bottlenecks"
    )

    fun sendMessage(text: String, isHintMode: Boolean = false) {
        if (text.isBlank() || isLoading) return
        messages.add(ChatMessage(sender = "user", text = text))
        userMessageInput = ""
        isLoading = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)

            val updatedContext = context.copy(isHintMode = isHintMode)
            val result = aiRepository.askAssistant(
                prompt = text,
                providerType = AiProviderType.GEMINI,
                model = "gemini-1.5-flash",
                endpoint = AiProviderType.GEMINI.defaultEndpoint,
                temperature = 0.7f,
                maxTokens = 1000,
                context = updatedContext
            )

            isLoading = false
            result.fold(
                onSuccess = { reply ->
                    messages.add(ChatMessage(sender = "assistant", text = reply))
                },
                onFailure = { err ->
                    val errorMsg = if (err is IllegalStateException) {
                        "🔒 BYOK Setup Required: ${err.message}\n\nPlease tap below to configure your Google Gemini, OpenAI, or OpenRouter API key. All offline course content remains fully accessible."
                    } else {
                        "⚠️ Assistant Offline: ${err.localizedMessage ?: "Network connection error"}. Offline course content, 3D simulations, and exercises remain 100% available."
                    }
                    messages.add(ChatMessage(sender = "system", text = errorMsg))
                }
            )
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Lab Assistant",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Repository-Aware AI Tutor",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp)
                        )
                    }
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Slate400)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Context Indicator Pill
            context.lessonTitle?.let { title ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = AccentSky, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Context: $title",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentSky)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Action Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickActions) { action ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable {
                                val isHint = action.contains("hint", ignoreCase = true)
                                sendMessage(action, isHintMode = isHint)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = action, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "user"
                    val isSystem = msg.sender == "system"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isSystem) 1.0f else 0.85f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isUser -> AccentIndigo
                                        isSystem -> Color(0xFF332020)
                                        else -> Color(0xFF1E293B)
                                    }
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSystem) Color(0xFFFFD2D2) else Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                )
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentIndigo, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Formulating response with course context...", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMessageInput,
                    onValueChange = { userMessageInput = it },
                    placeholder = { Text("Ask anything about this concept...", fontSize = 13.sp, color = Slate400) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendMessage(userMessageInput) },
                    enabled = userMessageInput.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (userMessageInput.isNotBlank()) AccentIndigo else Slate800)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
