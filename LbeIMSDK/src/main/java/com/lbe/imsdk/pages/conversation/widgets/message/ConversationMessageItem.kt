package com.lbe.imsdk.pages.conversation.widgets.message

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
import com.lbe.imsdk.R
import com.lbe.imsdk.*
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageSystemContentView
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageUserAgentContentView
import com.lbe.imsdk.provider.*
import com.lbe.imsdk.repository.db.entry.*
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus
import java.util.*

/**
 * 消息 item
 * @Date 2025-08-20
 */

val excludeDecorationMsgType = listOf(
    IMMsgContentType.IMAGE_CONTENT_TYPE,
    IMMsgContentType.VIDEO_CONTENT_TYPE,
)

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
                IMMsgContentType.AGENT_USER_JOIN_SESSION_CONTENT_TYPE -> {
                    val content = imMsg.userArentContent
                    MessageUserAgentContentView(content)
                }

                IMMsgContentType.END_SESSION_CONTENT_TYPE -> {
                    MessageSystemContentView(stringResource(R.string.chat_session_status_6))
                }

                IMMsgContentType.ANWSER_TIMEOUT_CONTENT_TYPE -> {
                    val content = imMsg.answerTimeoutContent
                    MessageSystemContentView(
                        stringResource(
                            R.string.chat_session_status_3,
                            content?.timeout ?: "1"
                        )
                    )
                }

                IMMsgContentType.RANKING_CONTENT_TYPE -> {
                    val content = imMsg.rankingBodyContent
                    MessageSystemContentView(
                        stringResource(
                            R.string.chat_session_status_2,
                            content?.number ?: 1
                        )
                    )
                }

                IMMsgContentType.TRANSFER_CONTENT_TYPE -> {
                    MessageSystemContentView(stringResource(R.string.chat_session_status_5))
                }

                IMMsgContentType.UNSUPPORTED_CONTENT_TYPE -> {
                    MessageSystemContentView(stringResource(R.string.chat_session_status_7))
                }

                else -> {
                    MessageRawItem(imMsg)
                }
            }
        }
    }
}

@Composable
fun MessageRawItem(
    imMsg: IMMessageEntry
) {
    val isSelfSend = imMsg.isSelfSender()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(40.dp)) {
            if (!isSelfSend) {
                Avatar(source = imMsg.senderFaceUrl, isSelf = false)
            }
        }
        MessageBodyItem(imMsg)
        Box(modifier = Modifier.size(40.dp)) {
            if (isSelfSend) {
                Avatar(source = imMsg.senderFaceUrl, isSelf = true)
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
    if (!isSelfSend) {
        val conversationVM = LocalConversationVM.current
        LaunchedEffect(imMsg) {
            if (imMsg.shouldMarkRead) {
                conversationVM.markRead(imMsg)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = if (isSelfSend) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (isSelfSend) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            // 用户昵称
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
        MessageItemDirection(
            imMsg,
            constraintsMsgTypes = excludeDecorationMsgType
        ) {
            if (imMsg.isSelfSender()) {
                MessageStatus(imMsg)
            }
            MessageContentDecoration(
                imMsg,
                excludeMsgType = excludeDecorationMsgType
            ) {
                MessageTypeContent(imMsg)
            }
        }
    }
}

@Composable
fun MessageStatus(
    imMsg: IMMessageEntry,
) {
    if (imMsg.sendMutableState.intValue == IMMsgSendStatus.FAILURE) {
        val conversationVM = LocalConversationVM.current
        Image(
            painter = painterResource(R.drawable.ic_send_fail),
            contentDescription = "发送失败",
            modifier = Modifier
                .size(15.dp)
                .clickable(onClick = {
                    conversationVM.reSendMessage(imMsg)
                })
        )
    }

    if (imMsg.readMutableState.intValue == IMMsgReadStatus.READ) {
        Image(
            painter = painterResource(R.drawable.ic_readed),
            contentDescription = "已读",
            modifier = Modifier.size(15.dp)
        )
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