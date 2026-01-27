package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import com.lbe.imsdk.R
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.provider.LocalConversationStateViewModel
import com.lbe.imsdk.provider.LocalCurrentConversationViewModel
import com.lbe.imsdk.provider.LocalThemeColors
import com.lbe.imsdk.service.http.SocketClient

/**
 *  网络状态
 * @Date 2025-09-05
 */
@Composable
fun NetWorkStateView() {
    val netState = LocalCurrentConversationViewModel.current.netState.value
    val socketState = LbeIMSDKManager.socketManager?.connectState?.collectAsState()?.value
    AnimatedVisibility(
        visible = !netState || (socketState != SocketClient.ConnectState.CONNECTING && socketState != SocketClient.ConnectState.OPENED),
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