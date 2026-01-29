package com.lbe.imsdk.pages.conversation.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.lbe.imsdk.extension.launchIO
import com.lbe.imsdk.extension.tryCatchCoroutine
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.repository.remote.model.TimeOutConfigModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 *
 * @Author mocaris
 * @Date 2026-01-29
 * @Since
 */
class ConversationTimeoutState(val viewModel: CurrentConversationVM) {
    val timeOutReply = mutableStateOf(false)

    private var timeOutReplyJob: Job? = null
    val timeOutConfig = mutableStateOf<TimeOutConfigModel.TimeOutConfigData?>(null)

    private fun getTimeoutConfig() {
        viewModel.viewModelScope.launchIO {
            if (null != timeOutConfig.value) {
                return@launchIO
            }
            tryCatchCoroutine { LbeIMSDKManager.imApiRepository?.getTimeoutConfig() }?.let {
                timeOutConfig.value = it
            }
        }
//    }


        // 开启超时未回复 job
        fun launchTimeOutJob(msgSeq: Long) {
            timeOutReplyJob?.cancel()
            val config = timeOutConfig.value ?: return
            if (!config.isOpen) {
                return
            }
            timeOutReplyJob = viewModel.viewModelScope.launch {
                timeOutReply.value = false
                delay(config.timeout.toDuration(DurationUnit.MINUTES))
                viewModel.msgList.maxByOrNull { it.msgSeq }?.let {
                    if (it.msgSeq <= msgSeq) {
                        timeOutReply.value = true
                    }
                }
            }
        }
    }
}