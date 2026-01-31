package com.lbe.imsdk.pages.conversation.vm

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.lbe.imsdk.R
import com.lbe.imsdk.components.DialogAction
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.components.IMCupertinoDialogContent
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.manager.ConversationMsgManager
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.manager.SocketEventCallback
import com.lbe.imsdk.manager.SocketManager
import com.lbe.imsdk.pages.conversation.preview.MediaMessagePreViewDialog
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.db.entry.isImageType
import com.lbe.imsdk.repository.db.entry.isVideoType
import com.lbe.imsdk.repository.local.upsert
import com.lbe.imsdk.repository.model.SDKInitConfig
import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
import com.lbe.imsdk.repository.remote.model.SessionListResModel
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.repository.remote.model.enumeration.*
import com.lbe.imsdk.service.http.interceptor.SignInterceptor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 *  会话页面的
 *  @Author mocaris
 *  @Date 2025-08-19
 */
class ConversationVM(
    val initConfig: SDKInitConfig,
    val dialogManager: DialogManager
) : ConversationMessageVM(),
    SocketEventCallback {
    val sessionData = mutableStateOf<CreateSessionResModel.SessionData?>(null)
    internal val uid: String? get() = sessionData.value?.uid
    internal val sessionId: String? get() = sessionData.value?.sessionId
    val lbeToken: String? get() = sessionData.value?.token

    private var msgManager: ConversationMsgManager? = null
    private val socketManagerState = mutableStateOf<SocketManager?>(null)
    val socketManager: SocketManager? get() = socketManagerState.value

    // 历史会话
    private var sessionListModel = mutableStateOf<SessionListResModel.SessionListDataModel?>(null)
    val endSession = mutableStateOf(true)

    ///是否进入人工客服
    val isCustomerService = mutableStateOf(false)

    /// 当前加载的历史会话索引
    private var currentHistoryIndex = 0

    private val lock = Mutex()

    suspend fun initSession() = withIOContext {
        lock.withLock(sessionData) {
            if (!endSession.value) {
                return@withLock
            }
            releaseSocket()
            SignInterceptor.lbeToken = ""
            SignInterceptor.lbeSession = ""
            sessionData.value = tryCatchCoroutine(
                catch = {
                    dialogManager.show(onDismissRequest = {
                        it()
                    }) {
                        val activity = LocalActivity.current
                        IMCupertinoDialogContent(
                            content = {
                                Text(stringResource(R.string.chat_session_status_9))
                            },
                            actions = listOf(
                                DialogAction(
                                    onClick = {
                                        endSession()
                                        activity!!.finish()
                                    },
                                    content = {
                                        Text(
                                            stringResource(R.string.chat_session_status_30),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            )
                        )
                    }
                }
            ) {
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
            with(sessionData.value ?: return@withLock) {
                endSession.value = false
                initSessionState(this)
            }
        }
    }

    private fun initSessionState(data: CreateSessionResModel.SessionData) {
        msgManager = ConversationMsgManager(data.sessionId)
        newMessageCount.intValue = 0
        getHistorySession()
        loadLocalNewest()
        socketManagerState.value =
            LbeIMSDKManager.socketConfigManager?.newSocketManager(data.token, data.sessionId)
                ?.also {
                    it.addSocketEventCallback(this)
                    viewModelScope.launchAsync {
                        it.connectState.collect { t ->
                            if (t.isConnected) {
                                loadLostMessage()
                                initFaq()
                            }
                        }
                    }
                    it.connect()
                }
    }

    private fun endCurrentSession() {
        with(sessionId ?: return) {
            viewModelScope.launchIO {
                tryCatchCoroutine {
                    imApiRepository?.let {
                        it.endSession(this@with)
                        endSession.value = true
                    }
                }
//                initSession()
            }
        }
    }

    private fun releaseSocket() {
        socketManager?.removeSocketEventCallback(this@ConversationVM)
        socketManager?.close()
        socketManagerState.value = null
    }

    override fun onCleared() {
        releaseSocket()
        sessionData.value = null
        super.onCleared()
    }

    override fun onNetChanged(isConnected: Boolean) {
        if (!isConnected) {
            return
        }
    }

    override fun onReceiveMessage(message: IMMessageEntry) {
        if (message.msgType == IMMsgContentType.AGENT_USER_JOIN_SESSION_CONTENT_TYPE) {
            onCustomService()
        }
        if (message.msgType == IMMsgContentType.END_SESSION_CONTENT_TYPE) {
            onEndSession(message.sessionId)
            releaseSocket()
        }
        messageList.add(message)
        newMessageCount.intValue += 1
    }

    override fun onReadMessage(
        sessionId: String,
        seqList: List<Long>
    ) {
        if (seqList.isEmpty() || messageList.isEmpty()) {
            return
        }
        viewModelScope.launchIO {
            for (message in messageList.filter { it.sessionId == sessionId && seqList.contains(it.msgSeq) }) {
                message.updateReadStatus(IMMsgReadStatus.READ)
            }
        }
    }

    override fun onEndSession(sessionId: String) {
        if (sessionId == this.sessionId) {
            endSession.value = true
            isCustomerService.value = false
//            releaseSocket()
        }
    }

    //被踢下线
    override fun onKickOffLine() {
        showKickOfflineDialog()
    }


    private fun onCustomService() {
        isCustomerService.value = true
    }

    fun sendTxtMessage() {
        viewModelScope.launch {
            if (endSession.value) {
                initSession()
            }
            sessionData.value?.let {
                sendTxtMessageInternal(it)
            }
        }
    }

    fun sendMultipleMediaMessage(picUriList: List<Uri>) {
        viewModelScope.launch {
            if (endSession.value) {
                initSession()
            }
            sessionData.value?.let {
                sendMultipleMediaMessageInternal(picUriList, it)
            }
        }
    }

    fun lastVisibleIndex(index: Int) {
        if (index == messageList.size - 1) {
            if (newMessageCount.intValue > 0) {
                newMessageCount.intValue = 0
            }
        }
    }

    private fun getHistorySession() {
        sessionData.value?.let {
            viewModelScope.launchIO {
                lock.withLock(sessionListModel) {
                    if (null != sessionListModel.value) {
                        return@launchIO
                    }
                    sessionListModel.value = tryCatchCoroutine {
                        imApiRepository?.getHistorySessionList(
                            page = 1,
                            size = 1000,
                            sessionType = 2
                        )
                    }?.also {
                        isCustomerService.value =
                            it.sessionList.firstOrNull { t -> t.sessionId == sessionId }?.isCustomService
                                ?: false
                    }
                }
            }
        }
    }

    // 加载断网后未接收到的消息
    private suspend fun loadLostMessage() = withIOContext {
        try {
            val list = msgManager?.loadLocalLostMsgList() ?: return@withIOContext
            if (list.isEmpty()) {
                return@withIOContext
            }
            messageList.addAll(list)
            scrollToBottom()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initFaq() {
        getFaq(FaqType.KNOWLEDGE_BASE, "")
    }

    fun getFaq(
        faqType: FaqType,
        id: String,
    ) {
        viewModelScope.launchIO {
            tryCatchCoroutine {
                if (endSession.value) {
                    initSession()
                }
                imApiRepository?.faq(faqType, id)

            }
        }
    }

    private fun loadLocalNewest(resetMsgList: Boolean = false) {
        msgManager?.let {
            viewModelScope.launchIO {
                tryCatchCoroutine {
                    val list = it.loadNewest(50)
//                    if (resetMsgList) {
//                        messageList.clear()
//                        currentHistoryIndex = 0
//                    }
                    messageList.addAll(list)
                }
                scrollToBottom()
            }
        }
    }


    //转人工
    fun serviceSupport() {
        viewModelScope.launchIO {
            tryCatchCoroutine {
                if (endSession.value) {
                    initSession()
                }
                imApiRepository?.serviceSupport()
            }
        }
    }

    //消息已读
    fun markRead(msg: IMMessageEntry) {
        if (msg.sessionId != sessionId) {
            return
        }
        with(msgManager ?: return) {
            viewModelScope.launchIO {
                try {
                    msgManager?.markRead(msg.msgSeq)
                    msg.updateReadStatus(IMMsgReadStatus.READ)
                    msg.upsert()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun endSession() {
        dialogManager.show(onDismissRequest = {
            it()
        }) { dismiss ->
            IMCupertinoDialogContent(
                title = {
                    Text(text = stringResource(R.string.chat_session_status_31))
                },
                content = {
                    Text(text = stringResource(R.string.chat_session_status_33))
                },
                actions = listOf(
                    DialogAction(onClick = {
                        dismiss()
                    }) {
                        Text(text = stringResource(R.string.chat_session_status_32))
                    },

                    DialogAction(onClick = {
                        dismiss()
                        endCurrentSession()
                    }) {
                        Text(
                            text = stringResource(R.string.chat_session_status_30),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            )
        }
    }

    fun loadHistory() {
        viewModelScope.launchIO {
            try {
                msgManager?.let { manager ->
                    if (isRefreshing.value) return@launchIO
                    withMainContext {
                        isRefreshing.value = true
                    }
                    if (messageList.isEmpty()) {
                        val list = manager.loadNewest(50)
                        messageList.addAll(list)
                        return@launchIO
                    }
                    // 拉取当前 session 最早一条消息，获取该消息之前的 50 条历史消息
                    val first = messageList.firstOrNull { it.msgSeq > 0 } ?: return@launchIO
                    if (first.sessionId != sessionId) {
                        loadMoreSessionHistory()
                        return@launchIO
                    }
                    val endSeq = first.msgSeq - 1
                    if (endSeq <= 1) {
                        loadMoreSessionHistory()
                        return@launchIO
                    }
                    val list = manager.loadMessageList(endSeq - 50, endSeq)
                    if (list.isEmpty()) {
                        loadMoreSessionHistory()
                        return@launchIO
                    }
                    messageList.addAll(0, list)
                }
            } finally {
                delay(300)
                withMainContext {
                    isRefreshing.value = false
                }
            }
        }
    }

    private suspend fun loadMoreSessionHistory() {
        tryCatchCoroutine {
            msgManager?.let { manager ->
                val sessionList = sessionListModel.value?.sessionList
                if (null == sessionList) {
                    getHistorySession()
                    return@let
                }
                with(sessionList) {
                    val sessionList =
                        this.filter { it.sessionId != sessionId }
                    if (currentHistoryIndex >= sessionList.size) {
                        return@with
                    }
                    if (sessionList.isEmpty()) {
                        return@with
                    }
                    val historySession =
                        sessionList.getOrNull(currentHistoryIndex) ?: return@with
                    /// 判断是否该拉取 历史记录
                    val firstMsg = messageList.firstOrNull()
                    val endSeq = if (firstMsg?.sessionId == historySession.sessionId) {
                        firstMsg.msgSeq - 1
                    } else {
                        (historySession.latestMsg?.msgSeq ?: 0) - 1
                    }
                    if (endSeq <= 1) {
                        currentHistoryIndex += 1
                        loadMoreSessionHistory()
                        return@with
                    }
                    val startSeq = (endSeq - 50).coerceAtLeast(1)
                    // 拉取历史 50条 先拉取本地
                    val list =
                        manager.loadMessageList(startSeq, endSeq, historySession.sessionId)
                    if (list.isEmpty()) {
                        currentHistoryIndex += 1
                        return@with
                    }
                    messageList.addAll(0, list)
                }
            }
        }
    }

    @UnstableApi
    fun previewMedia(content: IMMessageEntry, dialogManager: DialogManager) {
        if (!content.isVideoType() && !content.isImageType()) {
            return
        }
        dialogManager.show(
            onDismissRequest = { it() }, properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            val list =
                messageList.filter { (it.isImageType() || it.isVideoType()) && null != it.mediaBodyContent.value }
            val index = list.indexOf(content)
            MediaMessagePreViewDialog(index = index, list)
        }
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
}
