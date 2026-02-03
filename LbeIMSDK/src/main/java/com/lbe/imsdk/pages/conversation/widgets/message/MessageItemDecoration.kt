package com.lbe.imsdk.pages.conversation.widgets.message

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lbe.imsdk.provider.LocalThemeColors
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.db.entry.isSelfSender
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType

/**
 * 消息item装饰
 * @Date 2025-08-29
 */

val selfShape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
val toShape = RoundedCornerShape(topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 10.dp)

@Composable
fun MessageContentDecoration(
    imMsg: IMMessageEntry,
    // decoration 忽略的 IMMsgContentType
    excludeMsgType: List<IMMsgContentType>,
    content: @Composable BoxScope.() -> Unit
) {
    val isSelfSender = imMsg.isSelfSender()
    val themeColors = LocalThemeColors.current
    val density = LocalDensity.current
    val contentBgColor = if (isSelfSender)
        themeColors.conversationSelfBgColor
    else
        themeColors.conversationFromBgColor
    val shape = if (isSelfSender) selfShape else toShape
    Box(
        modifier = Modifier
            .wrapContentSize()
            .then(
                imMsg.cacheLayoutHeight?.let {
                    Modifier.sizeIn(minHeight = it)
                } ?: Modifier.onSizeChanged {
                    imMsg.cacheLayoutHeight = with(density) {
                        it.height.toDp()
                    }
                })
            .clip(shape)
            .animateContentSize()
            .then(
                if (excludeMsgType.contains(imMsg.msgContentType))
                    Modifier
                else Modifier
                    .background(contentBgColor)
                    .padding(10.dp)
            ),
        content = content,
    )
}

/**
 * 布局方向
 * [constraintsMsgTypes] 限制 IMMsgContentType 布局大小
 *
 */
@Composable
fun MessageItemDirection(
    imMsg: IMMessageEntry,
    constraintsMsgTypes: List<IMMsgContentType>,
    content: @Composable @UiComposable RowScope.() -> Unit
) {
    val isSelfSender = imMsg.isSelfSender()
    val arrangement = if (isSelfSender) Arrangement.spacedBy(3.dp, Alignment.End)
    else
        Arrangement.spacedBy(3.dp, Alignment.Start)

    @Composable
    fun rowContent(maxWidth: Dp? = null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    maxWidth?.let { Modifier.heightIn(max = maxWidth) }
                        ?: Modifier.wrapContentHeight()
                ),
            horizontalArrangement = arrangement,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
    if (constraintsMsgTypes.isNotEmpty() && constraintsMsgTypes.contains(imMsg.msgContentType)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            rowContent(maxWidth)
        }
    } else {
        rowContent()
    }

}

internal data class MessageDirectionScope(val arrangement: Arrangement) {

}