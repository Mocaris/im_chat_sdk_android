package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lbe.imsdk.R
import com.lbe.imsdk.pages.conversation.widgets.content.*
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
            when (imMsg.msgType) {
                IMMsgContentType.SYSTEM_CONTENT_TYPE,
                IMMsgContentType.TEXT_CONTENT_TYPE -> {
                    MessageTextContentView(imMsg.msgBody)
                }

                IMMsgContentType.IMAGE_CONTENT_TYPE -> {
                    val content = imMsg.localTempSource ?: imMsg.mediaBodyContent.value
                    if (null != content) {
                        MessageImageContentView(content, imMsg.localTempSource)
                    }
                }

                IMMsgContentType.VIDEO_CONTENT_TYPE -> {
                    val content = imMsg.localTempSource ?: imMsg.mediaBodyContent.value
                    if (null != content) {
                        MessageVideoContentView(content, imMsg.localTempSource)
                    }
                }

                IMMsgContentType.FAQ_CONTENT_TYPE -> {
                    val content = imMsg.faqBodyContent
                    if (null != content) {
                        MessageFaqContentView(content)
                    }
                }

                IMMsgContentType.KNOWLEDGE_POINT_CONTENT_TYPE -> {
                    val content = imMsg.faqKnowledgePointContent
                    MessageKnowledgeContentView(imMsg.title, content ?: emptyList())
                }

                IMMsgContentType.KNOWLEDGE_ANSWER_CONTENT_TYPE -> {
                    val content = imMsg.faqAnswerBodyContent
                    MessageAnswerContentView(content ?: emptyList())
                }

                IMMsgContentType.INVALID_CONTENT_TYPE -> {
                    MessageTextContentView("[${stringResource(R.string.not_support_msg_type)}]")
                }

//            IMMsgContentType.CreateSessionContentType
                else -> {}
            }
        }
    }
}
