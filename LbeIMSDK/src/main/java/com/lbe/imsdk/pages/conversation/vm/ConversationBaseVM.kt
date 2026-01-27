//package com.lbe.imsdk.pages.conversation.vm
//
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.lbe.imsdk.extension.appContext
//import com.lbe.imsdk.extension.catchException
//import com.lbe.imsdk.extension.launchAsync
//import com.lbe.imsdk.extension.launchIO
//import com.lbe.imsdk.extension.withIOContext
//import com.lbe.imsdk.extension.withMainContext
//import com.lbe.imsdk.manager.ConversationManager
//import com.lbe.imsdk.manager.LbeIMSDKManager
//import com.lbe.imsdk.manager.SocketEventCallback
//import com.lbe.imsdk.repository.db.entry.IMMessageEntry
//import com.lbe.imsdk.repository.local.upsert
//import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
//import com.lbe.imsdk.service.NetworkMonitor
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlin.time.DurationUnit
//import kotlin.time.toDuration
//
///**
// *
// * @Date 2025-09-04
// */
//@Deprecated("")
//abstract class ConversationBaseVM() : ViewModel(),
//    SocketEventCallback {
//
//    val msgList = mutableStateListOf<IMMessageEntry>()
//
//    val netState = mutableStateOf(true)
//
//    val newMessageCount = mutableIntStateOf(0)
//
//    val timeOutReply = mutableStateOf(false)
//    val endSession = mutableStateOf(false)
//
//    var timeOutReplyJob: Job? = null
//
//    private val networkMonitor = NetworkMonitor(appContext)
//
//    init {
//        addCloseable(networkMonitor.also {
//            it.startMonitoring()
//        })
//        LbeIMSDKManager.socketManager?.let {
//            it.addSocketEventCallback(this)
//            viewModelScope.launchAsync {
//                it.connectState.collect { t ->
//                    if (!t.isConnected) {
//                        loadLostMessage()
//                    }
//                }
//            }
//        }
//
//        viewModelScope.launchAsync {
//            networkMonitor.isConnected.collect {
//                netState.value = it
//            }
//        }
//    }
//
//    suspend fun onScrollToBottom() {
//        newMessageCount.intValue = 0
//    }
//
//    // 开启超时未回复 job
//    fun launchTimeOutJob(msgSeq: Long) {
//        val config = ConversationManager.timeOutConfig.value ?: return
//        if (!config.isOpen) {
//            return
//        }
//        timeOutReplyJob?.cancel()
//        timeOutReplyJob = viewModelScope.launch {
//            timeOutReply.value = false
//            delay(config.timeout.toDuration(DurationUnit.MINUTES))
//            msgList.maxByOrNull { it.msgSeq }?.let {
//                if (it.msgSeq <= msgSeq) {
//                    timeOutReply.value = true
//                }
//            }
//        }
//
//    }
//
//    fun lastVisibleIndex(index: Int) {
//        if (index == msgList.size - 1) {
//            if (newMessageCount.intValue > 0) {
//                newMessageCount.intValue = 0
//            }
//        }
//    }
//
//    private suspend fun loadLostMessage() = withIOContext {
//        try {
//            val lastestSeq = ConversationManager.messageManager?.getRemoteLastestSeq()
//            val messageEntry = msgList.maxByOrNull { it.msgSeq }
//            if (messageEntry == null || lastestSeq == messageEntry.msgSeq) {
//                return@withIOContext
//            }
//            manager.loadRemoteNewest(messageEntry.msgSeq, lastestSeq)
//            val list = manager.localMsgList(messageEntry.msgSeq, lastestSeq)
//            msgList.addAll(list)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//
//
//    //转人工
//    fun serviceSupport() {
//        viewModelScope.launchIO {
//            try {
//                ConversationManager.serviceSupport()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                e.catchException()
//            }
//        }
//    }
//
//    //消息已读
//    fun markRead(msg: IMMessageEntry) {
//        viewModelScope.launchIO {
//            try {
//                manager.markRead(msg.msgSeq)
//                msg.updateReadStatus(IMMsgReadStatus.READ)
//                msg.upsert()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    //收到消息
//    override fun onReceiveMessage(message: IMMessageEntry) {
//        msgList.add(message)
//        newMessageCount.intValue += 1
//        launchTimeOutJob(message.msgSeq)
//    }
//
//    //已读消息回调
//    override fun onReadMessage(
//        sessionId: String,
//        seqList: List<Long>
//    ) {
//        if (sessionId != this.sessionId) {
//            return
//        }
//        if (seqList.isEmpty() || msgList.isEmpty()) {
//            return
//        }
//        for (message in msgList) {
//            if (message.msgSeq in seqList) {
//                message.updateReadStatus(IMMsgReadStatus.READ)
//            }
//        }
//    }
//
//
//    override fun onEndSession(sessionId: String) {
//        endSession.value = true
//        viewModelScope.launch {
//            LbeIMSDKManager.initSession()
//        }
//    }
//
//
//    override fun onCleared() {
//        timeOutReplyJob?.cancel()
//        LbeIMSDKManager.socketManager?.removeSocketEventCallback(this)
//        super.onCleared()
//    }
//
//}