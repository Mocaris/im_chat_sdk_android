package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.R
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.provider.LocalDialogManager
import com.lbe.imsdk.provider.LocalSDKInitConfig
import com.lbe.imsdk.provider.LocalThemeColors
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.widgets.IMImageView

/**
 *
 * @Date 2025-09-03
 */

@Composable
fun Avatar(source: Any? = null, isSelf: Boolean = false) {
    var key: String? = null
    var url: String = ""
    if (source is SourceUrl) {
        key = source.key
        url = source.url
    } else {
        url = source?.toString() ?: ""
    }
    if (url.isEmpty() && isSelf) {
        val selfHeader = LocalSDKInitConfig.current.parseHeaderIcon
        if (selfHeader is SourceUrl) {
            key = selfHeader.key
            url = selfHeader.url
        } else {
            url = selfHeader.toString()
        }
    }
    IMImageView(
        key = key,
        url = url,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        placeholder = painterResource(id = if (isSelf) R.drawable.ic_default_user_avatar else R.drawable.ic_cs_avatar),
        error = painterResource(
            id =
                if (isSelf || url.isNotEmpty()) R.drawable.ic_default_user_avatar else R.drawable.ic_robots_avatar
        )
    )
}

// 文本
@Composable
fun TextContent(content: String) {
    val themeColors = LocalThemeColors.current
    val expand = remember { mutableStateOf(false) }
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