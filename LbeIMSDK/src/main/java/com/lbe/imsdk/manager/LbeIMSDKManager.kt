package com.lbe.imsdk.manager

import com.lbe.imsdk.extension.*
import com.lbe.imsdk.pages.navigation.*
import com.lbe.imsdk.repository.model.*
import com.lbe.imsdk.repository.remote.*
import com.lbe.imsdk.repository.remote.model.LbeIMConfigModel
import com.lbe.imsdk.repository.remote.model.SourceUrl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.io.*
import kotlin.collections.first

/**
 *
 *
 * @Date 2025-07-16
 */
object LbeIMSDKManager : Closeable {
    var sdkInitConfig: SDKInitConfig? = null
        private set
    val sdkInitLoading = MutableStateFlow(false)

    private val scope by lazy { CoroutineScope(Dispatchers.Default) }

    internal var lbeToken: String? = null
    internal var lbeSession: String? = null

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
        sdkInitLoading.value = false
        do {
            try {
                realInit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (initSuccessful) {
                break
            }
            delay(3000)
        } while (!initSuccessful)
        sdkInitLoading.value = true
    }

    private suspend fun realInit() {
        reset()
        val sdkInitConfig = sdkInitConfig ?: throw Exception("sdkInitConfig is null")
        val apiRepository = LbeApiRepository(domain = sdkInitConfig.domain)
        var hostData = getHostConfig(sdkInitConfig.domain)
        if (null == hostData || hostData.hasConfigEmpty()) {
            hostData = apiRepository.initConfig()
        } else {
            scope.launchIO {
                try {
                    val data = apiRepository.initConfig()
                    saveHostConfig(sdkInitConfig.domain, data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (hostData.rest.isNotEmpty()) {
            imApiRepository = LbeImApiRepository(hostData.rest.first())
        }
        if (hostData.oss.isNotEmpty()) {
            ossApiRepository = LbeOssApiRepository(hostData.oss.first())
        }
        initSession(sdkInitConfig, hostData)
    }

    private suspend fun initSession(
        sdkInitConfig: SDKInitConfig,
        hostData: LbeIMConfigModel.HostData
    ) {
        val session = imApiRepository?.createSession(
            device = sdkInitConfig.device,
            email = sdkInitConfig.email,
            extraInfo = Json.encodeToString(sdkInitConfig.extraInfo.map { it.key to it.value.toString() }
                .toMap()),
            groupID = sdkInitConfig.groupID,
            headIcon = if (sdkInitConfig.parseHeaderIcon is SourceUrl) "" else sdkInitConfig.headerIcon,
            language = sdkInitConfig.supportLanguage,
            nickId = sdkInitConfig.nickId,
            nickName = sdkInitConfig.nickName,
            phone = sdkInitConfig.phone,
            source = sdkInitConfig.source,
            uid = ""
        )?.also {
            lbeToken = it.token
            lbeSession = it.sessionId
        } ?: return
        //init socket manager
        if (hostData.ws.isNotEmpty()) {
            socketManager = SocketManager(hostData.ws.first(), session)
        }
        PageRoute.routes.offAll(PageRoute.Conversation(session))
    }

    private fun reset() {
        socketManager?.close()
        socketManager = null
    }

    override fun close() {
        scope.cancel()
    }

}