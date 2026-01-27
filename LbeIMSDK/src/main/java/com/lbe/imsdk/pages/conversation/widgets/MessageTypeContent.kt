package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lbe.imsdk.R
import com.lbe.imsdk.pages.conversation.widgets.TextContent
import com.lbe.imsdk.pages.conversation.widgets.content.MessageAnswerContentView
import com.lbe.imsdk.pages.conversation.widgets.content.MessageFaqContentView
import com.lbe.imsdk.pages.conversation.widgets.content.MessageImageContentView
import com.lbe.imsdk.pages.conversation.widgets.content.MessageKnowledgeContentView
import com.lbe.imsdk.pages.conversation.widgets.content.MessageVideoContentView
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType

//keep import R

/**
 *
 * @Date 2025-08-29
 */
@Composable
fun MessageTypeContent(imMsg: IMMessageEntry) {
    SelectionContainer(
        modifier = Modifier
            .wrapContentSize(),
    ) {
        Column {
//            Text("Message type: ${imMsg.msgType} ${imMsg.status} ${imMsg.sendStatus}")
            when (imMsg.msgType) {
                IMMsgContentType.TextContentType -> {
                    TextContent(imMsg.msgBody)
                }

                IMMsgContentType.ImgContentType -> {
                    val content = imMsg.localTempSource ?: imMsg.mediaBodyContent.value
                    if (null != content) {
                        MessageImageContentView(content, imMsg.localTempSource)
                    }
                }

                IMMsgContentType.VideoContentType -> {
                    val content = imMsg.localTempSource ?: imMsg.mediaBodyContent.value
                    if (null != content) {
                        MessageVideoContentView(content, imMsg.localTempSource)
                    }
                }

                IMMsgContentType.FaqContentType -> {
                    val content = imMsg.faqBodyContent
                    if (null != content) {
                        MessageFaqContentView(content)
                    }
                }

                IMMsgContentType.KnowledgePointContentType -> {
                    val content = imMsg.faqKnowledgePointContent
                    MessageKnowledgeContentView(imMsg.title, content ?: emptyList())
                }

                IMMsgContentType.KnowledgeAnswerContentType -> {
                    val content = imMsg.faqAnswerBodyContent
                    MessageAnswerContentView(content ?: emptyList())
                }

                IMMsgContentType.SystemContentType -> {
                    TextContent(imMsg.msgBody)
                }

                IMMsgContentType.InvalidContentType -> {
                    TextContent("[${stringResource(R.string.not_support_msg_type)}]")
                }

//            IMMsgContentType.CreateSessionContentType
                else -> {}
            }
        }
    }
}
