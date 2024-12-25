package com.kamina.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
import io.github.edsuns.adfilter.AdFilter
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
    private var videoId: Int = -1
    private lateinit var webView: WebView
    private lateinit var continueButton: Button
    private lateinit var handler: Handler
    private lateinit var nextEpisodeTitle: TextView
    private lateinit var countdownTextView: TextView
    private var currentVideoId: Int = -1
    private var countdownTime = 10
    private var userLanguage: String? = null

    private lateinit var adFilter: AdFilter

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_watch_page)

        webView = findViewById(R.id.episode_webview)
        continueButton = findViewById(R.id.continueButton)
        continueButton.visibility = View.GONE
        nextEpisodeTitle = findViewById(R.id.nextEpisodeTitle)
        countdownTextView = findViewById(R.id.countdownTextView)
        countdownTextView.visibility = View.GONE

        entityId = intent.getIntExtra("entityId", -1)
        season = intent.getIntExtra("season", 1)
        episode = intent.getIntExtra("episode", 1)
        userId = intent.getIntExtra("userId", -1)
        userLanguage = intent.getStringExtra("userLanguage")

        adFilter = AdFilter.create(this)

        setupWebView()
        fetchEpisodeDetails()

        continueButton.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            markEpisodeAsWatchedAndGoToNext()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                setContentView(view)
            }

            override fun onHideCustomView() {
                setContentView(R.layout.activity_watch_page)
                setupWebView()
            }

            // Updated method to handle console messages
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                val message = consoleMessage.message()
                val sourceID = consoleMessage.sourceId()
                val lineNumber = consoleMessage.lineNumber()

                if (message.contains("ReferenceError") && message.contains("is not defined")) {
                    // Suppress specific "ReferenceError" messages
                    Log.d("AdBlocker", "Suppressed console error: $message at $sourceID:$lineNumber")
                    return true // Return true to suppress the message from appearing in logs
                }

                // Log other console messages if needed
                return super.onConsoleMessage(consoleMessage)
            }
        }



        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url.toString()
                // Avoid blocking essential video resources
                return if (shouldBlockUrl(url) && !url.contains("hlswish.com")) {
                    WebResourceResponse("text/plain", "utf-8", null)
                } else {
                    adFilter.shouldIntercept(view, request)?.resourceResponse
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                Log.e("WebViewError", "Error: ${error.description} on URL ${request.url}")
            }


            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                adFilter.performScript(view, url)
            }
        }
    }

    // Helper function to match ad URLs
    private fun shouldBlockUrl(url: String): Boolean {
        return url.contains("mc.yandex.ru") ||
                url.contains("googletagmanager.com") ||
                //STREAMWISH///
                url.contains("dalysv.com") ||
                url.contains("media.dalysv.com") ||
                url.contains("media.dalysv.com") ||
                url.contains("jouwaikekaivep.net") ||
                url.contains("outwingullom.com") ||
                url.contains("dnsads.js") ||
                url.contains("code.min.js") ||
                url.contains("roseimgs.com") ||
                url.contains("streamwish.com/js") ||
                url.contains("naupsakiwhy.com") ||
                url.contains("psoroumukr.com") ||
                url.contains("ap.taichnewcal.com") ||


                //OKRU////
                url.contains("ok.ru/dk?cmd=videostatnew") ||
                url.contains("tns-counter.ru") ||
                url.contains("ad.mail.ru") ||
                url.contains("vk.com/js/lang-pack.js") ||
                url.contains("top-fwz1.mail.ru") ||
                url.contains("st.okcdn.ru/static/one-video-player")

    }

    // Function to convert the "duration" field (formatted as HH:mm:ss) to milliseconds
    private fun convertDurationToMillis(duration: String): Long {
        val timeParts = duration.split(":")
        val hours = if (timeParts.size > 2) timeParts[0].toLong() else 0L
        val minutes = if (timeParts.size > 1) timeParts[timeParts.size - 2].toLong() else 0L
        val seconds = if (timeParts.isNotEmpty()) timeParts[timeParts.size - 1].toLong() else 0L

        return TimeUnit.HOURS.toMillis(hours) +
                TimeUnit.MINUTES.toMillis(minutes) +
                TimeUnit.SECONDS.toMillis(seconds)
    }

    // Function to fetch episode details with language support
    private fun fetchEpisodeDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch episode data based on entityId, season, episode, and user language
                val episodeData: WatchEpisode? = fetchWatchEpisode(
                    entityId,
                    season,
                    episode,
                    userLanguage ?: "en"
                ) // Default to "en" if null

                withContext(Dispatchers.Main) {
                    if (episodeData != null) {
                        webView.loadUrl(episodeData.filePath)
                        currentVideoId = episodeData.id

                        // Convert duration (String) to milliseconds
                        val durationMillis = convertDurationToMillis(episodeData.duration)
                        setupContinueButtonTimer(durationMillis)

                        // Fetch and display next episode's title
                        fetchNextEpisodeTitle()
                    } else {
                        Log.e(
                            "WatchPage",
                            "No episode data found for entityId: $entityId, season: $season, episode: $episode"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Function to start the timer for showing the Continue button and start countdown
    private fun setupContinueButtonTimer(durationMillis: Long) {
        val delayMillis = (durationMillis * 0.99).toLong()
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

    // Function to fetch the next episode title
    private fun fetchNextEpisodeTitle() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Attempt to fetch the next episode in the current season
                var nextEpisodeData: WatchEpisode? = fetchWatchEpisode(
                    entityId,
                    season,
                    episode + 1,
                    userLanguage ?: "en"
                )

                // If no next episode in current season, try season + 1, episode 1
                if (nextEpisodeData == null) {
                    nextEpisodeData = fetchWatchEpisode(
                        entityId,
                        season + 1,
                        1,
                        userLanguage ?: "en"
                    )
                }

                withContext(Dispatchers.Main) {
                    if (nextEpisodeData != null) {
                        val nextTitle = "Up Next:\nS${nextEpisodeData.season}E${nextEpisodeData.episode}: ${nextEpisodeData.title}"
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

    // Function to go to the next episode
    private fun goToNextEpisode() {
        CoroutineScope(Dispatchers.Main).launch {
            // First, try the next episode in the current season
            episode += 1
            var episodeData: WatchEpisode? = fetchWatchEpisode(entityId, season, episode, userLanguage ?: "en")

            // If no more episodes in current season, go to first episode of next season
            if (episodeData == null) {
                season += 1
                episode = 1
                episodeData = fetchWatchEpisode(entityId, season, episode, userLanguage ?: "en")
            }

            if (episodeData != null) {
                continueButton.visibility = View.GONE
                nextEpisodeTitle.visibility = View.GONE
                countdownTextView.visibility = View.GONE  // Hide the countdown text
                fetchEpisodeDetails()  // Fetch the new episode data and load the video
            } else {
                Log.e("WatchPage", "No further episodes found.")
            }
        }
    }

    // Handle back press to return to the detail page
    override fun onBackPressed() {
        // Stop loading if WebView is active
        webView.stopLoading()

        // Redirect back to the DetailPageActivity with the correct entityId, userId, and userLanguage
        val intent = Intent(this, DetailPageActivity::class.java).apply {
            putExtra("entityId", entityId)
            putExtra("userId", userId)
            putExtra("userLanguage", userLanguage) // Include user language
        }
        startActivity(intent)
        finish() // Finish the current activity

        // Call super to handle the default back press behavior
        super.onBackPressed()
    }
}

//SERIES