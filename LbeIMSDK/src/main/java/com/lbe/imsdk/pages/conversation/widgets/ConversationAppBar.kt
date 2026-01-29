package com.lbe.imsdk.pages.conversation.widgets

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.lbe.imsdk.R
import com.lbe.imsdk.extension.canPop
import com.lbe.imsdk.extension.pop
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.pages.navigation.PageRoute
import com.lbe.imsdk.provider.LocalConversationStateViewModel
import com.lbe.imsdk.provider.LocalCurrentConversationViewModel

/**
 *
 * @Author mocaris
 * @Date 2026-01-29
 * @Since
 */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun IMAppBar() {
    val holderVM = LocalConversationStateViewModel.current
    val conversationVM = LocalCurrentConversationViewModel.current
    val appBarColors = TopAppBarDefaults.topAppBarColors()
    val view = LocalView.current
    val context = LocalContext.current
    val socketState = LbeIMSDKManager.socketManager?.connectState?.collectAsState()?.value

    SideEffect {
        if (context is Activity) {
            val isLight = appBarColors.containerColor.luminance() > 0.5f
            WindowCompat.getInsetsController(context.window, view).isAppearanceLightStatusBars =
                isLight
        }
    }
    CenterAlignedTopAppBar(
        title = {
            Text(stringResource(R.string.chat_session_status_1))
        },
        colors = appBarColors,
        navigationIcon = {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (PageRoute.routes.canPop()) {
                        PageRoute.routes.pop()
                    } else if (context is Activity) {
                        context.finish()
                    }
                }) {
                    Image(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "返回",
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (socketState?.isConnecting == true) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }, actions = {
            if (!conversationVM.endSession.value && conversationVM.isCustomerService.value) {
                CloseCustomerServiceButton(onClick = {
                    holderVM.endSession()
                })
            }
            Spacer(modifier = Modifier.width(16.dp))
//            IconButton(onClick = {
//                conversationVM.serviceSupport()
//            }) {
//                Image(
//                    painter = painterResource(R.drawable.ic_cs),
//                    modifier = Modifier.size(24.dp),
//                    contentDescription = "人工客服",
//                )
//            }
        })
}
