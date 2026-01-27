package com.lbe.imsdk.manager

import androidx.compose.runtime.mutableStateOf
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.pages.navigation.*
import com.lbe.imsdk.repository.model.*
import com.lbe.imsdk.repository.remote.*
import com.lbe.imsdk.repository.remote.model.LbeIMConfigModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.*

/**
 *
 *
 * @Date 2025-07-16
 */
object LbeIMSDKManager  {
    const val TEXT_CONTENT_LENGTH = 1000

    var sdkInitConfig: SDKInitConfig? = null
        private set
    val sdkInitLoading = mutableStateOf(false)

    val sdkInitException = mutableStateOf<Exception?>(null)

    private val scope by lazy { CoroutineScope(Dispatchers.Default) }

    //init job
    private var initJob: Job? = null
    var imApiRepository: LbeImApiRepository? = null
        private set
    var ossApiRepository: LbeOssApiRepository? = null
        private set
    var socketManager: SocketManager? = null
        private set

    val initSuccessful get() = sdkInitConfig != null && (socketManager != null || ossApiRepository != null || imApiRepository != null)

    fun startInit(sdkInitConfig: SDKInitConfig) {
//        PageRoute.routes.off(PageRoute.Test)
//        return
        this.sdkInitConfig = sdkInitConfig
        if (initJob?.isActive == true) {
            initJob?.cancel()
        }
        initJob = initInternal()
    }


    private fun initInternal() = scope.launch(Dispatchers.IO) {
        sdkInitLoading.value = true
        var initCount = 0
        do {
            initCount += 1
            try {
                realInit()
                sdkInitException.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                sdkInitException.value = e
            }
            if (initSuccessful) {
                break
            }
            delay(3000)
        } while (!initSuccessful && initCount < 3)
        sdkInitLoading.value = false
        if (!initSuccessful) {
            sdkInitException.value?.message?.showToast()
        }
    }

    private var hostData: LbeIMConfigModel.HostData? = null
    private suspend fun realInit() {
        with(sdkInitConfig ?: throw Exception("sdkInitConfig is null")) {
            val initConfig = this@with
            val apiRepository = LbeApiRepository(domain = initConfig.domain)
            hostData = getHostConfig(initConfig.domain)
            if (null == hostData || hostData!!.hasConfigEmpty()) {
                hostData = apiRepository.initConfig()
            } else {
                scope.launchIO {
                    try {
                        val data = apiRepository.initConfig()
                        saveHostConfig(initConfig.domain, data)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        e.catchException()
                    }
                }
            }
            with(hostData!!.rest) {
                if (this.isNotEmpty()) {
                    imApiRepository = LbeImApiRepository(this.first())
                }
            }
            with(hostData!!.oss) {
                if (this.isNotEmpty()) {
                    ossApiRepository = LbeOssApiRepository(this.first())
                }
            }
            //init socket manager
            with(hostData!!.ws) {
                if (this.isNotEmpty()) {
                    socketManager = SocketManager(this.first())
                }
            }
            PageRoute.routes.offAll(PageRoute.Conversation)
        }
    }

}