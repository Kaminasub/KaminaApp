package com.kamina.app

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.MimeTypes

class MediaSourceManager(private val context: Context) {

    // Function that receives MIME type if available
    fun getMediaSource(url: String, mimeType: String?): MediaSource {
        val uri = Uri.parse(url)
        val dataSourceFactory = DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory())

        // Use the provided MIME type if available, otherwise make a reasonable guess
        return when (mimeType) {
            MimeTypes.APPLICATION_M3U8 -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            MimeTypes.APPLICATION_MPD -> {
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            MimeTypes.VIDEO_MP4, MimeTypes.VIDEO_WEBM -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            else -> {
                // Default to progressive media source if MIME type is not recognized or provided
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
        }
    }

    // Initialize player with optional MIME type
    fun initializePlayer(player: ExoPlayer, url: String, mimeType: String? = null) {
        val mediaSource = getMediaSource(url, mimeType)
        player.setMediaSource(mediaSource)
        player.prepare()
    }
}