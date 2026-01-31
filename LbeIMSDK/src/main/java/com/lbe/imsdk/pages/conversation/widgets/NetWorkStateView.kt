package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.R
import com.lbe.imsdk.provider.LocalConversationVM
import com.lbe.imsdk.provider.LocalThemeColors

/**
 *  网络状态
 * @Date 2025-09-05
 */
@Composable
fun NetWorkStateView() {
    val netState = LocalConversationVM.current
        .networkMonitor
        .isConnected
        .collectAsState().value
//    val socketState = LbeIMSDKManager.socketManager?.connectState?.collectAsState()?.value
    AnimatedVisibility(
        visible = !netState /*|| socketState.isClosed==true*/,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red.copy(0.1f))
                .padding(vertical = 15.dp, horizontal = 15.dp)
        ) {
            Image(
                painterResource(R.drawable.ic_network_unavailable),
                contentDescription = "",
                modifier = Modifier.size(16.dp)
            )
            Text(
                stringResource(R.string.chat_session_status_9),
                modifier = Modifier
                    .wrapContentSize(),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = LocalThemeColors.current.conversationSystemTextColor
                )
            )
        }
    }
}