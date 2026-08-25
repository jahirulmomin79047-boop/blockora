package com.example.blockblast.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("block_blast_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HIGH_SCORE = "key_high_score"
        private const val KEY_SESSION_HIGH_SCORE = "key_session_high_score"
        private const val KEY_SFX_ENABLED = "key_sfx_enabled"
        private const val KEY_BGM_ENABLED = "key_bgm_enabled"
    }

    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()

    var sessionHighScore: Int
        get() = prefs.getInt(KEY_SESSION_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_SESSION_HIGH_SCORE, value).apply()

    var soundEffectsEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SFX_ENABLED, value).apply()

    var backgroundMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_BGM_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BGM_ENABLED, value).apply()

    fun resetHighScore() {
        prefs.edit()
            .putInt(KEY_HIGH_SCORE, 0)
            .putInt(KEY_SESSION_HIGH_SCORE, 0)
            .apply()
    }
}

