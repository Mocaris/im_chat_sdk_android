package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lbe.imsdk.R
import com.lbe.imsdk.provider.LocalSDKInitConfig
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.widgets.IMImageView

/**
 *
 * @Date 2025-09-03
 */

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    source: Any? = null, isSelf: Boolean = false
) {
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
            .clip(CircleShape)
            .then(modifier),
        placeholder = painterResource(id = if (isSelf) R.drawable.ic_default_user_avatar else R.drawable.ic_cs_avatar),
        error = painterResource(
            id =
                if (isSelf || url.isNotEmpty()) R.drawable.ic_default_user_avatar else R.drawable.ic_robots_avatar
        )
    )
}