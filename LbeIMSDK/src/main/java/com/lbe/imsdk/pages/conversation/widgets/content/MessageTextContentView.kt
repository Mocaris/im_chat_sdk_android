package com.lbe.imsdk.pages.conversation.widgets.content

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.R
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.provider.LocalThemeColors

/**
 *
 * @Author mocaris
 * @Date 2026-01-28
 * @Since
 */
@Composable
fun MessageTextContentView(content: String) {
    val themeColors = LocalThemeColors.current
    val expand = rememberSaveable { mutableStateOf(false) }
    Text(
        text = buildAnnotatedString {
            append(content.let {
                if (expand.value) it
                else
                    it.let {
                        if (it.length > LbeIMSDKManager.TEXT_CONTENT_LENGTH)
                            it.take(LbeIMSDKManager.TEXT_CONTENT_LENGTH).plus("...")
                        else it
                    }
            })
            if (content.length > LbeIMSDKManager.TEXT_CONTENT_LENGTH) {
                withLink(LinkAnnotation.Clickable(tag = "expand", linkInteractionListener = {
                    expand.value = !expand.value
                })) {
                    withStyle(SpanStyle(color = Color.Blue.copy(alpha = 0.8f))) {
                        append(expand.value.let {
                            if (it) stringResource(R.string.content_collapse)
                            else
                                stringResource(R.string.content_expand)
                        })
                    }
                }
            }
        }, modifier = Modifier
            .wrapContentSize(),
        style = TextStyle(
            fontSize = 14.sp,
            color = themeColors.conversationTextColor
        )
    )
}