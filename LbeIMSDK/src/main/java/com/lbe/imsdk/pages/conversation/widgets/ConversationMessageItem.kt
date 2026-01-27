package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.*
import coil3.compose.*
import com.lbe.imsdk.*
import com.lbe.imsdk.R
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.pages.conversation.widgets.content.MessageSystemContentView
import com.lbe.imsdk.pages.conversation.widgets.content.MessageUserAgentContentView
import com.lbe.imsdk.provider.*
import com.lbe.imsdk.repository.db.entry.*
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
import java.util.Date

/**
 * 消息 item
 * @Date 2025-08-20
 */
@Composable
fun ConversationMessageItem(preMsg: IMMessageEntry?, imMsg: IMMessageEntry) {
    CompositionLocalProvider(
        LocalIMMessageEntry provides imMsg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            val sendTime = imMsg.sendDate
            if (null != sendTime) {
                val preSendTime = preMsg?.sendDate
                checkTime(preSendTime, sendTime)?.let { tt ->
                    MessageSystemContentView(tt)
                }
            }
            when (imMsg.msgType) {
                IMMsgContentType.AgentUserJoinSessionContentType -> {
                    val content = imMsg.userArentContent
                    MessageUserAgentContentView(content)
                }

                IMMsgContentType.EndSessionContentType -> {
                    MessageSystemContentView(stringResource(R.string.chat_session_status_6))
                }

                IMMsgContentType.RankingContentType -> {
                    val content = imMsg.rankingBodyContent
                    MessageSystemContentView(
                        stringResource(
                            R.string.chat_session_status_2,
                            content?.number ?: 1
                        )
                    )
                }

                IMMsgContentType.TransferContentType -> {
                    MessageSystemContentView(stringResource(R.string.chat_session_status_5))
                }

                IMMsgContentType.UnsupportedContentType -> {
                    MessageSystemContentView(stringResource(R.string.chat_session_status_7))
                }

                else -> {
                    val isSelfSend = imMsg.isSelfSender()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (!isSelfSend) {
                            Avatar(source = imMsg.senderFaceUrl, isSelf = false)
                        }
                        MessageBodyItem(imMsg)
                        if (isSelfSend) {
                            Avatar(source = imMsg.senderFaceUrl, isSelf = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.MessageBodyItem(
    imMsg: IMMessageEntry
) {
    val isSelfSend = imMsg.isSelfSender()
    // 标记已读
    if (!isSelfSend && imMsg.readMutableState.intValue != IMMsgReadStatus.READ) {
        val conversationVM = LocalCurrentConversationViewModel.current
        LaunchedEffect(imMsg) {
            conversationVM.markRead(imMsg)
        }
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (isSelfSend) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Text(
                if (isSelfSend) {
                    imMsg.senderNickname.ifEmpty { LbeIMSDKManager.sdkInitConfig?.nickName ?: "" }
                } else if (imMsg.senderUid.isNotEmpty()) {
                    imMsg.senderNickname
                } else {
                    stringResource(R.string.chat_session_status_14)
                }
            )
        }
        MessageItemDecoration(imMsg) {
            MessageTypeContent(imMsg)
        }
    }
}


private fun checkTime(preDate: Date?, date: Date?): String? {
    if (preDate == null && date == null) {
        return null
    }
    val now = Date()
    // 今天内 显示 时分
    // 三分钟内显示第一个
    val isDiff3Minutes = if (null == preDate) {
        true
    } else {
        ((date?.time ?: 0) - preDate.time) > 3 * 60 * 1000
    }
    if (!isDiff3Minutes) {
        return null
    }
    val isToday = date?.year == now.year && date.month == now.month && date.day == now.day
    return if (isToday) {
        date.toHM()
    } else {
        date?.toyyyMMddHHmm()
    }
}