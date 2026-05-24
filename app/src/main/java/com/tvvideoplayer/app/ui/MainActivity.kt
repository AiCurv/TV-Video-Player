package com.tvvideoplayer.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tvvideoplayer.app.R
import com.tvvideoplayer.app.settings.SettingsActivity

/**
 * Main entry point - simple URL input for testing.
 * On Android TV, this would be replaced with a Leanback browse fragment.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val urlInput = findViewById<EditText>(R.id.url_input)
        val playButton = findViewById<Button>(R.id.play_button)
        val settingsButton = findViewById<Button>(R.id.settings_button)

        // Set a default test URL
        urlInput.setText("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")

        playButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a video URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, TVPlayerActivity::class.java).apply {
                putExtra(TVPlayerActivity.EXTRA_VIDEO_URL, url)
            }
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
