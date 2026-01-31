package com.lbe.imsdk.pages.conversation.vm

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbe.imsdk.extension.appContext
import com.lbe.imsdk.extension.launchAsync
import com.lbe.imsdk.service.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 *
 * @Author mocaris
 * @Date 2026-01-31
 * @Since
 */
abstract class ConversationStateVM : ViewModel() {
    val textFieldValue = mutableStateOf(TextFieldValue())
    val editFocusRequester = FocusRequester()
    val isRefreshing = mutableStateOf(false)
    val networkMonitor = NetworkMonitor(appContext)
    val scrollToBottomEvent = MutableSharedFlow<Boolean>()

    init {
        addCloseable(networkMonitor.also {
            it.startMonitoring()
        })
        viewModelScope.launchAsync {
            networkMonitor.isConnected.collect(::onNetChanged)
        }
    }

    abstract fun onNetChanged(isConnected: Boolean)

    override fun onCleared() {
        editFocusRequester.freeFocus()
        super.onCleared()
    }


    fun scrollToBottom(anim: Boolean = true) {
        viewModelScope.launch {
            delay(200)
            scrollToBottomEvent.emit(anim)
        }
    }



}

