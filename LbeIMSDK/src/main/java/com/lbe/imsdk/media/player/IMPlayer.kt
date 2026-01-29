package com.lbe.imsdk.media.player

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.media3.common.util.*
import androidx.media3.ui.compose.*
import com.lbe.imsdk.R
import com.lbe.imsdk.media.player.widgets.SeekBar

/**
 *
 * @Date 2025-09-05
 */

internal val LocalIMPlayerManager = compositionLocalOf<IMPlayerManager> {
    error("CompositionLocal IMPlayerManager not present")
}


@UnstableApi
@Composable
fun IMPlayer(
    modifier: Modifier = Modifier,
    manager: IMPlayerManager,
    width: Int? = null,
    height: Int? = null
) {
    val videoSize = manager.videoSize.collectAsState().value
    val isPlaying = manager.isPlaying.collectAsState().value
    val isBuffering = manager.isBuffering.collectAsState().value
    CompositionLocalProvider(
        LocalIMPlayerManager provides manager
    ) {
        Box(
            Modifier
                .wrapContentSize()
                .then(modifier)
        ) {
            PlayerSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (null != videoSize && videoSize.width > 0 && videoSize.height > 0) {
                            Modifier.aspectRatio(
                                videoSize.width / videoSize.height.toFloat()
                            )
                        } else if (null != width && null != height && width > 0 && height > 0) {
                            Modifier.aspectRatio(
                                width.toFloat() / height.toFloat()
                            )
                        } else Modifier.fillMaxWidth()
                    )
                    .background(Color.Black)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            if (isBuffering) {
                                return@clickable
                            }
                            if (isPlaying) {
                                manager.pause()
                            } else {
                                manager.play()
                            }
                        }),
                player = manager.exoPlayer
            )

            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(45.dp),
                    color = Color.White
                )
            } else {
                if (!isPlaying) {
                    IconButton(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = {
                            manager.play()
                        }) {
                        Image(
                            painter = painterResource(R.drawable.ic_play),
                            contentDescription = "播放",
                            modifier = Modifier
                                .size(45.dp)
                        )
                    }
                }
            }
//            SeekBar(
//                modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//                    .padding(bottom = 20.dp)
//            )
        }
    }
}