package com.lbe.imsdk.pages.conversation.vm

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbe.imsdk.R
import com.lbe.imsdk.components.DialogAction
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.components.IMCupertinoDialogContent
import com.lbe.imsdk.extension.launchIO
import com.lbe.imsdk.extension.tryCatchCoroutine
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.model.SDKInitConfig
import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.service.http.interceptor.SignInterceptor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 会话状态持有者, 状态管理
 * @Author mocaris
 * @Date 2026-01-27
 * @Since
 */
class ConversationSateHolderVM(
    val initConfig: SDKInitConfig,
    val dialogManager: DialogManager
) : ViewModel() {
    private val _msgList = mutableStateListOf<IMMessageEntry>()
    val currentSession = mutableStateOf<CreateSessionResModel.SessionData?>(null)

    val imApiRepository get() = LbeIMSDKManager.imApiRepository

    val newMessageCount = mutableIntStateOf(0)

    val msgList get() = _msgList

    internal val uid: String? get() = currentSession.value?.uid
    internal val sessionId: String? get() = currentSession.value?.sessionId

    val scrollToBottomEvent = MutableSharedFlow<Boolean>()

    /// ui state
    val textFieldValue = mutableStateOf(TextFieldValue())
    val editFocusRequester = FocusRequester()
    val isRefreshing = mutableStateOf(false)


    private val lock = Mutex()

    init {
        initSate()
    }

    private fun initSate() {
        initSession()
    }


    fun initSession() {
        viewModelScope.launchIO {
            lock.withLock(currentSession) {
                SignInterceptor.lbeToken = ""
                SignInterceptor.lbeSession = ""
                currentSession.value = tryCatchCoroutine {
                    imApiRepository?.createSession(
                        device = initConfig.device,
                        email = initConfig.email,
                        extraInfo = initConfig.extraInfo,
                        groupID = initConfig.groupID,
                        headIcon = if (initConfig.parseHeaderIcon is SourceUrl) "" else initConfig.headerIcon,
                        language = initConfig.language.locale,
                        nickId = initConfig.nickId,
                        nickName = initConfig.nickName,
                        phone = initConfig.phone,
                        source = initConfig.source,
                        uid = ""
                    )
                }?.also {
                    SignInterceptor.lbeToken = it.token
                    SignInterceptor.lbeSession = it.sessionId
                }
            }
        }
    }

    fun endSession() {
        sessionId?.let {
            fun end() {
                viewModelScope.launchIO {
                    tryCatchCoroutine {
                        imApiRepository?.endSession(it)
                        initSession()
                    }
                }
            }
            dialogManager.show(onDismissRequest = {
                it()
            }) { dismiss ->
                IMCupertinoDialogContent(
                    title = {
                        Text(text = stringResource(R.string.chat_session_txt_1))
                    },
                    content = {
                        Text(text = stringResource(R.string.chat_session_txt_2))
                    },
                    actions = listOf(
                        DialogAction(onClick = {
                            dismiss()
                        }) {
                            Text(text = stringResource(R.string.customerServiceManage_customerService_23))
                        },

                        DialogAction(onClick = {
                            end()
                            dismiss()
                        }) {
                            Text(
                                text = stringResource(R.string.chat_session_txt_3),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                )
            }
        }
    }

    fun lastVisibleIndex(index: Int) {
        if (index == msgList.size - 1) {
            if (newMessageCount.intValue > 0) {
                newMessageCount.intValue = 0
            }
        }
    }

    fun scrollToBottom(anim: Boolean = true) {
        viewModelScope.launch {
            delay(200)
            scrollToBottomEvent.emit(anim)
        }
    }

    fun onEndSession(sessionId: String) {
        if (this.sessionId == sessionId) {
            initSession()
        }
    }

    //被踢下线
    fun onKickOffLine() {
        showKickOfflineDialog()
    }

    fun showKickOfflineDialog() {
        dialogManager.show(onDismissRequest = { }) {
            val activity = LocalActivity.current
            IMCupertinoDialogContent(
                content = {
                    Text(
                        text = stringResource(R.string.chat_session_status_8),
                    )
                },
                actions = listOf(
                    DialogAction(onClick = {
                        activity?.finish()
                    }) {
                        Text(
                            text = stringResource(android.R.string.ok),
                        )
                    }
                )
            )
        }
    }

    override fun onCleared() {
        editFocusRequester.freeFocus()
        currentSession.value = null
        super.onCleared()
    }
}