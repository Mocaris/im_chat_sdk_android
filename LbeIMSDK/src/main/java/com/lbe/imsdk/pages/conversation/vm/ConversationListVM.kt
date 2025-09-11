package com.lbe.imsdk.pages.conversation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbe.imsdk.extension.launchAsync
import com.lbe.imsdk.manager.ConversationManager
import com.lbe.imsdk.manager.LbeIMSDKManager
import kotlinx.coroutines.launch

/**
 *
 *
 * @Date 2025-08-19
 */
class ConversationListVM(private val sessionId: String) : ViewModel() {

    init {

    }

    fun getSessionList() {
    }


}