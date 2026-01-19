package com.lbe.imsdk.pages.conversation.vm

import android.net.*
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.window.*
import androidx.lifecycle.*
import androidx.media3.common.util.*
import com.lbe.imsdk.R
import com.lbe.imsdk.components.DialogAction
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.components.IMCupertinoDialogContent
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.manager.*
import com.lbe.imsdk.pages.conversation.preview.MediaMessagePreViewDialog
import com.lbe.imsdk.repository.db.entry.*
import com.lbe.imsdk.repository.local.upsert
import com.lbe.imsdk.repository.remote.api.params.SendMessageBody
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus
import com.lbe.imsdk.repository.remote.model.enumeration.UploadSignType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import java.io.File

/**
 *
 *
 * @Date 2025-08-19
 */
class ConversationVM(
    sessionData: CreateSessionResModel.SessionData,
    val dialogManager: DialogManager
) :
    ConversationBaseVM(sessionData) {

    /*    val frameClock = object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                return suspendCancellableCoroutine<R> { continuation ->
                    // 等待下一帧时间戳
                    Choreographer.getInstance().postFrameCallback {
                        try {
                            val result = onFrame(System.nanoTime())
                            continuation.resume(result) { cause, _, _ -> continuation.cancel() }
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
            }
        }*/
//    val msgListSource = Pager(PagingConfig(pageSize = 20)) {
//        MessagePagingSource(sessionId)
//    }.flow.cachedIn(viewModelScope)

    val isRefreshing = mutableStateOf(false)

    val editFocusRequester = FocusRequester()

    val textFieldValue = mutableStateOf(TextFieldValue())

    val scrollToBottomEvent = MutableSharedFlow<Boolean>()

    init {
        initConversation()
    }

    fun scrollToBottom(anim: Boolean = true) {
        viewModelScope.launch {
            scrollToBottomEvent.emit(anim)
        }
    }

    fun initConversation() {
        viewModelScope.launchIO {
            try {
                manager.loadRemoteNewest()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val list = manager.loadLocalNewest(50)
                msgList.addAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(100)
            scrollToBottom()
        }
    }

    fun loadHistory() {
        viewModelScope.launchIO {
            try {
                if (isRefreshing.value) return@launchIO
                withMainContext {
                    isRefreshing.value = true
                }
                if (msgList.isNotEmpty()) {
                    val first = msgList.firstOrNull { it.msgSeq > 0 } ?: return@launchIO
                    val list = manager.loadHistory(first.msgSeq, 50)
                    msgList.addAll(0, list)
                } else {
                    initConversation()
                }
            } finally {
                withMainContext {
                    isRefreshing.value = false
                }
            }
        }
    }

    fun sendTxtMessage() = synchronized(this) {
        val text = textFieldValue.value.text
        if (text.isEmpty()) {
            return@synchronized
        }
        textFieldValue.value = textFieldValue.value.copy(text = "")
        viewModelScope.launchIO {
            try {
                val messageBody = SendMessageBody.createTextMessage(text)
                val localMessage = createLocalMessage(messageBody)
                saveMessageEntry(localMessage)
                sendMessageInternal(localMessage)
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
        delay(100)
        scrollToBottom()
    }

    fun sendMediaMessage(picUri: Uri) {
        viewModelScope.launchIO {
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
                        filePath = uriFile.path,
                    )
                }
                saveMessageEntry(localMsg)
                sendMessageInternal(localMsg)
            } catch (e: Exception) {
                appContext.getString(R.string.content_description_send_fail).showToast()
            }
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
                val msgSeq = manager.sendMessage(
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
        val ossApiRepository =
            LbeIMSDKManager.ossApiRepository ?: throw Exception("api repository is null")
        val localTempSource = localMsg.localTempSource ?: throw Exception("source is null")
        val thumbFile = File(localTempSource.thumbnail.url)
        val sourceFile = File(localTempSource.resource.url)
        //先上传 缩略图
        try {
            localMsg.updateSendStatus(IMMsgSendStatus.SENDING)
            val thumbData = UploadSingleFileTask(
                thumbFile,
                signType = UploadSignType.IMAGE,
                apiRepository = ossApiRepository,
                onProgress = {}
            ).upload()
            //上传原文件
            var originKey: String = ""
            var originUrl: String
            if (sourceFile.needChunk()) {
                val uploadTask = localMsg.uploadTask ?: IMUploadTask(
                    width = localTempSource.width,
                    height = localTempSource.height,
                    thumbnail = thumbFile.path,
                    filePath = sourceFile.path,
                )
                originUrl = UploadBigFileTask(
                    sourceFile = sourceFile,
                    apiRepository = ossApiRepository,
                    reUploadInfo = localMsg.uploadTask?.let {
                        UploadBigFileTask.ReUploadInfo(
                            uploadId = it.uploadId,
                            nodes = it.nodes,
                            uploadedChunk = it.uploadChunkMD5
                        )
                    },
                    onInitChunkNodes = {
                        val fileNode = ossApiRepository.initMultiPartUpload(
                            size = sourceFile.length(), name = "${sourceFile.name}"
                        )!!
                        uploadTask.uploadId = fileNode.uploadId
                        uploadTask.nodes = fileNode.node
                        return@UploadBigFileTask fileNode
                    },
                    onUploadedChunk = {
                        uploadTask.uploadChunkMD5 = it
                    }
                ) {
                    localMsg.updateSendProgress(it)
                }.upload()
            } else {
                val originData = UploadSingleFileTask(
                    sourceFile,
                    signType = if (localMsg.isVideoType()) UploadSignType.VIDEO else UploadSignType.IMAGE,
                    apiRepository = ossApiRepository
                ) {
                    localMsg.updateSendProgress(it)
                }.upload()
                originKey = originData.first
                originUrl = originData.second
            }
            localMsg.updateSendProgress(1f)
            val contentBody = MediaMessageContent(
                width = localTempSource.width,
                height = localTempSource.height,
                thumbnail = SourceUrl(
                    key = thumbData.first,
                    url = thumbData.second
                ),
                resource = SourceUrl(
                    key = originKey,
                    url = originUrl
                ),
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
            sessionId = sessionId,
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

    private fun saveMessageSource(content: IMMessageEntry, onProgress: (Int) -> Unit) {
        viewModelScope.launchIO {
            try {
                content.mediaBodyContent.value?.let {
                    val url = it.resource.url
                    val fileName = url.getFileName().ifEmpty {
                        if (content.isImageType()) {
                            "${url.md5Str}.jpg"
                        } else {
                            "${url.md5Str}.mp4"
                        }
                    }
                    DownloadManager.downloadSaveToGallery(it.resource.url, fileName) { p ->
                        onProgress(p)
                    }
                }
                appContext.getString(R.string.save_success).showToast()
            } catch (e: Exception) {
                appContext.getString(R.string.save_fail).showToast()
            }
        }
    }

    //被踢下线
    override fun onKickOffLine() {
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
        super.onCleared()
    }


}