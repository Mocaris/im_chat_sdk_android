package com.lbe.imsdk.pages.conversation.widgets.message

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lbe.imsdk.R
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageAnswerContentView
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageFaqContentView
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageImageContentView
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageKnowledgeContentView
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageTextContentView
import com.lbe.imsdk.pages.conversation.widgets.message.content.MessageVideoContentView
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType

/**
 *
 * @Date 2025-08-29
 */
@Composable
fun MessageTypeContent(imMsg: IMMessageEntry) {
    SelectionContainer() {
        Column {
            when (imMsg.msgContentType) {
                IMMsgContentType.SystemContentType, IMMsgContentType.TextContentType -> {
                    MessageTextContentView(imMsg.msgBody)
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

                IMMsgContentType.InvalidContentType, IMMsgContentType.UNRECOGNIZED -> {
                    MessageTextContentView("[${stringResource(R.string.not_support_msg_type)}]")
                }

//            IMMsgContentType.CreateSessionContentType
                else -> {}
            }
        }
    }
}
