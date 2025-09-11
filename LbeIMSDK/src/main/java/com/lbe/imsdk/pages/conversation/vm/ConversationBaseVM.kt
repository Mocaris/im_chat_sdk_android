package com.lbe.imsdk.pages.conversation.vm

import androidx.activity.compose.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import com.lbe.imsdk.R
import com.lbe.imsdk.components.DialogAction
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.components.IMCupertinoDialogContent
import com.lbe.imsdk.extension.appContext
import com.lbe.imsdk.extension.catchException
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

/**
 *
 * @Date 2025-09-04
 */
open class ConversationBaseVM(val sessionData: CreateSessionResModel.SessionData) : ViewModel(),
    SocketEventCallback {

    val sessionId = sessionData.sessionId

    val manager = ConversationManager(sessionId)

    val msgList = mutableStateListOf<IMMessageEntry>()

    val msgCount: Int get() = msgList.size

    val timeOutConfig = mutableStateOf<TimeOutConfigModel.TimeOutConfigData?>(null)

    val toBottomEventAnim = mutableIntStateOf(0)
    val toBottomEventNoAnim = mutableIntStateOf(0)

    val netState = mutableStateOf(true)

    val newMessageCount = mutableIntStateOf(0)

    val timeOutReply = mutableStateOf(false)

    val kickOffLine = mutableStateOf(false)

    var timeOutReplyJob: Job? = null

    private val networkMonitor = NetworkMonitor(appContext)

    init {
        addCloseable(networkMonitor.also {
            it.startMonitoring()
        })
        getTimeOutConfig()
        LbeIMSDKManager.socketManager?.let {
            it.addSocketEventCallback(this)
            viewModelScope.launch {
                it.connectState.collect {
                    if (!it.isConnected) {
                        loadLostMessage()
                    }
                }
            }
        }
        getFaq(FaqType.KNOWLEDGE_BASE, "")
        viewModelScope.launch {
            networkMonitor.isConnected.collect {
                netState.value = it
            }
        }
    }

    fun launchTimeOutJob(msgSeq: Long) {
        val config = timeOutConfig.value
        if (null == config) {
            return
        }
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


    fun scrollToBottom(anim: Boolean = true) {
        if (anim) {
            toBottomEventAnim.intValue += 1
        } else {
            toBottomEventNoAnim.intValue += 1
        }
        newMessageCount.intValue = 0
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
                timeOutConfig.value = manager.getTimeoutConfig()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun serviceSupport(dialogManager: DialogManager) {
        viewModelScope.launchIO {
            try {
                manager.serviceSupport()
            } catch (e: Exception) {
                e.printStackTrace()
                e.catchException()
            }
        }
    }

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

    override fun onReceiveMessage(message: IMMessageEntry) {
        msgList.add(message)
        newMessageCount.intValue += 1
        launchTimeOutJob(message.msgSeq)
    }

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

    override fun onKickOffLine() {
        kickOffLine.value = true
    }

    fun showKickOfflineDialog(dialogManager: DialogManager) {
        dialogManager.show(onDismissRequest = { }) {
            val activity = LocalActivity.current
            IMCupertinoDialogContent(
                content = {
                    Text(
                        text = stringResource(R.string.chat_session_status_8),
                    )
                },
                actions = {
                    DialogAction(onClick = {
                        activity?.finish()
                    }) {
                        Text(
                            text = stringResource(android.R.string.ok),
                            style = TextStyle(
                                color = Color.Blue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            )
        }
    }

    override fun onEndSession(sessionId: String) {

    }
}