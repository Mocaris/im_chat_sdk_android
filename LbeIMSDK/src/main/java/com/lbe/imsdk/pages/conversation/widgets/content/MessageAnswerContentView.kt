package com.lbe.imsdk.pages.conversation.widgets.content

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.util.UnstableApi
import com.lbe.imsdk.pages.conversation.preview.MediaPreviewDialog
import com.lbe.imsdk.pages.conversation.preview.MediaPreviewInfo
import com.lbe.imsdk.provider.LocalDialogManager
import com.lbe.imsdk.repository.remote.model.FaqAnswerMessageContent
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.widgets.IMImageView

/**
 *
 * 知识库 答案
 * @Date 2025-08-29
 */
@SuppressLint("UnsafeOptInUsageError")
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
                        val dialogManager = LocalDialogManager.current
                        IMImageView(
                            key = it.key,
                            url = it.url,
                            thumbnail = 0.8f,
                            modifier = Modifier
                                .wrapContentSize()
                                .clickable(onClick = {
                                    val imageList =
                                        list.filter { it.type == FaqAnswerMessageContent.TYPE_IMAGE }
                                    val imgIndex = imageList.indexOf(item).coerceAtLeast(0)
                                    dialogManager.show(
                                        onDismissRequest = { it() },
                                        properties = DialogProperties(usePlatformDefaultWidth = false)
                                    ) {
                                        MediaPreviewDialog(
                                            index = imgIndex, list =
                                                imageList.map {
                                                    MediaPreviewInfo(
                                                        type = MediaPreviewInfo.PreType.Image,
                                                        sourceUrl = SourceUrl(
                                                            key = it.imageUrl?.key ?: "",
                                                            url = it.imageUrl?.url ?: ""
                                                        )
                                                    )
                                                }
                                        )
                                    }
                                })
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
                    MessageTextContentView(item.content)
                }
            }
        }
    }
}