package com.lbe.imsdk.pages.conversation

import android.*
import android.app.*
import android.os.*
import androidx.activity.compose.*
import androidx.activity.result.*
import androidx.activity.result.contract.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.*
import com.lbe.imsdk.R
import com.lbe.imsdk.extension.pop
import com.lbe.imsdk.pages.conversation.vm.ConversationVM
import com.lbe.imsdk.pages.conversation.widgets.ConversationMessageItem
import com.lbe.imsdk.pages.conversation.widgets.KeyboardInputBox
import com.lbe.imsdk.pages.conversation.widgets.NetWorkStateView
import com.lbe.imsdk.pages.navigation.PageRoute
import com.lbe.imsdk.provider.LocalSessionViewModel
import com.lbe.imsdk.provider.LocalSession
import com.lbe.imsdk.provider.LocalThemeColors
import com.lbe.imsdk.provider.rememberKeyboardState
import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
import com.lbe.imsdk.widgets.IMRefresh
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 会话页面
 *
 * @Date 2025-07-16
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationPage(
    sessionData: CreateSessionResModel.SessionData, conversationVM: ConversationVM = viewModel(
        key = sessionData.sessionId, initializer = {
            ConversationVM(sessionData)
        })
) {
    CompositionLocalProvider(
        LocalSession provides sessionData,
        LocalSessionViewModel provides conversationVM,
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                IMAppBar()
            }) {
            ConversationPageBody(conversationVM, it)
        }
    }
}

@Composable
private fun ConversationPageBody(conversationVM: ConversationVM, padding: PaddingValues) {
    val listState = rememberLazyListState()

    val userScrolling = remember { mutableStateOf(false) }
    suspend fun scrollToBottom(anim: Boolean = true) {
        if (!listState.canScrollForward) {
            return
        }
        if (conversationVM.msgList.isEmpty()) {
            return
        }
        if (anim) {
            listState.animateScrollToItem(conversationVM.msgCount - 1)
        } else {
            listState.scrollToItem(conversationVM.msgCount - 1)
        }
    }

    val keyboardState = rememberKeyboardState()
    //键盘弹起时

    LaunchedEffect(keyboardState) {
        if (keyboardState.isOpened()) {
            delay(100)
            conversationVM.scrollToBottom(anim = true)
        }
    }
    // 有新消息时
    LaunchedEffect(conversationVM.newMessageCount) {
        snapshotFlow { conversationVM.newMessageCount.intValue }
            .distinctUntilChanged()
            .collect { size ->
                if (size > 0 && !userScrolling.value) {
                    delay(200)
                    conversationVM.scrollToBottom(anim = true)
                }
            }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect {
            it?.let {
                conversationVM.lastVisibleIndex(it)
            }
        }
    }

    val eventAnimValue = conversationVM.toBottomEventAnim.intValue
    LaunchedEffect(eventAnimValue) {
        scrollToBottom()
    }

    val eventNoAnimValue = conversationVM.toBottomEventNoAnim.intValue
    LaunchedEffect(eventNoAnimValue) {
        scrollToBottom(anim = false)
    }

    val requestPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { it ->
            if (null != it) {
                conversationVM.sendMediaMessage(it)
            }
        },
    )
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { it ->
            if (it.any { !it.value }) {
                return@rememberLauncherForActivityResult
            }
            requestPhotoLauncher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo,
                )
            )
        },
    )
    val timeoutReply = conversationVM.timeOutReply.value

    fun pickPhoto() {
        requestPermissionLauncher.launch(
            mutableStateListOf<String>().apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    plus(Manifest.permission.READ_MEDIA_IMAGES)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        plus(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    }
                }
            }.toTypedArray(),
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        NetWorkStateView()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            IMRefresh(
                modifier = Modifier.fillMaxSize(),
                refreshing = conversationVM.isRefreshing.value,
                onRefresh = {
                    conversationVM.loadHistory()
                }) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    userScrolling.value = true
                                },
                                onDragEnd = {
                                    userScrolling.value = false
                                },
                                onDragCancel = {
                                    userScrolling.value = false
                                },
                                onVerticalDrag = { _, _ -> },
                            )
                        },
                ) {
                    itemsIndexed(items = conversationVM.msgList) { index, msg ->
                        val preMsg = if (index > 0) conversationVM.msgList[index - 1] else null
                        ConversationMessageItem(preMsg = preMsg, imMsg = msg)
                    }
                }
            }
            if (listState.canScrollForward) {
                ConversationFloatTip()
            }
        }

        if (timeoutReply) {
            Text(
                text = stringResource(
                    R.string.chat_session_status_3,
                    conversationVM.timeOutConfig.value?.timeout ?: 5
                ),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = LocalThemeColors.current.conversationSystemTextColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalThemeColors.current.tipBackgroundColor)
                    .padding(horizontal = 15.dp, vertical = 14.dp)
            )
        }
        KeyboardInputBox(
            focusRequester = conversationVM.editFocusRequester,
            value = conversationVM.textFieldValue.value,
            onValueChange = { v ->
                conversationVM.textFieldValue.value = v
            },
            keyboardActions = {

            },
            onSend = {
                conversationVM.sendTxtMessage()

            },
            onPickPhoto = {
                pickPhoto()
            },
            onFocusChanged = {})
    }
}

@Composable
private fun BoxScope.ConversationFloatTip() {
    val conversationVM = LocalSessionViewModel.current
    val receiveMessageEvent = conversationVM.newMessageCount.intValue
    Row(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            .align(BiasAlignment(horizontalBias = 0.9f, verticalBias = 0.7f))
            .clickable(onClick = {
                conversationVM.scrollToBottom(anim = true)
            })
            .padding(vertical = 10.dp, horizontal = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(
            5.dp,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text =
                if (receiveMessageEvent > 0)
                    stringResource(
                        R.string.chat_session_status_11,
                        receiveMessageEvent
                    )
                else
                    stringResource(R.string.chat_session_status_10)
        )
        Image(
            painterResource(R.drawable.ic_to_bottom),
            contentDescription = stringResource(R.string.chat_session_status_10),
            modifier = Modifier.size(15.dp)
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun IMAppBar() {
    val conversationVM = LocalSessionViewModel.current
    CenterAlignedTopAppBar(
        title = {
            Text(stringResource(R.string.chat_session_status_1))
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors().copy(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black,
            navigationIconContentColor = Color.Black
        ),
        navigationIcon = {
            val context = LocalContext.current
            IconButton(onClick = {
                if (PageRoute.routes.size > 1) {
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
        }, actions = {
            IconButton(onClick = {
                conversationVM.serviceSupport()
            }) {
                Image(
                    painter = painterResource(R.drawable.ic_cs),
                    modifier = Modifier
                        .size(24.dp),
                    contentDescription = "人工客服",
                )
            }
        })
}


