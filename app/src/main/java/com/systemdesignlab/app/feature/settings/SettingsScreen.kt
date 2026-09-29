package com.systemdesignlab.app.feature.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.core.datastore.PreferencesManager
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.ai.AiAssistantRepository
import com.systemdesignlab.app.data.ai.AiProviderType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    aiAssistantRepository: AiAssistantRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentTheme by preferencesManager.themeModeFlow.collectAsState(initial = "system")
    val currentProviderStr by preferencesManager.selectedAiProviderFlow.collectAsState(initial = "gemini")
    val currentModel by preferencesManager.selectedAiModelFlow.collectAsState(initial = "gemini-1.5-flash")
    val currentEndpoint by preferencesManager.customAiEndpointFlow.collectAsState(initial = "https://generativelanguage.googleapis.com")
    val currentTargetXp by preferencesManager.dailyGoalTargetXpFlow.collectAsState(initial = 50)

    val currentProvider = remember(currentProviderStr) {
        try {
            AiProviderType.valueOf(currentProviderStr.uppercase())
        } catch (e: Exception) {
            AiProviderType.GEMINI
        }
    }

    var selectedProvider by remember(currentProvider) { mutableStateOf(currentProvider) }
    var endpointInput by remember(currentEndpoint) { mutableStateOf(currentEndpoint) }
    var modelInput by remember(currentModel) { mutableStateOf(currentModel) }
    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }

    val hasKey = remember(selectedProvider) { aiAssistantRepository.hasKeyForProvider(selectedProvider) }
    val maskedKey = remember(selectedProvider) { aiAssistantRepository.getMaskedKeyForProvider(selectedProvider) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultStatus by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Appearance / Theme
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "APPEARANCE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("system" to "System", "dark" to "Dark", "light" to "Light").forEach { (mode, label) ->
                                val isSelected = currentTheme == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        coroutineScope.launch { preferencesManager.setThemeMode(mode) }
                                    },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentIndigo,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. BYOK AI Assistant Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LAB ASSISTANT (BYOK)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = AccentIndigo)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x2610B981))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("OPTIONAL", color = AccentEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Provide your own API key to ask custom questions. Keys are encrypted with hardware-backed Android Keystore and never stored in plain text.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Provider Selector
                        Text("AI Provider", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate400))
                        Spacer(modifier = Modifier.height(6.dp))

                        AiProviderType.values().forEach { prov ->
                            val isSelected = selectedProvider == prov
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        selectedProvider = prov
                                        endpointInput = prov.defaultEndpoint
                                        modelInput = prov.defaultModel
                                        coroutineScope.launch {
                                            preferencesManager.setAiProvider(prov.name.lowercase())
                                            preferencesManager.setCustomAiEndpoint(prov.defaultEndpoint)
                                            preferencesManager.setAiModel(prov.defaultModel)
                                        }
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = prov.displayName, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Endpoint Input
                        OutlinedTextField(
                            value = endpointInput,
                            onValueChange = {
                                endpointInput = it
                                coroutineScope.launch { preferencesManager.setCustomAiEndpoint(it) }
                            },
                            label = { Text("API Endpoint") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Model Input
                        OutlinedTextField(
                            value = modelInput,
                            onValueChange = {
                                modelInput = it
                                coroutineScope.launch { preferencesManager.setAiModel(it) }
                            },
                            label = { Text("Model Identifier") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // API Key Input (Encrypted Keystore)
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text(if (hasKey) "Stored: $maskedKey" else "API Key (Keystore Encrypted)") },
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Save, Test, Clear
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (apiKeyInput.isNotBlank()) {
                                        aiAssistantRepository.saveKeyForProvider(selectedProvider, apiKeyInput)
                                        apiKeyInput = ""
                                        Toast.makeText(context, "Key securely saved in Keystore", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = apiKeyInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Key", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    isTestingConnection = true
                                    testResultStatus = null
                                    coroutineScope.launch {
                                        val res = aiAssistantRepository.testConnection(selectedProvider, modelInput, endpointInput)
                                        isTestingConnection = false
                                        res.fold(
                                            onSuccess = { testResultStatus = "✓ Connection Successful" },
                                            onFailure = { testResultStatus = "✗ Failed: ${it.localizedMessage}" }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test Connection", fontSize = 11.sp)
                            }

                            if (hasKey) {
                                OutlinedButton(
                                    onClick = {
                                        aiAssistantRepository.clearKeyForProvider(selectedProvider)
                                        Toast.makeText(context, "Key erased from Keystore", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = AccentRose)
                                }
                            }
                        }

                        testResultStatus?.let { status ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = status,
                                color = if (status.startsWith("✓")) AccentEmerald else AccentRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 3. Privacy & Offline Guarantee Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRIVACY & OFFLINE-FIRST GUARANTEE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentEmerald, letterSpacing = 1.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Zero Mandatory Accounts: All progress, streaks, and bookmarks stay on your phone.\n• Zero Remote Backend: The core learning academy functions completely offline.\n• Hardware Encryption: Your BYOK keys are encrypted using the device's secure enclave / Android Keystore.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400, lineHeight = 20.sp)
                        )
                    }
                }
            }
        }
    }
}
