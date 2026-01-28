package com.lbe.imsdk.pages.conversation.vm

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
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
import com.lbe.imsdk.repository.local.LbeImDataRepository
import com.lbe.imsdk.repository.local.upsert
import com.lbe.imsdk.repository.remote.api.params.SendMessageBody
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.FaqType
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus
import com.lbe.imsdk.repository.remote.model.enumeration.UploadSignType
import com.lbe.imsdk.service.NetworkMonitor
import com.lbe.imsdk.service.upload.BigFileUploadTask
import com.lbe.imsdk.service.upload.SmallFileUploadTask
import com.lbe.imsdk.service.upload.UploadResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
    ViewModel(), SocketEventCallback, DefaultLifecycleObserver {
    private val networkMonitor = NetworkMonitor(appContext)

    val sessionId: String get() = sessionData.sessionId
    val lbeToken: String get() = sessionData.token

    private val msgList get() = holderVM.msgList
    private val msgManager = ConversationMsgManager(sessionId)
    val timeOutReply = mutableStateOf(false)

    private var timeOutReplyJob: Job? = null

    val netState = mutableStateOf(true)
    val timeOutConfig = mutableStateOf<TimeOutConfigModel.TimeOutConfigData?>(null)

    private var sessionListModel: SessionListResModel.SessionListDataModel? = null


    init {
        AppLifecycleObserver.addObserver(this)
        initState()
    }

    private fun initState() {
        addCloseable(networkMonitor.also {
            it.startMonitoring()
        })
        viewModelScope.launchAsync {
            networkMonitor.isConnected.collect(::onNetChanged)
        }
        LbeIMSDKManager.socketManager?.let {
            it.addSocketEventCallback(this)
            viewModelScope.launchAsync {
                it.connectState.collect { t ->
                    if (t.isConnected) {
                        loadLostMessage()
                    }
                }
            }
            it.initSessionSocket(sessionData)
        }
        loadLocalNewest()
        initFaq()
        getHistorySession()
    }

    private fun onNetChanged(enable: Boolean) {
        netState.value = enable
        checkConfig()
    }

    private fun checkConfig() {
        if (netState.value) {
            getTimeoutConfig()
        }
    }

    private fun getTimeoutConfig() {
        viewModelScope.launchIO {
            if (null != timeOutConfig.value) {
                return@launchIO
            }
            tryCatchCoroutine { holderVM.imApiRepository?.getTimeoutConfig() }?.let {
                timeOutConfig.value = it
            }
        }
    }

    private fun getHistorySession() {
        viewModelScope.launchIO {
            sessionListModel = tryCatchCoroutine {
                holderVM.imApiRepository?.getHistorySessionList(
                    page = 1,
                    size = 1000,
                    sessionType = 2
                )
            }
        }
    }

    // 加载断网后未接收到的消息
    private suspend fun loadLostMessage() = withIOContext {
        try {
            val lastestSeq = msgManager.getRemoteLastestSeq()
            val messageEntry =
                msgList.filter { it.sessionId == sessionId }.maxByOrNull { it.msgSeq }
            if (messageEntry == null || lastestSeq <= messageEntry.msgSeq) {
                return@withIOContext
            }
            msgManager.loadRemoteNewest(messageEntry.msgSeq, lastestSeq)
            val list = msgManager.localMsgList(messageEntry.msgSeq + 1, lastestSeq)
            msgList.addAll(list)
            if (list.isNotEmpty()) {
                holderVM.scrollToBottom()
            }
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
                msgManager.loadRemoteNewest()
                val list = msgManager.loadLocalNewest(50)
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

    // 开启超时未回复 job
    fun launchTimeOutJob(msgSeq: Long) {
        timeOutReplyJob?.cancel()
        val config = timeOutConfig.value ?: return
        if (!config.isOpen) {
            return
        }
        timeOutReplyJob = viewModelScope.launch {
            timeOutReply.value = false
            delay(config.timeout.toDuration(DurationUnit.MINUTES))
            msgList.maxByOrNull { it.msgSeq }?.let {
                if (it.msgSeq <= msgSeq) {
                    timeOutReply.value = true
                }
            }
        }

    }

    override fun onReceiveMessage(message: IMMessageEntry) {
        msgList.add(message)
        holderVM.newMessageCount.intValue += 1
        launchTimeOutJob(message.msgSeq)
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
        holderVM.onEndSession(sessionId)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (LbeIMSDKManager.socketManager?.connectState?.value?.isConnected != true) {
            LbeIMSDKManager.socketManager?.connect()
        }
    }

    override fun onCleared() {
        AppLifecycleObserver.removeObserver(this)
        holderVM.editFocusRequester.freeFocus()
        LbeIMSDKManager.socketManager?.disconnect()
        LbeIMSDKManager.socketManager?.removeSocketEventCallback(this)
        super.onCleared()
    }

    fun loadHistory() {
        viewModelScope.launchIO {
            try {
                if (holderVM.isRefreshing.value) return@launchIO
                withMainContext {
                    holderVM.isRefreshing.value = true
                }
                suspend fun loadMoreSessionHistory() {
                    with(sessionListModel ?: return) {
                        val firstMsgSessionId = msgList.firstOrNull()?.sessionId
                        val sessionIdList = this.sessionList.filter { it.sessionId != sessionId }
                            .map { it.sessionId }
                        val loaded = sessionIdList.any { it == firstMsgSessionId }
                        if (loaded) {
                            return@with
                        }
                        val list = LbeImDataRepository.findSessionMsgList(sessionIdList)
                        if (list.isEmpty()) {
                            return@with
                        }
                        msgList.addAll(0, list)
                    }
                }

                if (msgList.isNotEmpty()) {
                    val firstSessionId = msgList.first().sessionId
                    if (firstSessionId != sessionId) {
                        return@launchIO
                    }
                    // 拉取当前 session 最早一条消息，获取该消息之前的 50 条历史消息
                    val first = msgList.firstOrNull { it.msgSeq > 0 } ?: return@launchIO
                    val list = msgManager.loadHistory(first.msgSeq - 1, 50)
                    if (list.isNotEmpty()) {
                        msgList.addAll(0, list)
                    } else {
                        loadMoreSessionHistory()
                    }
                } else {
                    loadMoreSessionHistory()
                }
            } finally {
                delay(300)
                withMainContext {
                    holderVM.isRefreshing.value = false
                }
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
            val uriFile = picUri.toUriFile() ?: throw Exception("uriFile is null")
            val thumbnail = uriFile.thumbnailImage() ?: throw Exception("thumbnail is null")
            val sourceFile = uriFile.cacheSourceFile() ?: throw Exception("sourceFile is null")
            val messageBody = SendMessageBody.createMediaMessage(
                MediaMessageContent(
                    width = uriFile.width,
                    height = uriFile.height,
                    thumbnail = SourceUrl(key = "", url = ""),
                    resource = SourceUrl(key = "", url = ""),
                ),
                uriFile.isVideo()
            )
            val localMsg = createLocalMessage(messageBody).also {
                it.localTempSource = MediaMessageContent(
                    width = uriFile.width,
                    height = uriFile.height,
                    thumbnail = SourceUrl(key = "", url = thumbnail.path),
                    resource = SourceUrl(key = "", url = sourceFile.path),
                )
                it.uploadTask = IMUploadTask(
                    width = uriFile.width,
                    height = uriFile.height,
                    thumbnail = thumbnail.path,
                    filePath = sourceFile.path,
                )
            }
            saveMessageEntry(localMsg)
            sendMessageInternal(localMsg)
        } catch (e: Exception) {
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