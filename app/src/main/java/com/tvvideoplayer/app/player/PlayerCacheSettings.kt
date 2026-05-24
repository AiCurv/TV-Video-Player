package com.tvvideoplayer.app.player

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Manages cache and buffer settings using SharedPreferences.
 * Direct port of CloudStream's Settings.kt pattern for player preferences.
 */
class PlayerCacheSettings(context: Context) {

    companion object {
        // Preference keys - matches CloudStream's naming convention
        const val VIDEO_CACHE_SIZE_KEY = "video_cache_size"
        const val VIDEO_BUFFER_SIZE_KEY = "video_buffer_size"
        const val VIDEO_BUFFER_LENGTH_KEY = "video_buffer_length"

        // Default values
        const val DEFAULT_CACHE_SIZE_MB = 250
        const val DEFAULT_BUFFER_SIZE_MB = 150
        const val DEFAULT_BUFFER_LENGTH_MS = 30000 // 30 seconds

        // Cache size options (MB) - matches CloudStream's video_buffer_size_values
        val CACHE_SIZE_OPTIONS = listOf(0, 100, 250, 500, 1024, 2048, -1) // -1 = unlimited
        val CACHE_SIZE_NAMES = listOf(
            "Disabled",
            "100 MB",
            "250 MB",
            "500 MB",
            "1 GB",
            "2 GB",
            "Unlimited"
        )

        // Buffer size options (MB)
        val BUFFER_SIZE_OPTIONS = listOf(16, 50, 100, 150, 200, 500)
        val BUFFER_SIZE_NAMES = listOf("16 MB", "50 MB", "100 MB", "150 MB", "200 MB", "500 MB")

        // Buffer length options (seconds)
        val BUFFER_LENGTH_OPTIONS = listOf(0, 15000, 30000, 60000, 120000, 180000, 300000)
        val BUFFER_LENGTH_NAMES = listOf(
            "Auto",
            "15 seconds",
            "30 seconds",
            "1 minute",
            "2 minutes",
            "3 minutes",
            "5 minutes"
        )
    }

    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun getCacheSizeBytes(): Long {
        val sizeMb = prefs.getInt(VIDEO_CACHE_SIZE_KEY, DEFAULT_CACHE_SIZE_MB)
        return when {
            sizeMb < 0 -> Long.MAX_VALUE // unlimited
            sizeMb == 0 -> 0L // disabled
            else -> sizeMb.toLong() * 1024 * 1024
        }
    }

    fun setCacheSizeMb(sizeMb: Int) {
        prefs.edit().putInt(VIDEO_CACHE_SIZE_KEY, sizeMb).apply()
    }

    fun getBufferSizeBytes(): Long {
        val sizeMb = prefs.getInt(VIDEO_BUFFER_SIZE_KEY, DEFAULT_BUFFER_SIZE_MB)
        return sizeMb.toLong() * 1024 * 1024
    }

    fun setBufferSizeMb(sizeMb: Int) {
        prefs.edit().putInt(VIDEO_BUFFER_SIZE_KEY, sizeMb).apply()
    }

    fun getBufferLengthMs(): Long {
        return prefs.getInt(VIDEO_BUFFER_LENGTH_KEY, DEFAULT_BUFFER_LENGTH_MS).toLong()
    }

    fun setBufferLengthMs(lengthMs: Int) {
        prefs.edit().putInt(VIDEO_BUFFER_LENGTH_KEY, lengthMs).apply()
    }

    fun isCacheEnabled(): Boolean {
        return getCacheSizeBytes() > 0
    }
}
