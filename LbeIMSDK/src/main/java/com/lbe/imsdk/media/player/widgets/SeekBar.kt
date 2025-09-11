package com.lbe.imsdk.media.player.widgets

import androidx.annotation.OptIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.media3.common.util.*
import com.lbe.imsdk.media.player.LocalIMPlayerManager

/**
 *
 * @Author mocaris
 * @Date 2025-09-08
 */
@OptIn(UnstableApi::class)
@Composable
fun SeekBar(modifier: Modifier = Modifier) {
    val playerManager = LocalIMPlayerManager.current
    val duration = playerManager.duration.collectAsState().value
    val position = playerManager.position.collectAsState().value
    Slider(
        modifier = modifier,
        enabled = duration > 0,
        value = duration.let {
            if (it > 0) {
                ((100 * position) / duration) / 100f
            } else 0f
        },
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTickColor = Color.White,
        ),
        onValueChange = {
            playerManager.seekTo(it)
        })
}