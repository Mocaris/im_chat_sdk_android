package com.lbe.imsdk.pages.conversation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.lifecycle.viewmodel.compose.*
import com.lbe.imsdk.pages.conversation.vm.ConversationListVM

/**
 * 会话列表页面
 *
 * @Date 2025-07-16
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListPage(
    sessionId: String,
    conversationVM: ConversationListVM = viewModel()
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("消息列表")
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

        }
    }
}