package com.systemdesignlab.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val SLEEP_TIMER_MINUTES = intPreferencesKey("sleep_timer_minutes")
        val DAILY_GOAL_TARGET_XP = intPreferencesKey("daily_goal_target_xp")
        val SELECTED_AI_PROVIDER = stringPreferencesKey("selected_ai_provider")
        val SELECTED_AI_MODEL = stringPreferencesKey("selected_ai_model")
        val CUSTOM_AI_ENDPOINT = stringPreferencesKey("custom_ai_endpoint")
        val AI_TEMPERATURE = floatPreferencesKey("ai_temperature")
        val AI_MAX_TOKENS = intPreferencesKey("ai_max_tokens")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system"
    }

    val playbackSpeedFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PLAYBACK_SPEED] ?: 1.0f
    }

    val sleepTimerMinutesFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SLEEP_TIMER_MINUTES] ?: 0
    }

    val dailyGoalTargetXpFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_GOAL_TARGET_XP] ?: 50
    }

    val selectedAiProviderFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_AI_PROVIDER] ?: "gemini"
    }

    val selectedAiModelFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_AI_MODEL] ?: "gemini-1.5-flash"
    }

    val customAiEndpointFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_AI_ENDPOINT] ?: "https://api.openai.com/v1"
    }

    val aiTemperatureFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[AI_TEMPERATURE] ?: 0.7f
    }

    val aiMaxTokensFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AI_MAX_TOKENS] ?: 1000
    }

    val hapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTIC_ENABLED] ?: true
    }

    val reducedMotionFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REDUCED_MOTION] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[THEME_MODE] = mode }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { prefs -> prefs[PLAYBACK_SPEED] = speed }
    }

    suspend fun setSleepTimerMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[SLEEP_TIMER_MINUTES] = minutes }
    }

    suspend fun setDailyGoalTargetXp(targetXp: Int) {
        context.dataStore.edit { prefs -> prefs[DAILY_GOAL_TARGET_XP] = targetXp }
    }

    suspend fun setAiProvider(provider: String) {
        context.dataStore.edit { prefs -> prefs[SELECTED_AI_PROVIDER] = provider }
    }

    suspend fun setAiModel(model: String) {
        context.dataStore.edit { prefs -> prefs[SELECTED_AI_MODEL] = model }
    }

    suspend fun setCustomAiEndpoint(endpoint: String) {
        context.dataStore.edit { prefs -> prefs[CUSTOM_AI_ENDPOINT] = endpoint }
    }

    suspend fun setAiTemperature(temp: Float) {
        context.dataStore.edit { prefs -> prefs[AI_TEMPERATURE] = temp }
    }

    suspend fun setAiMaxTokens(maxTokens: Int) {
        context.dataStore.edit { prefs -> prefs[AI_MAX_TOKENS] = maxTokens }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[HAPTIC_ENABLED] = enabled }
    }

    suspend fun setReducedMotion(reduced: Boolean) {
        context.dataStore.edit { prefs -> prefs[REDUCED_MOTION] = reduced }
    }
}
