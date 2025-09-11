package com.lbe.imsdk.pages.vm

import androidx.lifecycle.*
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.repository.model.SDKInitConfig

/**
 * Lbe Main ViewModel
 *
 * @Date 2025-07-16
 */
class LbeMainViewModel : ViewModel() {

    fun initSdk(sdkInitConfig: SDKInitConfig) {
        LbeIMSDKManager.startInit(sdkInitConfig)
    }
}