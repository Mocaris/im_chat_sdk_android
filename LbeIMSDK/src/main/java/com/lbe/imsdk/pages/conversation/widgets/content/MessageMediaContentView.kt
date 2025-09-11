package com.lbe.imsdk.pages.conversation.widgets.content

import androidx.annotation.OptIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.media3.common.util.*
import com.lbe.imsdk.R
import com.lbe.imsdk.provider.LocalIMMessageEntry
import com.lbe.imsdk.provider.LocalSessionViewModel
import com.lbe.imsdk.repository.db.entry.isSelfSender
import com.lbe.imsdk.repository.remote.model.MediaMessageContent
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus
import com.lbe.imsdk.widgets.IMImageView
import com.lbe.imsdk.widgets.IMUploadIndicator


///图片缩略图
@OptIn(UnstableApi::class)
@Composable
fun MessageImageContentView(content: MediaMessageContent, localTemp: MediaMessageContent? = null) {
    val ratio = content.width.toFloat() / content.height.toFloat()
    val conversationVM = LocalSessionViewModel.current
    val iMMessageEntry = LocalIMMessageEntry.current
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        IMImageView(
            key = content.thumbnail.key.ifEmpty { localTemp?.thumbnail?.key },
            url = content.thumbnail.url.ifEmpty { localTemp?.thumbnail?.url ?: "" },
            modifier = Modifier
                .fillMaxWidth(
                    when {
                        ratio > 3 -> 1f
                        ratio > 2 -> 0.8f
                        ratio < 0.5 -> 0.4f
                        else -> 0.6f
                    }
                )
                .aspectRatio(ratio)
                .background(Color.Gray.copy(alpha = 0.1f))
                .clickable(onClick = {
                    conversationVM.previewMedia(iMMessageEntry)
                }),
        )
        LocalIMMessageEntry.current.let {
            val sendState = it.sendMutableState.intValue
            if (it.isSelfSender()) {
                when (sendState) {
                    IMMsgSendStatus.SENDING -> {
                        val progress = it.sendProgress.floatValue
                        IMUploadIndicator(Modifier.size(32.dp), progress = { progress })
                    }

                    IMMsgSendStatus.FAILURE -> {
                        Image(
                            painterResource(R.drawable.ic_media_send_failed),
                            contentDescription = "",
                            Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

///视频缩略图
@Composable
fun MessageVideoContentView(content: MediaMessageContent, localTemp: MediaMessageContent? = null) {
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        MessageImageContentView(content, localTemp)
        LocalIMMessageEntry.current.let {
            val sendState = it.sendMutableState.intValue
            if (!it.isSelfSender() || sendState == IMMsgSendStatus.SUCCESS) {
                Image(
                    painterResource(R.drawable.ic_play),
                    contentDescription = "",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
