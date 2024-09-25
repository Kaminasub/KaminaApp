package com.kamina.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.util.Log
import com.kamina.app.api.UserProgress
import com.kamina.app.api.WatchEpisode
import com.kamina.app.api.createUserProgress
import com.kamina.app.api.fetchUserProgress
import com.kamina.app.api.fetchWatchEpisode
import com.kamina.app.api.updateUserProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WatchPage : AppCompatActivity() {

    private var entityId: Int = -1
    private var season: Int = 1
    private var episode: Int = 1
    private var userId: Int = -1
    private var videoId: Int = -1  // Add videoId to track the video
    private lateinit var webView: WebView
    private lateinit var continueButton: Button
    private lateinit var handler: Handler
    private lateinit var nextEpisodeTitle: TextView
    private lateinit var countdownTextView: TextView // TextView for countdown
    private var currentVideoId: Int = -1
    private var countdownTime = 10  // Initial countdown value

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the activity to full-screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Set the content view to the layout with WebView and Continue button
        setContentView(R.layout.activity_watch_page)

        // Initialize the WebView, Continue button, Next Episode Title, and Countdown Text
        webView = findViewById(R.id.episode_webview)
        continueButton = findViewById(R.id.continueButton)
        continueButton.visibility = View.GONE  // Initially hide the button
        nextEpisodeTitle = findViewById(R.id.nextEpisodeTitle)
        countdownTextView = findViewById(R.id.countdownTextView)  // New TextView for countdown
        countdownTextView.visibility = View.GONE  // Initially hide the countdown text

        // Extract the entityId, season, and episode from the intent
        entityId = intent.getIntExtra("entityId", -1)
        season = intent.getIntExtra("season", 1)
        episode = intent.getIntExtra("episode", 1)
        userId = intent.getIntExtra("userId", -1)
        Log.d("WatchPage", "Received userId in WatchPage: $userId")

        if (userId == -1) {
            Log.e("WatchPage", "Invalid userId passed to WatchPage")
        }

        // Configure WebView
        setupWebView()

        // Fetch episode details and load the video
        fetchEpisodeDetails()

        // Handle Continue button click to move to the next episode
        continueButton.setOnClickListener {
            handler.removeCallbacksAndMessages(null)  // Cancel the countdown if the button is clicked
            markEpisodeAsWatchedAndGoToNext()
        }
    }

    // Function to configure WebView
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        // Enable full-screen video
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                // Enter full-screen mode when playing video
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                setContentView(view)
            }

            override fun onHideCustomView() {
                // Exit full-screen mode when video is done
                setContentView(R.layout.activity_watch_page)
                setupWebView()  // Reinitialize WebView after exiting full screen
            }
        }

        // Handle redirects inside WebView
        webView.webViewClient = WebViewClient()
    }

    // Function to fetch episode details
    private fun fetchEpisodeDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val episodeData: WatchEpisode? = fetchWatchEpisode(entityId, season, episode)

                withContext(Dispatchers.Main) {
                    if (episodeData != null) {
                        webView.loadUrl(episodeData.filePath)
                        currentVideoId = episodeData.id

                        // Convert duration (String) to milliseconds
                        val durationMillis = convertDurationToMillis(episodeData.duration)
                        setupContinueButtonTimer(durationMillis)

                        // Fetch and display next episode's title
                        fetchNextEpisodeTitle()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Function to start the timer for showing the Continue button and start countdown
    private fun setupContinueButtonTimer(durationMillis: Long) {
        val delayMillis = (durationMillis * 0.95).toLong()
        Handler(Looper.getMainLooper()).postDelayed({
            continueButton.visibility = View.VISIBLE
            nextEpisodeTitle.visibility = View.VISIBLE
            startCountdown()
        }, delayMillis)
    }

    // Function to start the countdown and automatically click Continue when it reaches 0
    private fun startCountdown() {
        countdownTime = 10  // Start from 10 seconds
        countdownTextView.visibility = View.VISIBLE  // Show the countdown text

        handler = Handler(Looper.getMainLooper())
        val countdownRunnable = object : Runnable {
            override fun run() {
                if (countdownTime > 0) {
                    countdownTextView.text = "Continuing in $countdownTime seconds..."
                    countdownTime--
                    handler.postDelayed(this, 1000)
                } else {
                    // Auto-hit the Continue button when the countdown reaches 0
                    countdownTextView.visibility = View.GONE  // Hide the countdown text
                    continueButton.performClick()  // Simulate the button click
                }
            }
        }
        handler.post(countdownRunnable)
    }

    // Update the method to fetch next episode title
    private fun fetchNextEpisodeTitle() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nextEpisodeData: WatchEpisode? =
                    fetchWatchEpisode(entityId, season, episode + 1)

                withContext(Dispatchers.Main) {
                    if (nextEpisodeData != null) {
                        val nextTitle =
                            "Up Next:\nS${season}E${episode + 1}: ${nextEpisodeData.title}"
                        nextEpisodeTitle.text = nextTitle
                    } else {
                        nextEpisodeTitle.text = ""
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Function to mark the current episode as watched and go to the next episode
    private fun markEpisodeAsWatchedAndGoToNext() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("WatchPage", "Attempting to mark episode S${season}E${episode} as watched.")

                // Assuming userId is passed through intent
                val progress = UserProgress(
                    userId = userId,
                    videoId = currentVideoId,
                    currentSeason = season,
                    currentEpisode = episode,
                    skipIntro = 0,
                    skipOutro = 0,
                    watched = 1,
                    entityId = entityId
                )

                // Check if the episode has already been marked as watched
                val existingProgress = fetchUserProgress(userId, entityId)
                val success = if (existingProgress == null) {
                    createUserProgress(progress)
                } else {
                    updateUserProgress(progress.copy(id = existingProgress.id))
                }

                Log.d("WatchPage", "Create/Update result: $success")
                Log.d("WatchPage", "userId: $userId, videoId: $currentVideoId")

                withContext(Dispatchers.Main) {
                    if (success) {
                        goToNextEpisode()
                    } else {
                        Log.e("WatchPage", "Failed to mark episode as watched.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("WatchPage", "Error marking episode as watched: ${e.message}")
            }
        }
    }




    // Function to convert the "duration" field (formatted as time) to milliseconds
    private fun convertDurationToMillis(duration: String): Long {
        val timeParts = duration.split(":")
        val hours = timeParts[0].toLong()
        val minutes = timeParts[1].toLong()
        val seconds = timeParts[2].toLong()

        return TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(
            seconds
        )
    }

    // Function to go to the next episode
    private fun goToNextEpisode() {
        CoroutineScope(Dispatchers.Main).launch {
            episode += 1
            continueButton.visibility = View.GONE
            nextEpisodeTitle.visibility = View.GONE
            countdownTextView.visibility = View.GONE  // Hide the countdown text
            fetchEpisodeDetails()  // Fetch the new episode data and load the video
        }
    }

    // Handle back press to return to the detail page
    override fun onBackPressed() {
        val intent = Intent(this, DetailPageActivity::class.java)
        intent.putExtra("entityId", entityId)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}