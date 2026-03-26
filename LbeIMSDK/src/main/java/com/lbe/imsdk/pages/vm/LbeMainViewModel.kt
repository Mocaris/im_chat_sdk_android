package com.lbe.imsdk.pages.vm

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lbe.imsdk.R
import com.lbe.imsdk.components.DialogAction
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.components.IMCupertinoDialogContent
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.repository.model.SDKInitConfig
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Lbe Main ViewModel
 *
 * @Date 2025-07-16
 */
class LbeMainViewModel : ViewModel() {

    val dialogManager by lazy { DialogManager() }

    init {
        viewModelScope.launch {
            LbeIMSDKManager.sdkInitException.collect { e ->
                if (e != null) {
                    dialogManager.show(
                        key = "init_sdk_error",
                        properties = DialogProperties(dismissOnClickOutside = false),
                        onDismissRequest = {
                            LbeIMSDKManager.sdkInitException.value = null
                            it()
                        }) {
                        val activity = LocalActivity.current
                        IMCupertinoDialogContent(
                            content = {
                                Text(e.message ?: e.localizedMessage)
                            },
                            actions = listOf(
                                DialogAction(
                                    onClick = {
                                        activity?.finish()
                                    },
                                    content = {
                                        Text(
                                            stringResource(R.string.chat_session_status_30),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun initSdk(sdkInitConfig: SDKInitConfig) {
        LbeIMSDKManager.startInit(sdkInitConfig)
    }


    override fun onCleared() {
        dialogManager.dismissAll()
        super.onCleared()
    }
}