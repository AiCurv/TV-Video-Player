package com.tvvideoplayer.app.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.tvvideoplayer.app.R
import com.tvvideoplayer.app.cache.VideoCacheManager
import com.tvvideoplayer.app.player.PlayerCacheSettings
import com.tvvideoplayer.app.player.TVVideoPlayer

/**
 * Main Android TV player activity.
 * Uses ExoPlayer's PlayerView with Leanback-style controls.
 *
 * Receives video URL via intent extras:
 * - EXTRA_VIDEO_URL: The video URL to play
 * - EXTRA_VIDEO_TITLE: Optional title for display
 * - EXTRA_HEADERS: Optional headers map for the request
 */
@UnstableApi
class TVPlayerActivity : FragmentActivity(), TVVideoPlayer.PlayerListener {

    companion object {
        private const val TAG = "TVPlayerActivity"
        const val EXTRA_VIDEO_URL = "video_url"
        const val EXTRA_VIDEO_TITLE = "video_title"
        const val EXTRA_HEADERS = "video_headers"
        const val EXTRA_START_POSITION = "start_position"
    }

    private lateinit var player: TVVideoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var cacheSettings: PlayerCacheSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        cacheSettings = PlayerCacheSettings(this)
        playerView = findViewById(R.id.player_view)

        // Initialize the player
        player = TVVideoPlayer(this).apply {
            playerListener = this@TVPlayerActivity
            initialize()
        }

        // Bind ExoPlayer to PlayerView
        playerView.player = player.getExoPlayer()

        // Load video from intent
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        if (videoUrl.isNullOrBlank()) {
            Toast.makeText(this, "No video URL provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val headers = intent.getSerializableExtra(EXTRA_HEADERS) as? Map<String, String> ?: emptyMap()
        player.play(videoUrl, headers)
    }

    override fun onPlaybackStateChanged(state: TVVideoPlayer.PlaybackState) {
        Log.d(TAG, "State: $state")
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(TAG, "Playing: $isPlaying")
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        Log.d(TAG, "Video size: ${width}x${height}")
    }

    override fun onError(error: PlaybackException) {
        Log.e(TAG, "Error: ${error.message}")
        runOnUiThread {
            Toast.makeText(this, "Playback error: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPositionChanged(positionMs: Long, durationMs: Long) {
        // Could update a seek bar here
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onResume() {
        super.onResume()
        if (player.getPlaybackState() != TVVideoPlayer.PlaybackState.ENDED) {
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
