package com.tvvideoplayer.app.settings

import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tvvideoplayer.app.R
import com.tvvideoplayer.app.cache.VideoCacheManager
import com.tvvideoplayer.app.player.PlayerCacheSettings

/**
 * Settings screen for cache and buffer configuration.
 * Direct port of CloudStream's SettingsPlayer.kt pattern.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.apply {
            title = "Player Settings"
            setDisplayHomeUpEnabled(true)
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var cacheSettings: PlayerCacheSettings
        private lateinit var cacheManager: VideoCacheManager

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_player, rootKey)

            cacheSettings = PlayerCacheSettings(requireContext())
            cacheManager = VideoCacheManager.getInstance(requireContext())

            setupCacheSizePreference()
            setupBufferSizePreference()
            setupBufferLengthPreference()
            setupClearCachePreference()
        }

        private fun setupCacheSizePreference() {
            val pref = findPreference<ListPreference>("video_cache_size") ?: return

            // Build entries from cache size options
            val entries = PlayerCacheSettings.CACHE_SIZE_NAMES.toTypedArray()
            val entryValues = PlayerCacheSettings.CACHE_SIZE_OPTIONS.map { it.toString() }.toTypedArray()

            pref.entries = entries
            pref.entryValues = entryValues

            // Set current value
            val currentSize = cacheSettings.getCacheSizeBytes()
            val currentValue = when {
                currentSize <= 0 -> "0"
                currentSize == Long.MAX_VALUE -> "-1"
                else -> (currentSize / (1024 * 1024)).toString()
            }
            pref.value = currentValue
            pref.summary = getCacheSizeSummary(currentSize)

            pref.setOnPreferenceChangeListener { _, newValue ->
                val sizeMb = (newValue as String).toIntOrNull() ?: 250
                cacheSettings.setCacheSizeMb(sizeMb)

                val bytes = when {
                    sizeMb < 0 -> Long.MAX_VALUE
                    sizeMb == 0 -> 0L
                    else -> sizeMb.toLong() * 1024 * 1024
                }

                // Reinitialize cache with new size
                cacheManager.initialize(bytes)
                pref.summary = getCacheSizeSummary(bytes)

                Toast.makeText(requireContext(), "Cache size updated to ${pref.entry}", Toast.LENGTH_SHORT).show()
                true
            }
        }

        private fun setupBufferSizePreference() {
            val pref = findPreference<ListPreference>("video_buffer_size") ?: return

            val entries = PlayerCacheSettings.BUFFER_SIZE_NAMES.toTypedArray()
            val entryValues = PlayerCacheSettings.BUFFER_SIZE_OPTIONS.map { it.toString() }.toTypedArray()

            pref.entries = entries
            pref.entryValues = entryValues

            val currentMb = (cacheSettings.getBufferSizeBytes() / (1024 * 1024)).toInt()
            pref.value = currentMb.toString()
            pref.summary = "Current: ${pref.entry}"

            pref.setOnPreferenceChangeListener { _, newValue ->
                val sizeMb = (newValue as String).toIntOrNull() ?: 150
                cacheSettings.setBufferSizeMb(sizeMb)
                pref.summary = "Current: $sizeMb MB"
                Toast.makeText(requireContext(), "Buffer size: $sizeMb MB (applies to new playback)", Toast.LENGTH_SHORT).show()
                true
            }
        }

        private fun setupBufferLengthPreference() {
            val pref = findPreference<ListPreference>("video_buffer_length") ?: return

            val entries = PlayerCacheSettings.BUFFER_LENGTH_NAMES.toTypedArray()
            val entryValues = PlayerCacheSettings.BUFFER_LENGTH_OPTIONS.map { it.toString() }.toTypedArray()

            pref.entries = entries
            pref.entryValues = entryValues

            val currentMs = cacheSettings.getBufferLengthMs().toInt()
            pref.value = currentMs.toString()
            pref.summary = "Current: ${pref.entry}"

            pref.setOnPreferenceChangeListener { _, newValue ->
                val lengthMs = (newValue as String).toIntOrNull() ?: 30000
                cacheSettings.setBufferLengthMs(lengthMs)
                pref.summary = "Current: ${pref.entry}"
                val label = if (lengthMs <= 0) "Auto" else "${lengthMs / 1000}s"
                Toast.makeText(requireContext(), "Buffer length: $label (applies to new playback)", Toast.LENGTH_SHORT).show()
                true
            }
        }

        private fun setupClearCachePreference() {
            val pref = findPreference<Preference>("clear_cache") ?: return
            updateCacheSizeSummary(pref)

            pref.setOnPreferenceClickListener {
                cacheManager.clearCache()
                // Reinitialize with current settings
                cacheManager.initialize(cacheSettings.getCacheSizeBytes())
                updateCacheSizeSummary(pref)
                Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show()
                true
            }
        }

        private fun updateCacheSizeSummary(pref: Preference) {
            val cacheSize = cacheManager.getCacheDirSize()
            pref.summary = "Current cache usage: ${Formatter.formatShortFileSize(requireContext(), cacheSize)}"
        }

        private fun getCacheSizeSummary(bytes: Long): String {
            return when {
                bytes <= 0 -> "Cache disabled - videos will not be cached to disk"
                bytes == Long.MAX_VALUE -> "Unlimited cache size - cached videos are never automatically removed"
                else -> "Cache size: ${Formatter.formatShortFileSize(requireContext(), bytes)}. " +
                        "When full, least recently watched videos are removed first."
            }
        }
    }
}
