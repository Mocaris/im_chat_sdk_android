package com.lbe.imsdk.media.player

import android.content.Context
import androidx.annotation.FloatRange
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM
import androidx.media3.common.Player.Commands
import androidx.media3.common.Player.Listener
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.lbe.imsdk.service.http.interceptor.SignInterceptor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Closeable

/**
 *
 * @Author mocaris
 * @Date 2025-09-05
 */

@UnstableApi
class IMPlayerManager(
    private val context: Context,
    private val url: String,
    val lbeToken: String = SignInterceptor.lbeToken ?: ""
) :
    Listener, Closeable {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val mediaSourceFactory: DefaultMediaSourceFactory by lazy {
        DefaultMediaSourceFactory(
            DefaultDataSource.Factory(
                context,
                DefaultHttpDataSource.Factory().setDefaultRequestProperties(
                    mapOf(
                        "lbeToken" to lbeToken
                    )
                )
            )
        )
    }

    val exoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .build()

    val isPlaying = MutableStateFlow(false)
    val isBuffering = MutableStateFlow(false)
    val videoSize = MutableStateFlow<VideoSize?>(null)

    val duration = MutableStateFlow(0L)
    val position = MutableStateFlow(0L)

    private val observerJob: Job? = null

    init {
        exoPlayer.addListener(this)
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
    }

    private fun observePosition() {
        observerJob?.cancel()
        scope.launch {
            while (isActive) {
                position.value = exoPlayer.currentPosition
                delay(500)
            }
        }
    }

    override fun onAvailableCommandsChanged(availableCommands: Commands) {
        if (availableCommands.contains(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            duration.value = exoPlayer.duration
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        this.isPlaying.value = isPlaying
        if (isPlaying) {
            observePosition()
        } else {
            observerJob?.cancel()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                isBuffering.value = true
            }

            Player.STATE_READY -> {
                isBuffering.value = false
            }

            Player.STATE_ENDED -> {
                isPlaying.value = false
            }

            Player.STATE_IDLE -> {
                isPlaying.value = false
            }
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        this.videoSize.value = videoSize
    }

    fun seekTo(position: Long) {
        if (exoPlayer.availableCommands.contains(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            exoPlayer.seekTo(position)
        }
    }

    fun seekTo(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (exoPlayer.availableCommands.contains(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            exoPlayer.seekTo((exoPlayer.duration * alpha).toLong())
        }
    }

    fun play() {
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
            exoPlayer.playWhenReady = true
        }
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.stop()
    }

    fun release() {
        exoPlayer.release()
    }

    override fun close() {
        scope.cancel()
        exoPlayer.removeListener(this)
        exoPlayer.release()
    }
}

//class IMUrlPlayerManager(url: String) : IMPlayerManager() {
//
//
//}
//
//class IMLocalPlayerManager(file: File) : IMPlayerManager() {
//
//    fun play(file: File) {
//        exoPlayer.setMediaItem(MediaItem.fromUri(file.absolutePath))
//        exoPlayer.prepare()
//        exoPlayer.play()
//    }
//
//}