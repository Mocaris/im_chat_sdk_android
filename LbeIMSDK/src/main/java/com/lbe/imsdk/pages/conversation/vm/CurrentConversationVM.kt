package com.lbe.imsdk.pages.conversation.vm

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.lbe.imsdk.R
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.manager.*
import com.lbe.imsdk.pages.conversation.preview.MediaMessagePreViewDialog
import com.lbe.imsdk.provider.AppLifecycleObserver
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.db.entry.IMUploadTask
import com.lbe.imsdk.repository.db.entry.isImageType
import com.lbe.imsdk.repository.db.entry.isVideoType
import com.lbe.imsdk.repository.local.upsert
import com.lbe.imsdk.repository.remote.api.params.SendMessageBody
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.*
import com.lbe.imsdk.service.NetworkMonitor
import com.lbe.imsdk.service.upload.BigFileUploadTask
import com.lbe.imsdk.service.upload.SmallFileUploadTask
import com.lbe.imsdk.service.upload.UploadResult
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.io.File

/**
 *
 * @Author mocaris
 * @Date 2026-01-27
 * @Since
 */
class CurrentConversationVM(
    private val holderVM: ConversationSateHolderVM,
    private val sessionData: CreateSessionResModel.SessionData
) :
    ViewModel(), SocketEventCallback {
    private val networkMonitor = NetworkMonitor(appContext)

    val sessionId: String get() = sessionData.sessionId
    val lbeToken: String get() = sessionData.token

    val msgList get() = holderVM.msgList
    private val msgManager = ConversationMsgManager(sessionId)

    var socketManager =
        LbeIMSDKManager.socketConfigManager?.newSocketManager(lbeToken, sessionId)


    val netState = mutableStateOf(true)

    private var sessionListModel: SessionListResModel.SessionListDataModel? = null

    val endSession = mutableStateOf(false)

    ///是否进入人工客服
    val isCustomerService = mutableStateOf(false)

    /// 当前加载的历史会话索引
    private var currentHistoryIndex = 0

    init {
//        AppLifecycleObserver.addObserver(this)
        initState()
    }

    private fun initState() {
        addCloseable(networkMonitor.also {
            it.startMonitoring()
        })
        viewModelScope.launchAsync {
            networkMonitor.isConnected.collect(::onNetChanged)
        }
        socketManager?.let {
            it.addSocketEventCallback(this)
            viewModelScope.launchAsync {
                it.connectState.collect { t ->
                    if (t.isConnected) {
                        loadLostMessage()
                    }
                }
            }
            it.connect()
        }
        loadLocalNewest()
        initFaq()
        getHistorySession()
    }

    private fun onNetChanged(enable: Boolean) {
        netState.value = enable
//        checkConfig()
        getHistorySession()
    }


    private fun getHistorySession() {
        viewModelScope.launchIO {
            sessionListModel = tryCatchCoroutine {
                holderVM.imApiRepository?.getHistorySessionList(
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

    // 加载断网后未接收到的消息
    private suspend fun loadLostMessage() = withIOContext {
        try {
            val list = msgManager.loadLocalLostMsgList()
            if (list.isEmpty()) {
                return@withIOContext
            }
            msgList.addAll(list)
            holderVM.scrollToBottom()
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
                holderVM.imApiRepository?.faq(faqType, id)

            }
        }
    }

    private fun loadLocalNewest() {
        viewModelScope.launchIO {
            tryCatchCoroutine {
                val list = msgManager.loadNewest(50)
                msgList.addAll(list)
            }
            holderVM.scrollToBottom()
        }
    }


    //转人工
    fun serviceSupport() {
        viewModelScope.launchIO {
            tryCatchCoroutine {
                holderVM.imApiRepository?.serviceSupport()
            }
        }
    }

    //消息已读
    fun markRead(msg: IMMessageEntry) {
        if (msg.sessionId != sessionId) {
            return
        }
        viewModelScope.launchIO {
            try {
                msgManager.markRead(msg.msgSeq)
                msg.updateReadStatus(IMMsgReadStatus.READ)
                msg.upsert()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onReceiveMessage(message: IMMessageEntry) {
        if (message.msgType == IMMsgContentType.AGENT_USER_JOIN_SESSION_CONTENT_TYPE) {
            onCustomService()
        }
        msgList.add(message)
        holderVM.newMessageCount.intValue += 1
//        launchTimeOutJob(message.msgSeq)
    }

    fun onCustomService() {
        isCustomerService.value = true
    }

    override fun onReadMessage(
        sessionId: String,
        seqList: List<Long>
    ) {
        if (seqList.isEmpty() || msgList.isEmpty()) {
            return
        }
        viewModelScope.launchIO {
            for (message in msgList.filter { it.sessionId == sessionId && seqList.contains(it.msgSeq) }) {
                message.updateReadStatus(IMMsgReadStatus.READ)
            }
//            LbeImDataRepository.updateReadStatus()
        }
    }

    //被踢下线
    override fun onKickOffLine() {
        holderVM.onKickOffLine()
    }

    override fun onEndSession(sessionId: String) {
        if (sessionId == this.sessionId) {
            endSession.value = true
            isCustomerService.value = false
        }
        holderVM.onEndSession(sessionId)
    }

    override fun onCleared() {
        socketManager?.close()
        socketManager?.removeSocketEventCallback(this)
        socketManager = null
        super.onCleared()
    }

    fun loadHistory() {
        viewModelScope.launchIO {
            try {
                if (holderVM.isRefreshing.value) return@launchIO
                withMainContext {
                    holderVM.isRefreshing.value = true
                }
                if (msgList.isEmpty()) {
                    val list = msgManager.loadNewest(50)
                    msgList.addAll(list)
                    return@launchIO
                }
                // 拉取当前 session 最早一条消息，获取该消息之前的 50 条历史消息
                val first = msgList.firstOrNull { it.msgSeq > 0 } ?: return@launchIO
                if (first.sessionId != sessionId) {
                    loadMoreSessionHistory()
                    return@launchIO
                }

                if (first.msgSeq <= 1) {
                    loadMoreSessionHistory()
                    return@launchIO
                }
                val endSeq = first.msgSeq - 1
                val list = msgManager.loadMessageList(endSeq - 50, endSeq)
                if (list.isEmpty()) {
                    loadMoreSessionHistory()
                    return@launchIO
                }
                msgList.addAll(0, list)
            } finally {
                delay(300)
                withMainContext {
                    holderVM.isRefreshing.value = false
                }
            }
        }
    }

    private suspend fun loadMoreSessionHistory() {
        tryCatchCoroutine {
            with(sessionListModel ?: return@tryCatchCoroutine) {
                val sessionList =
                    this.sessionList.filter { it.sessionId != sessionId }
                if (currentHistoryIndex >= sessionList.size) {
                    return@with
                }
                if (sessionList.isEmpty()) {
                    return@with
                }
                val historySession =
                    sessionList.getOrNull(currentHistoryIndex) ?: return@with
                /// 判断是否该拉取 历史记录
                val firstMsg = msgList.firstOrNull()
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
                    msgManager.loadMessageList(startSeq, endSeq - 1, historySession.sessionId)
                if (list.isEmpty()) {
                    currentHistoryIndex += 1
                    return@with
                }
                msgList.addAll(0, list)
            }
        }
    }

    fun sendTxtMessage() = synchronized(this) {
        val text = holderVM.textFieldValue.value.text.trim()
        if (text.isEmpty()) {
            return@synchronized
        }
        holderVM.textFieldValue.value = TextFieldValue(text = "")
        viewModelScope.launchIO {
            val chunked = text.chunked(LbeIMSDKManager.TEXT_CONTENT_LENGTH)
            try {
                for (chunk in chunked) {
                    val messageBody = SendMessageBody.createTextMessage(chunk)
                    val localMessage = createLocalMessage(messageBody)
                    saveMessageEntry(localMessage)
                    sendMessageInternal(localMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveMessageEntry(messageEntry: IMMessageEntry) {
        //替换 本地消息
        val insertRowId = messageEntry.upsert()
        if (insertRowId == 0L) {
            throw Exception("insert message error")
        }
        msgList.add(messageEntry)
        holderVM.scrollToBottom()
    }

    fun sendMultipleMediaMessage(picUriList: List<Uri>) {
        viewModelScope.launchIO {
            for (picUri in picUriList) {
                sendMediaMessage(picUri)
            }
        }
    }

    suspend fun sendMediaMessage(picUri: Uri) = withIOContext {
        try {
            val uriFile = picUri.toCompatUriFile()
            val thumbnail = uriFile.thumbnailImage() ?: throw Exception("thumbnail is null")
            val fileMetadata = uriFile.getFileMetaData()
            val mediaMetadata =
                fileMetadata.mediaMetadata ?: throw Exception("mediaMetadata is null")
            val messageBody = SendMessageBody.createMediaMessage(
                MediaMessageContent(
                    width = mediaMetadata.width,
                    height = mediaMetadata.height,
                    thumbnail = SourceUrl(key = "", url = ""),
                    resource = SourceUrl(key = "", url = ""),
                ),
                fileMetadata.isVideo()
            )
            val localMsg = createLocalMessage(messageBody).also {
                it.localTempSource = MediaMessageContent(
                    width = mediaMetadata.width,
                    height = mediaMetadata.height,
                    thumbnail = SourceUrl(key = "", url = thumbnail.path),
                    resource = SourceUrl(key = "", url = fileMetadata.absolutePath),
                )
                it.uploadTask = IMUploadTask(
                    width = mediaMetadata.width,
                    height = mediaMetadata.height,
                    thumbnail = thumbnail.path,
                    filePath = fileMetadata.absolutePath,
                )
            }
            saveMessageEntry(localMsg)
            sendMessageInternal(localMsg)
        } catch (e: Exception) {
            e.printStackTrace()
            appContext.getString(R.string.content_description_send_fail).showToast()
        }
    }

    fun reSendMessage(msgEntry: IMMessageEntry) {
        sendMessageInternal(msgEntry)
    }

    private fun sendMessageInternal(localMsg: IMMessageEntry) {
        viewModelScope.launchIO {
            localMsg.updateSendStatus(IMMsgSendStatus.SENDING)
            try {
                //上传文件
                checkUploadMedia(localMsg)
                val msgSeq = msgManager.sendMessage(
                    SendMessageBody.createByMessageEntry(localMsg)
                )
                if (null == msgSeq) {
                    throw Exception("send message error")
                }
                localMsg.msgSeq = msgSeq
                localMsg.updateSendStatus(IMMsgSendStatus.SUCCESS)
            } catch (e: Exception) {
                e.printStackTrace()
                localMsg.updateSendStatus(IMMsgSendStatus.FAILURE)
            } finally {
                localMsg.upsert()
            }
        }
    }

    private suspend fun checkUploadMedia(localMsg: IMMessageEntry) {
        if (!localMsg.isImageType() && !localMsg.isVideoType()) {
            return
        }
        val localTempSource = localMsg.localTempSource ?: throw Exception("source is null")
        val thumbFile = File(localTempSource.thumbnail.url)
        val sourceFile = File(localTempSource.resource.url)
        //先上传 缩略图
        try {
            localMsg.updateSendStatus(IMMsgSendStatus.SENDING)
            val thumbData = SmallFileUploadTask(
                thumbFile,
                signType = UploadSignType.IMAGE,
                onProgress = {
                }
            ).upload()
            //上传原文件
            var originUrl: UploadResult
            if (sourceFile.needChunk()) {
                val uploadTask = localMsg.uploadTask ?: IMUploadTask(
                    width = localTempSource.width,
                    height = localTempSource.height,
                    thumbnail = thumbFile.path,
                    filePath = sourceFile.path,
                )
                originUrl = BigFileUploadTask(
                    sourceFile = sourceFile,
                    reUploadInfo = localMsg.uploadTask?.let {
                        BigFileUploadTask.ReUploadInfo(
                            uploadId = it.uploadId,
                            nodes = it.nodes,
                            uploadedChunk = it.uploadChunkMD5
                        )
                    },
                    onInitChunkNodes = { fileNode ->
                        uploadTask.uploadId = fileNode.uploadId
                        uploadTask.nodes = fileNode.node
                    },
                    onChunkUpload = {
                        uploadTask.uploadChunkMD5 = it
                    },
                    onProgress = localMsg::updateSendProgress
                ).upload()
            } else {
                originUrl = SmallFileUploadTask(
                    sourceFile,
                    signType = if (localMsg.isVideoType()) UploadSignType.VIDEO else UploadSignType.IMAGE,
                    onProgress = localMsg::updateSendProgress
                ).upload()
            }
            localMsg.updateSendProgress(1f)
            val contentBody = MediaMessageContent(
                width = localTempSource.width,
                height = localTempSource.height,
                thumbnail = thumbData.uploadUrl,
                resource = originUrl.uploadUrl,
            )
            localMsg.msgBody = Json.encodeToString(contentBody)
            localMsg.mediaBodyContent.value = contentBody
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            localMsg.upsert()
        }
    }


    private fun createLocalMessage(
        body: SendMessageBody,
    ): IMMessageEntry {
        return IMMessageEntry.createByMsgBody(
            msgBody = body,
            uid = sessionData.uid,
            sessionId = sessionData.sessionId,
        )
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
                msgList.filter { (it.isImageType() || it.isVideoType()) && null != it.mediaBodyContent.value }
            val index = list.indexOf(content)
            MediaMessagePreViewDialog(index = index, list)
        }
    }

}