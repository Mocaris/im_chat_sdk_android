package com.lbe.imsdk.pages.conversation.widgets.message.content

import androidx.annotation.OptIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.media3.common.util.*
import com.lbe.imsdk.R
import com.lbe.imsdk.provider.LocalConversationVM
import com.lbe.imsdk.provider.LocalDialogManager
import com.lbe.imsdk.provider.LocalIMMessageEntry
import com.lbe.imsdk.repository.db.entry.isSelfSender
import com.lbe.imsdk.repository.remote.model.MediaMessageContent
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus
import com.lbe.imsdk.widgets.IMImageView
import com.lbe.imsdk.widgets.IMUploadIndicator

// 限制缩率图 大小
private fun Modifier.mediaThumbSize(ratio: Float): Modifier {
    return this
        .fillMaxWidth(
            when {
                ratio < 1 -> 0.5f
                ratio >= 1 -> 0.85f
//                        ratio > 3 -> 1f
//                        ratio > 2 -> 0.8f
//                        ratio < 0.5 -> 0.4f
                else -> 0.6f
            }
        )
        .aspectRatio(ratio)
}


///图片缩略图
@OptIn(UnstableApi::class)
@Composable
fun MessageImageContentView(
    content: MediaMessageContent,
    localTemp: MediaMessageContent? = null,
    centerContent: (@Composable BoxScope.() -> Unit)? = null
) {
    val ratio = content.width.toFloat() / content.height.toFloat()
    val conversationVM = LocalConversationVM.current
    val iMMessageEntry = LocalIMMessageEntry.current
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        val dialogManager = LocalDialogManager.current
        IMImageView(
            key = localTemp?.thumbnail?.key ?: content.thumbnail.key,
            url = localTemp?.thumbnail?.url ?: content.thumbnail.url,
            thumbnail = 0.7f,
            modifier = Modifier
//                .mediaThumbSize(ratio)
                .aspectRatio(ratio)
                .background(Color.Gray.copy(alpha = 0.1f))
                .clickable(onClick = {
                    conversationVM.previewMedia(iMMessageEntry, dialogManager)
                }),
        )
        iMMessageEntry.let {
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
        centerContent?.invoke(this)
    }
}

///视频缩略图
@Composable
fun MessageVideoContentView(content: MediaMessageContent, localTemp: MediaMessageContent? = null) {
    MessageImageContentView(content, localTemp) {
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
