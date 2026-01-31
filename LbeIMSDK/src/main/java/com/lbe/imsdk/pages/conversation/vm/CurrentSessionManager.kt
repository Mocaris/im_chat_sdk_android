//package com.lbe.imsdk.pages.conversation.vm
//
//import android.net.Uri
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.lifecycle.viewModelScope
//import com.lbe.imsdk.R
//import com.lbe.imsdk.extension.appContext
//import com.lbe.imsdk.extension.launchIO
//import com.lbe.imsdk.extension.needChunk
//import com.lbe.imsdk.extension.showToast
//import com.lbe.imsdk.extension.toCompatUriFile
//import com.lbe.imsdk.extension.withIOContext
//import com.lbe.imsdk.manager.ConversationMsgManager
//import com.lbe.imsdk.manager.LbeIMSDKManager
//import com.lbe.imsdk.repository.db.entry.IMMessageEntry
//import com.lbe.imsdk.repository.db.entry.IMUploadTask
//import com.lbe.imsdk.repository.db.entry.isImageType
//import com.lbe.imsdk.repository.db.entry.isVideoType
//import com.lbe.imsdk.repository.local.upsert
//import com.lbe.imsdk.repository.remote.api.params.SendMessageBody
//import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
//import com.lbe.imsdk.repository.remote.model.MediaMessageContent
//import com.lbe.imsdk.repository.remote.model.SourceUrl
//import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus
//import com.lbe.imsdk.repository.remote.model.enumeration.UploadSignType
//import com.lbe.imsdk.service.upload.BigFileUploadTask
//import com.lbe.imsdk.service.upload.SmallFileUploadTask
//import com.lbe.imsdk.service.upload.UploadResult
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.serialization.json.Json
//import java.io.File
//
///**
// *
// * @Author mocaris
// * @Date 2026-01-31
// * @Since
// */
//typealias OnSendMessageCallback = (messageEntry: IMMessageEntry) -> Unit
//
//class CurrentSessionManager(
//    private val sessionData: CreateSessionResModel.SessionData,
//    private val onSendMessageCallback: OnSendMessageCallback
//) {
//    internal val uid: String get() = sessionData.uid
//    internal val sessionId: String get() = sessionData.sessionId
//
//    internal val msgList = mutableStateListOf<IMMessageEntry>()
//    val newMessageCount = mutableIntStateOf(0)
//
//    private val viewModelScope = CoroutineScope(Dispatchers.Unconfined)
//
//
//    internal fun sendTxtMessageInternal(content: String) =
//        synchronized(this) {
//            val text = content.trim()
//            if (text.isEmpty()) {
//                return@synchronized
//            }
//            viewModelScope.launchIO {
//                val chunked = text.chunked(LbeIMSDKManager.TEXT_CONTENT_LENGTH)
//                try {
//                    for (chunk in chunked) {
//                        val messageBody = SendMessageBody.createTextMessage(chunk)
//                        val localMessage = createLocalMessage(
//                            messageBody,
//                            uid = sessionData.uid,
//                            sessionId = sessionData.sessionId
//                        )
//                        saveMessageEntry(localMessage)
//                        sendMessageInternal(localMessage)
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//
//    private suspend fun saveMessageEntry(messageEntry: IMMessageEntry) {
//        //替换 本地消息
//        val insertRowId = messageEntry.upsert()
//        if (insertRowId == 0L) {
//            throw Exception("insert message error")
//        }
//        msgList.add(messageEntry)
//        scrollToBottom()
//    }
//
//    internal fun sendMultipleMediaMessageInternal(
//        picUriList: List<Uri>,
//        sessionData: CreateSessionResModel.SessionData
//    ) {
//        viewModelScope.launchIO {
//            for (picUri in picUriList) {
//                sendMediaMessageInternal(picUri, sessionData)
//            }
//        }
//    }
//
//    internal suspend fun sendMediaMessageInternal(
//        picUri: Uri,
//        sessionData: CreateSessionResModel.SessionData
//    ) =
//        withIOContext {
//            try {
//                val uriFile = picUri.toCompatUriFile()
//                val thumbnail = uriFile.thumbnailImage() ?: throw Exception("thumbnail is null")
//                val fileMetadata = uriFile.getFileMetaData()
//                val mediaMetadata =
//                    fileMetadata.mediaMetadata ?: throw Exception("mediaMetadata is null")
//                val messageBody = SendMessageBody.createMediaMessage(
//                    MediaMessageContent(
//                        width = mediaMetadata.width,
//                        height = mediaMetadata.height,
//                        thumbnail = SourceUrl(key = "", url = ""),
//                        resource = SourceUrl(key = "", url = ""),
//                    ),
//                    fileMetadata.isVideo()
//                )
//                val localMsg =
//                    createLocalMessage(messageBody, sessionData.uid, sessionData.sessionId).also {
//                        it.localTempSource = MediaMessageContent(
//                            width = mediaMetadata.width,
//                            height = mediaMetadata.height,
//                            thumbnail = SourceUrl(key = "", url = thumbnail.path),
//                            resource = SourceUrl(key = "", url = fileMetadata.absolutePath),
//                        )
//                        it.uploadTask = IMUploadTask(
//                            width = mediaMetadata.width,
//                            height = mediaMetadata.height,
//                            thumbnail = thumbnail.path,
//                            filePath = fileMetadata.absolutePath,
//                        )
//                    }
//                saveMessageEntry(localMsg)
//                sendMessageInternal(localMsg)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                appContext.getString(R.string.content_description_send_fail).showToast()
//            }
//        }
//
//    fun reSendMessage(msgEntry: IMMessageEntry) {
//        sendMessageInternal(msgEntry)
//    }
//
//    private fun sendMessageInternal(localMsg: IMMessageEntry) {
//        viewModelScope.launchIO {
//            localMsg.updateSendStatus(IMMsgSendStatus.SENDING)
//            try {
//                //上传文件
//                checkUploadMedia(localMsg)
//                val msgSeq = ConversationMsgManager.sendMessage(
//                    SendMessageBody.createByMessageEntry(localMsg)
//                )
//                if (null == msgSeq) {
//                    throw Exception("send message error")
//                }
//                localMsg.msgSeq = msgSeq
//                localMsg.updateSendStatus(IMMsgSendStatus.SUCCESS)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                localMsg.updateSendStatus(IMMsgSendStatus.FAILURE)
//            } finally {
//                localMsg.upsert()
//            }
//        }
//    }
//
//    private suspend fun checkUploadMedia(localMsg: IMMessageEntry) {
//        if (!localMsg.isImageType() && !localMsg.isVideoType()) {
//            return
//        }
//        val localTempSource = localMsg.localTempSource ?: throw Exception("source is null")
//        val thumbFile = File(localTempSource.thumbnail.url)
//        val sourceFile = File(localTempSource.resource.url)
//        //先上传 缩略图
//        try {
//            localMsg.updateSendStatus(IMMsgSendStatus.SENDING)
//            val thumbData = SmallFileUploadTask(
//                thumbFile,
//                signType = UploadSignType.IMAGE,
//                onProgress = {
//                }
//            ).upload()
//            //上传原文件
//            var originUrl: UploadResult
//            if (sourceFile.needChunk()) {
//                val uploadTask = localMsg.uploadTask ?: IMUploadTask(
//                    width = localTempSource.width,
//                    height = localTempSource.height,
//                    thumbnail = thumbFile.path,
//                    filePath = sourceFile.path,
//                )
//                originUrl = BigFileUploadTask(
//                    sourceFile = sourceFile,
//                    reUploadInfo = localMsg.uploadTask?.let {
//                        BigFileUploadTask.ReUploadInfo(
//                            uploadId = it.uploadId,
//                            nodes = it.nodes,
//                            uploadedChunk = it.uploadChunkMD5
//                        )
//                    },
//                    onInitChunkNodes = { fileNode ->
//                        uploadTask.uploadId = fileNode.uploadId
//                        uploadTask.nodes = fileNode.node
//                    },
//                    onChunkUpload = {
//                        uploadTask.uploadChunkMD5 = it
//                    },
//                    onProgress = localMsg::updateSendProgress
//                ).upload()
//            } else {
//                originUrl = SmallFileUploadTask(
//                    sourceFile,
//                    signType = if (localMsg.isVideoType()) UploadSignType.VIDEO else UploadSignType.IMAGE,
//                    onProgress = localMsg::updateSendProgress
//                ).upload()
//            }
//            localMsg.updateSendProgress(1f)
//            val contentBody = MediaMessageContent(
//                width = localTempSource.width,
//                height = localTempSource.height,
//                thumbnail = thumbData.uploadUrl,
//                resource = originUrl.uploadUrl,
//            )
//            localMsg.msgBody = Json.encodeToString(contentBody)
//            localMsg.mediaBodyContent.value = contentBody
//        } catch (e: Exception) {
//            e.printStackTrace()
//            throw e
//        } finally {
//            localMsg.upsert()
//        }
//    }
//
//
//    private fun createLocalMessage(
//        body: SendMessageBody,
//        uid: String,
//        sessionId: String
//    ): IMMessageEntry {
//        return IMMessageEntry.createByMsgBody(
//            msgBody = body,
//            uid = uid,
//            sessionId = sessionId,
//        )
//    }
//
//}