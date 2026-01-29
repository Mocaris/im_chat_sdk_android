package com.lbe.imsdk.pages.conversation.preview

import androidx.annotation.OptIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.*
import androidx.media3.common.util.*
import com.lbe.imsdk.R
import com.lbe.imsdk.extension.appContext
import com.lbe.imsdk.extension.getFileName
import com.lbe.imsdk.extension.launchIO
import com.lbe.imsdk.extension.getMD5
import com.lbe.imsdk.extension.showToast
import com.lbe.imsdk.manager.DownloadManager
import com.lbe.imsdk.media.player.IMPlayer
import com.lbe.imsdk.media.player.IMPlayerManager
import com.lbe.imsdk.provider.LocalDialogManager
import com.lbe.imsdk.provider.LocalThemeColors
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.db.entry.isVideoType
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.widgets.IMImageView
import kotlinx.coroutines.*

/**
 * 预览
 * @Date 2025-09-05
 */
data class MediaPreviewInfo(
    val autoPlay: Boolean = true,
    val thumbnail: SourceUrl? = null,
    val sourceUrl: SourceUrl,
    val width: Int? = null,
    val height: Int? = null,
    val type: PreType,
) {
    enum class PreType {
        Image, Video,
    }
}

@OptIn(UnstableApi::class)
@Composable
fun MediaMessagePreViewDialog(
    index: Int = 0,
    list: List<IMMessageEntry>,
) {
    MediaPreviewDialog(
        index, list.map { content ->
            content.mediaBodyContent.value!!.let {
                MediaPreviewInfo(
                    thumbnail = SourceUrl(key = it.thumbnail.key.ifEmpty {
                    content.localTempSource?.thumbnail?.key ?: ""
                }, url = it.thumbnail.url.ifEmpty {
                    content.localTempSource?.thumbnail?.url ?: ""
                }),
                    sourceUrl = SourceUrl(key = it.resource.key.ifEmpty {
                        content.localTempSource?.resource?.key ?: ""
                    }, url = it.resource.url.ifEmpty {
                        content.localTempSource?.resource?.url ?: ""
                    }),
                    width = it.width,
                    height = it.height,
                    type = if (content.isVideoType()) MediaPreviewInfo.PreType.Video else MediaPreviewInfo.PreType.Image)
            }
        })
}


@UnstableApi
@Composable
fun MediaPreviewDialog(
    index: Int = 0,
    list: List<MediaPreviewInfo>,
) {
    val pagerState = rememberPagerState(initialPage = index) { list.size }
    HorizontalPager(
        modifier = Modifier.fillMaxSize(), state = pagerState
    ) { index ->
        PreviewContent(list[index])
    }
}

@UnstableApi
@Composable
private fun PreviewContent(preInfo: MediaPreviewInfo) {
    val saveProgress = remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope(getContext = { Dispatchers.IO })
    val dialogManager = LocalDialogManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .safeContentPadding(),
        contentAlignment = Alignment.Center
    ) {
        when (preInfo.type) {
            MediaPreviewInfo.PreType.Image -> {
                IMImageView(
                    modifier = Modifier.fillMaxSize(),
                    preInfo.sourceUrl.key,
                    preInfo.sourceUrl.url,
                    contentScale = ContentScale.Fit,
                )
            }

            MediaPreviewInfo.PreType.Video -> {
                val context = LocalContext.current
                val player = remember { IMPlayerManager(context, preInfo.sourceUrl.url) }
                DisposableEffect(player) {
                    if (preInfo.autoPlay) {
                        player.play()
                    }
                    onDispose {
                        player.close()
                    }
                }
                IMPlayer(
                    manager = player, width = preInfo.width, height = preInfo.height
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (saveProgress.intValue in 1..<100) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), progress = {
                        saveProgress.intValue / 100f
                    })
            } else {
                Text(
                    stringResource(R.string.chat_session_status_18),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(LocalThemeColors.current.conversationSystemTextColor)
                        .clickable(onClick = {
                            scope.launchIO {
                                saveToGallery(preInfo, saveProgress)
                            }
                        })
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    style = TextStyle(fontSize = 12.sp, color = Color.White)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LocalThemeColors.current.conversationSystemTextColor)
                    .clickable(onClick = {
                        dialogManager.dismiss()
                    }), contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(R.drawable.ic_media_close),
                    contentDescription = stringResource(R.string.content_description_close),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}


private suspend fun saveToGallery(
    preInfo: MediaPreviewInfo, saveProgress: MutableIntState
) {
    try {
        preInfo.let {
            val url = it.sourceUrl.url
            val fileName = url.getFileName().ifEmpty {
                if (preInfo.type == MediaPreviewInfo.PreType.Image) {
                    "${url.getMD5()}.jpg"
                } else {
                    "${url.getMD5()}.mp4"
                }
            }
            DownloadManager.downloadSaveToGallery(url, fileName) { p ->
                saveProgress.intValue = p
            }
        }
        appContext.getString(R.string.save_success).showToast()
    } catch (e: Exception) {
        appContext.getString(R.string.save_fail).showToast()
    }
}