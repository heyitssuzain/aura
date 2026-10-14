package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class AuraPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aura_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_INTRO_COMPLETED = "key_intro_completed"
        private const val KEY_ANIMATIONS_ENABLED = "key_animations_enabled"
        private const val KEY_AUDIO_QUALITY = "key_audio_quality"
        private const val KEY_LAST_VOLUME = "key_last_volume"
    }

    var isIntroCompleted: Boolean
        get() = prefs.getBoolean(KEY_INTRO_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_INTRO_COMPLETED, value).apply()

    var isAnimationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ANIMATIONS_ENABLED, value).apply()

    var audioQuality: String
        get() = prefs.getString(KEY_AUDIO_QUALITY, "High (320kbps)") ?: "High (320kbps)"
        set(value) = prefs.edit().putString(KEY_AUDIO_QUALITY, value).apply()
}
