package com.lbe.imsdk.pages.conversation.widgets.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.R
import com.lbe.imsdk.repository.remote.model.AgentUser
import com.lbe.imsdk.repository.remote.model.MediaMessageContent
import com.lbe.imsdk.widgets.IMImageView

/**
 * 客服接入
 * @Date 2025-08-29
 */
@Composable
fun MessageUserAgentContentView(content: AgentUser?) {
    val faceUrl = content?.parseFaceUrl
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            stringResource(
                R.string.chat_session_status_4, content?.username ?: ""
            ),//"${csJoinInfo.username} 将为您服务",
            modifier = Modifier
                .padding(top = 16.dp)
                .background(Color.White, RoundedCornerShape(5.dp))
                .padding(top = 20.dp)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            style = TextStyle(
                fontSize = 10.sp,
            )
        )
        IMImageView(
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape),
            key = faceUrl?.key,
            url = faceUrl?.url ?: "",
            error = painterResource(R.drawable.ic_default_cs_avatar)
        )
    }
}