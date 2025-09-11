package com.lbe.imsdk.media.player

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.media3.common.util.*
import androidx.media3.ui.compose.*
import com.lbe.imsdk.R

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
    val isPlaying = manager.isPlaying.collectAsState()
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
                    .clickable(onClick = {
                        if (isPlaying.value) {
                            manager.pause()
                        } else {
                            manager.play()
                        }
                    }),
                player = manager.exoPlayer
            )

            if (!isPlaying.value) {
                IconButton(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        manager.play()
                    }) {
                    Image(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = "播放",
                        modifier = Modifier
                            .size(32.dp)
                    )
                }
            }
//            SeekBar(modifier
//                .fillMaxWidth()
//                .align(Alignment.BottomCenter))
        }
    }
}