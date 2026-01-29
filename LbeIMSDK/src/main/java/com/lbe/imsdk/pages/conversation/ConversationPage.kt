package com.lbe.imsdk.pages.conversation

import android.*
import android.app.*
import android.os.*
import android.widget.Space
import androidx.activity.compose.*
import androidx.activity.result.*
import androidx.activity.result.contract.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.*
import com.lbe.imsdk.R
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.pages.conversation.vm.ConversationSateHolderVM
import com.lbe.imsdk.pages.conversation.vm.CurrentConversationVM
import com.lbe.imsdk.pages.conversation.widgets.IMAppBar
import com.lbe.imsdk.pages.conversation.widgets.KeyboardInputBox
import com.lbe.imsdk.pages.conversation.widgets.NetWorkStateView
import com.lbe.imsdk.pages.conversation.widgets.StartCustomerServiceButton
import com.lbe.imsdk.pages.conversation.widgets.message.ConversationMessageItem
import com.lbe.imsdk.provider.*
import com.lbe.imsdk.repository.model.SDKInitConfig
import com.lbe.imsdk.widgets.IMRefresh
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 会话页面
 *
 * @Date 2025-07-16
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationPage(
    dialogManager: DialogManager = LocalDialogManager.current,
    sdkInitConfig: SDKInitConfig = LocalSDKInitConfig.current,
//    conversationVM: ConversationVM = viewModel(
//        initializer = {
//            ConversationVM(dialogManager)
//        }),
    conversationStateVM: ConversationSateHolderVM = viewModel(
        initializer = {
            ConversationSateHolderVM(sdkInitConfig, dialogManager)
        })
) {
    CompositionLocalProvider(
        LocalConversationStateViewModel provides conversationStateVM,
    ) {
        val currentSession = conversationStateVM.currentSession
        if (null != currentSession) {
            val currentConversationVM = viewModel(key = currentSession.sessionId) {
                CurrentConversationVM(conversationStateVM, currentSession)
            }
            CompositionLocalProvider(
                LocalSession provides currentSession,
                LocalCurrentConversationViewModel provides currentConversationVM,
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(), topBar = {
                        IMAppBar()
                    }) {
                    ConversationPageBody(currentConversationVM, it)
                }
            }
        } else {
            Scaffold {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ConversationPageBody(conversationVM: CurrentConversationVM, padding: PaddingValues) {
    val holderVM = LocalConversationStateViewModel.current
    val requestPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9),
        onResult = conversationVM::sendMultipleMediaMessage,
    )
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            if (it.any { t -> !t.value }) {
                return@rememberLauncherForActivityResult
            }
            requestPhotoLauncher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo,
                )
            )
        },
    )

    val listState = rememberLazyListState()
    val userScrolling = remember { mutableStateOf(false) }
    val keyboardState = rememberKeyboardState()

    val totalItemsCount = remember { mutableIntStateOf(0) }

//    val visibleItemIndex = remember { mutableStateOf(Pair(0, 0)) }
//    val firstVisibleItemIndex = visibleItemIndex.value.first
    val lastVisibleItemIndex = remember { mutableIntStateOf(0) }
    suspend fun scrollToBottom() {
        if (!listState.canScrollForward
            || listState.isScrollInProgress
        ) {
            return
        }
        val firstVisibleItemIndex = listState.firstVisibleItemIndex
        val endOffset = listState.layoutInfo.viewportEndOffset
        if (firstVisibleItemIndex == 0) {
            listState.scrollToItem(totalItemsCount.intValue - 1, endOffset)
        } else {
            listState.animateScrollToItem(totalItemsCount.intValue - 1, endOffset)
        }
//            holderVM.onScrollToBottom()
    }
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }.distinctUntilChanged().collect {
            lastVisibleItemIndex.intValue = it
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.totalItemsCount
        }.distinctUntilChanged().collect {
            totalItemsCount.intValue = it
        }
    }

    LaunchedEffect(listState) {
        holderVM.scrollToBottomEvent.collect {
            scrollToBottom()
        }
    }

    //键盘弹起时
    LaunchedEffect(keyboardState) {
        if (keyboardState.isOpened()) {
            holderVM.scrollToBottom()
        }
    }
    // 有新消息时
    LaunchedEffect(holderVM.newMessageCount) {
        snapshotFlow { holderVM.newMessageCount.intValue }.distinctUntilChanged().collect { size ->
            if (size > 0 && !userScrolling.value) {
                holderVM.scrollToBottom()
            }
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect {
            it?.let {
                holderVM.lastVisibleIndex(it)
            }
        }
    }

    fun pickPhoto() {
        requestPermissionLauncher.launch(
            mutableStateListOf<String>().apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                    add(Manifest.permission.READ_MEDIA_AUDIO)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
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
                refreshing = holderVM.isRefreshing.value,
                onRefresh = conversationVM::loadHistory
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    userScrolling.value = true
                                },
                                onDragEnd = {
                                    userScrolling.value = false
                                },
                                onDragCancel = {
                                    userScrolling.value = false
                                },
                                onDrag = { _, _ -> userScrolling.value = true },
                            )
                        },
                ) {
                    itemsIndexed(
                        items = holderVM.msgList,
                        key = { _, item -> "${item.sessionId}-${item.clientMsgID}" },
                        contentType = { _, item -> item.msgType }
                    ) { index, msg ->
                        val preMsg = if (index > 0) holderVM.msgList[index - 1] else null
                        ConversationMessageItem(preMsg = preMsg, imMsg = msg)
                    }
                }
            }

            if (lastVisibleItemIndex.intValue < totalItemsCount.intValue - 1 && listState.canScrollForward) {
                ConversationFloatTip()
            }
        }


        if (!conversationVM.isCustomerService.value) {
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(horizontal = 15.dp)
            ) {
                StartCustomerServiceButton {
                    conversationVM.serviceSupport()
                }
            }
        }

//        TimeoutTipFloat()
        KeyboardInputBox(
            maxLength = LbeIMSDKManager.TEXT_CONTENT_LENGTH,
            focusRequester = holderVM.editFocusRequester,
            value = holderVM.textFieldValue.value,
            onValueChange = { v ->
                holderVM.textFieldValue.value = v
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
    val holderVM = LocalConversationStateViewModel.current
    val receiveMessageEvent = holderVM.newMessageCount.intValue
    Row(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(LocalThemeColors.current.conversationFlotTipColor.copy(alpha = 0.8f))
            .align(BiasAlignment(horizontalBias = 0.9f, verticalBias = 0.7f))
            .clickable(onClick = holderVM::scrollToBottom)
            .padding(vertical = 10.dp, horizontal = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(
            5.dp, Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (receiveMessageEvent > 0) stringResource(
                R.string.chat_session_status_11, receiveMessageEvent
            )
            else stringResource(R.string.chat_session_status_10)
        )
        Image(
            painterResource(R.drawable.ic_to_bottom),
            contentDescription = stringResource(R.string.chat_session_status_10),
            modifier = Modifier.size(15.dp)
        )
    }
}



