package com.systemdesignlab.app.data.ai

import com.systemdesignlab.app.core.security.KeystoreSecretManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiProviderType(val displayName: String, val defaultEndpoint: String, val defaultModel: String) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com", "gemini-1.5-flash"),
    OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-4o-mini"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1", "meta-llama/llama-3.1-8b-instruct:free"),
    CUSTOM("Custom Endpoint", "https://api.openai.com/v1", "gpt-3.5-turbo"),
    LOCAL("Local Model (Offline)", "http://localhost:8080/v1", "local-model")
}

data class CompactCourseContext(
    val lessonTitle: String? = null,
    val lessonProblem: String? = null,
    val lessonTradeoffs: String? = null,
    val lessonTakeaway: String? = null,
    val conceptName: String? = null,
    val conceptSummary: String? = null,
    val exerciseQuestion: String? = null,
    val simulationState: String? = null,
    val isHintMode: Boolean = false
) {
    fun toPromptContext(): String {
        val parts = mutableListOf<String>()
        lessonTitle?.let { parts.add("Lesson: $it") }
        lessonProblem?.let { parts.add("Core Problem: $it") }
        lessonTakeaway?.let { parts.add("Key Takeaway: $it") }
        lessonTradeoffs?.let { parts.add("Trade-offs: $it") }
        conceptName?.let { parts.add("Active Concept: $it") }
        conceptSummary?.let { parts.add("Concept Summary: $it") }
        exerciseQuestion?.let { parts.add("Current Exercise: $it") }
        simulationState?.let { parts.add("Simulation Live State: $it") }
        return if (parts.isNotEmpty()) {
            "--- COURSE CONTEXT ---\n" + parts.joinToString("\n") + "\n---------------------"
        } else {
            ""
        }
    }
}

interface AiProvider {
    val providerType: AiProviderType

    suspend fun generateResponse(
        userPrompt: String,
        systemPrompt: String,
        context: CompactCourseContext,
        apiKey: String,
        model: String,
        endpoint: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String>

    suspend fun testConnection(
        apiKey: String,
        model: String,
        endpoint: String
    ): Result<Boolean>
}

class OpenAiCompatibleProvider(
    override val providerType: AiProviderType = AiProviderType.OPENAI,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) : AiProvider {

    override suspend fun generateResponse(
        userPrompt: String,
        systemPrompt: String,
        context: CompactCourseContext,
        apiKey: String,
        model: String,
        endpoint: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${endpoint.trimEnd('/')}/chat/completions"
            val fullSystemPrompt = buildString {
                append(systemPrompt)
                val ctx = context.toPromptContext()
                if (ctx.isNotBlank()) {
                    append("\n\n").append(ctx)
                }
                if (context.isHintMode) {
                    append("\n\nIMPORTANT: The user is asking for a HINT on an exercise. DO NOT reveal the complete solution or answer key. Provide a subtle guiding clue or thought-provoking question to help them discover it themselves.")
                }
            }

            val jsonBody = JSONObject().apply {
                put("model", model)
                put("temperature", temperature)
                put("max_tokens", maxTokens)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", fullSystemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }
                put("messages", messages)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("AI Provider Error (${response.code}): $errorBody"))
            }

            val responseJson = JSONObject(response.body?.string() ?: "{}")
            val choices = responseJson.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val content = choices.getJSONObject(0).optJSONObject("message")?.optString("content")
                    ?: "No content received."
                Result.success(content)
            } else {
                Result.failure(Exception("Malformed AI response format."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(
        apiKey: String,
        model: String,
        endpoint: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = "${endpoint.trimEnd('/')}/chat/completions"
            val jsonBody = JSONObject().apply {
                put("model", model)
                put("max_tokens", 5)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "ping")
                    })
                }
                put("messages", messages)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Connection test failed (HTTP ${response.code})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GeminiProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) : AiProvider {

    override val providerType: AiProviderType = AiProviderType.GEMINI

    override suspend fun generateResponse(
        userPrompt: String,
        systemPrompt: String,
        context: CompactCourseContext,
        apiKey: String,
        model: String,
        endpoint: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val base = endpoint.trimEnd('/')
            val safeModel = if (model.isBlank()) "gemini-1.5-flash" else model
            val url = "$base/v1beta/models/$safeModel:generateContent?key=$apiKey"

            val fullSystemPrompt = buildString {
                append(systemPrompt)
                val ctx = context.toPromptContext()
                if (ctx.isNotBlank()) {
                    append("\n\n").append(ctx)
                }
                if (context.isHintMode) {
                    append("\n\nIMPORTANT: The user is asking for a HINT on an exercise. DO NOT reveal the complete solution or answer key. Provide a subtle guiding clue or thought-provoking question to help them discover it themselves.")
                }
            }

            val jsonBody = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", fullSystemPrompt) })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userPrompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", temperature)
                    put("maxOutputTokens", maxTokens)
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("Gemini API Error (${response.code}): $errorBody"))
            }

            val responseJson = JSONObject(response.body?.string() ?: "{}")
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val parts = candidate.optJSONObject("content")?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text") ?: "No response generated."
                Result.success(text)
            } else {
                Result.failure(Exception("No candidate response generated by Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(
        apiKey: String,
        model: String,
        endpoint: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val base = endpoint.trimEnd('/')
            val safeModel = if (model.isBlank()) "gemini-1.5-flash" else model
            val url = "$base/v1beta/models/$safeModel:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "ping") })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gemini test failed (HTTP ${response.code})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class LocalAiProvider : AiProvider {
    override val providerType: AiProviderType = AiProviderType.LOCAL

    override suspend fun generateResponse(
        userPrompt: String,
        systemPrompt: String,
        context: CompactCourseContext,
        apiKey: String,
        model: String,
        endpoint: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> {
        return Result.success(
            "Local On-Device Inference Stub: Extensible interface for on-device quantized LLM (e.g. MediaPipe / llama.cpp / ExecuTorch). Context provided: ${context.lessonTitle ?: "General"}. Response generated completely offline."
        )
    }

    override suspend fun testConnection(
        apiKey: String,
        model: String,
        endpoint: String
    ): Result<Boolean> {
        return Result.success(true)
    }
}

class AiAssistantRepository(
    private val keystoreSecretManager: KeystoreSecretManager
) {
    private val providers: Map<AiProviderType, AiProvider> = mapOf(
        AiProviderType.GEMINI to GeminiProvider(),
        AiProviderType.OPENAI to OpenAiCompatibleProvider(AiProviderType.OPENAI),
        AiProviderType.OPENROUTER to OpenAiCompatibleProvider(AiProviderType.OPENROUTER),
        AiProviderType.CUSTOM to OpenAiCompatibleProvider(AiProviderType.CUSTOM),
        AiProviderType.LOCAL to LocalAiProvider()
    )

    private val defaultSystemPrompt = """
        You are 'Lab Assistant', a specialized, world-class Staff Distributed Systems Engineer and tutor on System Design Lab.
        The course is built directly from Karan Pratap Singh's renowned system-design repository.
        Guidelines:
        1. Base your primary explanations directly on proven distributed systems principles and the course context.
        2. Clearly distinguish repository-provided definitions from additional real-world engineering experiences.
        3. Be concise, rigorous, and educational. Format answers with clear headings and bullet points.
        4. Focus on latency, scalability, throughput, fault tolerance, and practical architectural trade-offs.
    """.trimIndent()

    suspend fun askAssistant(
        prompt: String,
        providerType: AiProviderType,
        model: String,
        endpoint: String,
        temperature: Float,
        maxTokens: Int,
        context: CompactCourseContext
    ): Result<String> {
        val provider = providers[providerType] ?: providers[AiProviderType.GEMINI]!!
        
        // If Local provider, no API key needed
        if (providerType == AiProviderType.LOCAL) {
            return provider.generateResponse(
                userPrompt = prompt,
                systemPrompt = defaultSystemPrompt,
                context = context,
                apiKey = "",
                model = model,
                endpoint = endpoint,
                temperature = temperature,
                maxTokens = maxTokens
            )
        }

        val apiKey = keystoreSecretManager.getApiKey(providerType.name)
        if (apiKey.isNullOrBlank()) {
            return Result.failure(
                IllegalStateException("No API key configured for ${providerType.displayName}. Please configure your API key in Settings -> AI Assistant.")
            )
        }

        return provider.generateResponse(
            userPrompt = prompt,
            systemPrompt = defaultSystemPrompt,
            context = context,
            apiKey = apiKey,
            model = model,
            endpoint = endpoint,
            temperature = temperature,
            maxTokens = maxTokens
        )
    }

    suspend fun testConnection(
        providerType: AiProviderType,
        model: String,
        endpoint: String
    ): Result<Boolean> {
        val provider = providers[providerType] ?: providers[AiProviderType.GEMINI]!!
        val apiKey = keystoreSecretManager.getApiKey(providerType.name) ?: ""
        return provider.testConnection(apiKey, model, endpoint)
    }

    fun hasKeyForProvider(providerType: AiProviderType): Boolean {
        if (providerType == AiProviderType.LOCAL) return true
        return keystoreSecretManager.hasApiKey(providerType.name)
    }

    fun getMaskedKeyForProvider(providerType: AiProviderType): String? {
        return keystoreSecretManager.getMaskedKey(providerType.name)
    }

    fun saveKeyForProvider(providerType: AiProviderType, key: String): Boolean {
        return keystoreSecretManager.saveApiKey(providerType.name, key)
    }

    fun clearKeyForProvider(providerType: AiProviderType): Boolean {
        return keystoreSecretManager.clearApiKey(providerType.name)
    }
}
