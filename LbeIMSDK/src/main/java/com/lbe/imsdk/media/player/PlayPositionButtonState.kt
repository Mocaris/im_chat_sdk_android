package com.lbe.imsdk.media.player

import androidx.compose.runtime.*
import androidx.media3.common.*
import androidx.media3.common.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 *
 * @Author mocaris
 * @Date 2025-09-08
 */
@UnstableApi
@Composable
fun rememberPositionButtonState(player: Player): PlayPositionButtonState {
    val seekForwardButtonState = remember(player) { PlayPositionButtonState(player) }
    LaunchedEffect(player) {
        seekForwardButtonState.observe()
    }
    return seekForwardButtonState
}

@UnstableApi
class PlayPositionButtonState(private val player: Player) {
    private val scope = CoroutineScope(Dispatchers.Main)

    var seek by mutableStateOf(0L)
        private set

    var duration by mutableStateOf(0L)
        private set
    private var seekJob: Job? = null

    suspend fun observe() {
        player.listen { events ->
            if (events.contains(Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {
                if (player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
                    this@PlayPositionButtonState.duration = player.duration
                }
            }
            if (events.containsAny(Player.EVENT_IS_PLAYING_CHANGED)) {
                if (player.isPlaying) {
                    updateSeek()
                } else {
                    seekJob?.cancel()
                }
            }
        }
    }

    private fun updateSeek() {
        seekJob = scope.launch {
            while (isActive) {
                seek = player.currentPosition
                delay(500)
            }
        }
    }

    fun close() {
        scope.cancel()
    }

}
