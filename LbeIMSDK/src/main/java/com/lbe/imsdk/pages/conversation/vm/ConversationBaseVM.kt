package com.lbe.imsdk.pages.conversation.vm

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbe.imsdk.extension.appContext
import com.lbe.imsdk.extension.catchException
import com.lbe.imsdk.extension.launchAsync
import com.lbe.imsdk.extension.launchIO
import com.lbe.imsdk.extension.withIOContext
import com.lbe.imsdk.manager.ConversationManager
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.manager.SocketEventCallback
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.local.upsert
import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
import com.lbe.imsdk.repository.remote.model.TimeOutConfigModel
import com.lbe.imsdk.repository.remote.model.enumeration.FaqType
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
import com.lbe.imsdk.service.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 *
 * @Date 2025-09-04
 */
abstract class ConversationBaseVM(val sessionData: CreateSessionResModel.SessionData) : ViewModel(),
    SocketEventCallback {

    val sessionId: String = sessionData.sessionId

    val manager = ConversationManager(sessionId)

    val msgList = mutableStateListOf<IMMessageEntry>()

    val timeOutConfig = mutableStateOf<TimeOutConfigModel.TimeOutConfigData?>(null)

    val netState = mutableStateOf(true)

    val newMessageCount = mutableIntStateOf(0)

    val timeOutReply = mutableStateOf(false)

    var timeOutReplyJob: Job? = null

    private val networkMonitor = NetworkMonitor(appContext)

    init {
        addCloseable(networkMonitor.also {
            it.startMonitoring()
        })
        getTimeOutConfig()
        LbeIMSDKManager.socketManager?.let {
            it.addSocketEventCallback(this)
            viewModelScope.launchAsync {
                it.connectState.collect { t ->
                    if (!t.isConnected) {
                        loadLostMessage()
                    }
                }
            }
        }
        getFaq(FaqType.KNOWLEDGE_BASE, "")
        viewModelScope.launchAsync {
            networkMonitor.isConnected.collect {
                netState.value = it
                if (it) {
                    getTimeOutConfig();
                }
            }
        }
    }



    suspend fun onScrollToBottom() {
        newMessageCount.intValue = 0
    }

    // 开启超时未回复 job
    fun launchTimeOutJob(msgSeq: Long) {
        val config = timeOutConfig.value ?: return
        if (!config.isOpen) {
            return
        }
        timeOutReplyJob?.cancel()
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

     fun lastVisibleIndex(index: Int) {
        if (index == msgList.size - 1) {
            if (newMessageCount.intValue > 0) {
                newMessageCount.intValue = 0
            }
        }
    }

    private suspend fun loadLostMessage() = withIOContext {
        try {
            val lastestSeq = manager.getRemoteLastestSeq()
            val messageEntry = msgList.maxByOrNull { it.msgSeq }
            if (messageEntry == null || lastestSeq == messageEntry.msgSeq) {
                return@withIOContext
            }
            manager.loadRemoteNewest(messageEntry.msgSeq, lastestSeq)
            val list = manager.localMsgList(messageEntry.msgSeq, lastestSeq)
            msgList.addAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getFaq(
        faqType: FaqType,
        id: String,
    ) {
        viewModelScope.launchIO {
            try {
                manager.faq(faqType, id)
            } catch (e: Exception) {
                e.printStackTrace()
                e.catchException()
            }
        }
    }

    fun getTimeOutConfig() {
        viewModelScope.launchIO {
            try {
                if (timeOutConfig.value == null) {
                    timeOutConfig.value = manager.getTimeoutConfig()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //转人工
    fun serviceSupport() {
        viewModelScope.launchIO {
            try {
                manager.serviceSupport()
            } catch (e: Exception) {
                e.printStackTrace()
                e.catchException()
            }
        }
    }

    //消息已读
    fun markRead(msg: IMMessageEntry) {
        viewModelScope.launchIO {
            try {
                manager.markRead(msg.msgSeq)
                msg.updateReadStatus(IMMsgReadStatus.READ)
                msg.upsert()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //收到消息
    override fun onReceiveMessage(message: IMMessageEntry) {
        msgList.add(message)
        newMessageCount.intValue += 1
        launchTimeOutJob(message.msgSeq)
    }

    //已读消息回调
    override fun onReadMessage(
        sessionId: String,
        seqList: List<Long>
    ) {
        if (sessionId != this.sessionId) {
            return
        }
        if (seqList.isEmpty() || msgList.isEmpty()) {
            return
        }
        for (message in msgList) {
            if (message.msgSeq in seqList) {
                message.updateReadStatus(IMMsgReadStatus.READ)
            }
        }
    }


    override fun onEndSession(sessionId: String) {

    }
}