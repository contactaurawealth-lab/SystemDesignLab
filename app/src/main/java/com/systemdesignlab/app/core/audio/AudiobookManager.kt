package com.systemdesignlab.app.core.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudiobookPlayerState(
    val currentChapterId: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutes: Int = 0,
    val sleepTimerRemainingSec: Int? = null
)

class AudiobookManager(
    private val context: Context,
    private val onProgressUpdate: (suspend (chapterId: String, positionMs: Long, durationMs: Long, isCompleted: Boolean) -> Unit)? = null
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var sleepTimerJob: Job? = null
    private var progressTrackingJob: Job? = null

    private val _playerState = MutableStateFlow(AudiobookPlayerState())
    val playerState: StateFlow<AudiobookPlayerState> = _playerState.asStateFlow()

    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startProgressTracker()
                    } else {
                        stopProgressTracker()
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        val duration = duration.coerceAtLeast(0L)
                        _playerState.value = _playerState.value.copy(durationMs = duration)
                    } else if (state == Player.STATE_ENDED) {
                        _playerState.value = _playerState.value.copy(isPlaying = false)
                        val chapterId = _playerState.value.currentChapterId
                        if (chapterId != null) {
                            scope.launch {
                                onProgressUpdate?.invoke(chapterId, duration, duration, true)
                            }
                        }
                    }
                }
            })
        }

    fun playChapter(chapterId: String, assetPath: String, startPositionMs: Long = 0L) {
        val uri = Uri.parse("asset:///$assetPath")
        val mediaItem = MediaItem.fromUri(uri)

        player.setMediaItem(mediaItem)
        player.prepare()
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.play()

        _playerState.value = _playerState.value.copy(
            currentChapterId = chapterId,
            currentPositionMs = startPositionMs,
            isPlaying = true
        )
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        player.pause()
        _playerState.value = _playerState.value.copy(isPlaying = false)
        persistCurrentProgress()
    }

    fun resume() {
        player.play()
        _playerState.value = _playerState.value.copy(isPlaying = true)
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))
        _playerState.value = _playerState.value.copy(currentPositionMs = player.currentPosition)
        persistCurrentProgress()
    }

    fun skipForward(seconds: Int = 15) {
        val target = (player.currentPosition + seconds * 1000L).coerceAtMost(player.duration.coerceAtLeast(0L))
        seekTo(target)
    }

    fun skipBackward(seconds: Int = 15) {
        val target = (player.currentPosition - seconds * 1000L).coerceAtLeast(0L)
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _playerState.value = _playerState.value.copy(
                sleepTimerMinutes = 0,
                sleepTimerRemainingSec = null
            )
            return
        }

        _playerState.value = _playerState.value.copy(
            sleepTimerMinutes = minutes,
            sleepTimerRemainingSec = minutes * 60
        )

        sleepTimerJob = scope.launch {
            var remaining = minutes * 60
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _playerState.value = _playerState.value.copy(sleepTimerRemainingSec = remaining)
            }
            if (isActive) {
                pause()
                _playerState.value = _playerState.value.copy(
                    sleepTimerMinutes = 0,
                    sleepTimerRemainingSec = null
                )
            }
        }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch {
            var counter = 0
            while (isActive) {
                val current = player.currentPosition
                val duration = player.duration.coerceAtLeast(0L)
                _playerState.value = _playerState.value.copy(
                    currentPositionMs = current,
                    durationMs = duration
                )
                counter++
                if (counter % 5 == 0) { // Persist every 5 seconds
                    persistCurrentProgress()
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackingJob?.cancel()
    }

    private fun persistCurrentProgress() {
        val chapterId = _playerState.value.currentChapterId ?: return
        val current = player.currentPosition
        val duration = player.duration.coerceAtLeast(0L)
        val isCompleted = duration > 0 && current >= (duration * 0.95)

        scope.launch {
            onProgressUpdate?.invoke(chapterId, current, duration, isCompleted)
        }
    }

    fun release() {
        stopProgressTracker()
        sleepTimerJob?.cancel()
        player.release()
        scope.cancel()
    }
}
