package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lbe.imsdk.R
import com.lbe.imsdk.provider.LocalCurrentConversationViewModel
import com.lbe.imsdk.provider.LocalThemeColors
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.db.entry.isSelfSender
import com.lbe.imsdk.repository.model.proto.IMMsg
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus

/**
 * 消息item装饰
 * @Date 2025-08-29
 */

val selfShape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
val toShape = RoundedCornerShape(topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 10.dp)

@Composable
fun MessageItemDecoration(
    imMsg: IMMessageEntry,
    content: @Composable BoxScope.() -> Unit
) {
    val msgType = imMsg.msgType
    val isSelfSender = imMsg.isSelfSender()
    val themeColors = LocalThemeColors.current
    val density = LocalDensity.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = if (isSelfSender) Arrangement.spacedBy(5.dp, Alignment.End)
        else
            Arrangement.spacedBy(5.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MessageStatus(imMsg)
        Box(
            modifier = Modifier
                .wrapContentSize()
                .then(
                    if (imMsg.layoutCacheSize > 0.dp)
                        Modifier.sizeIn(minHeight = imMsg.layoutCacheSize)
                    else Modifier.onSizeChanged {
                        imMsg.layoutCacheSize = with(density) {
                            it.height.toDp()
                        }
                    })
                .then(
                    if (
                        msgType == IMMsg.ContentType.ImgContentType_VALUE ||
                        msgType == IMMsg.ContentType.VideoContentType_VALUE
                    ) Modifier else Modifier
                        .background(
                            if (isSelfSender)
                                themeColors.conversationSelfBgColor
                            else
                                themeColors.conversationFromBgColor,
                            shape = if (isSelfSender) selfShape else toShape
                        )
                        .padding(10.dp)
                ),
            content = content,
        )
    }
}

@Composable
fun MessageStatus(
    imMsg: IMMessageEntry,
) {
    if (imMsg.isSelfSender()) {
        if (imMsg.sendMutableState.intValue == IMMsgSendStatus.FAILURE) {
            val conversationVM = LocalCurrentConversationViewModel.current
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
}