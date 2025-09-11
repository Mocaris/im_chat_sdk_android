package com.lbe.imsdk.pages.conversation.widgets.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.pages.conversation.widgets.TextContent
import com.lbe.imsdk.repository.remote.model.FaqAnswerMessageContent
import com.lbe.imsdk.widgets.IMImageView

/**
 *
 * 知识库 答案
 * @Date 2025-08-29
 */
@Composable
fun MessageAnswerContentView(list: List<FaqAnswerMessageContent>) {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        list.forEachIndexed { index, item ->
            when (item.type) {
                FaqAnswerMessageContent.TYPE_IMAGE -> {
                    item.imageUrl?.let {
                        IMImageView(
                            key = it.key,
                            url = it.url,
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )
                    }
                }

                FaqAnswerMessageContent.TYPE_LINK -> {
                    item.linkList?.let {
                        Text(buildAnnotatedString {
                            for (link in it) {
                                if (link.url.isNotEmpty()) {
                                    withLink(LinkAnnotation.Url(link.url)) {
                                        withStyle(
                                            style = SpanStyle(
                                                fontSize = 14.sp,
                                                color = Color.Blue
                                            )
                                        ) {
                                            append(link.content)
                                        }
                                    }
                                } else {
                                    withStyle(
                                        style = SpanStyle(
                                            fontSize = 14.sp,
                                        )
                                    ) {
                                        append(link.content)
                                    }
                                }
                            }
                        })
                    }
                }

                else -> {
                    TextContent(item.content)
                }
            }
        }
    }
}