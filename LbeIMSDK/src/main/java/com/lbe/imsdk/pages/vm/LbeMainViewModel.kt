package com.lbe.imsdk.pages.vm

import androidx.lifecycle.ViewModel
import com.lbe.imsdk.components.DialogManager
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.repository.model.SDKInitConfig

/**
 * Lbe Main ViewModel
 *
 * @Date 2025-07-16
 */
class LbeMainViewModel : ViewModel() {

    val dialogManager by lazy { DialogManager() }

    fun initSdk(sdkInitConfig: SDKInitConfig) {
        LbeIMSDKManager.startInit(sdkInitConfig)
    }


    override fun onCleared() {
        dialogManager.dismissAll()
        super.onCleared()
    }
}