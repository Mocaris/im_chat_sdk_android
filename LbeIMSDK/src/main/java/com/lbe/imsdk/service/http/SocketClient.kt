package com.lbe.imsdk.service.http

import android.os.*
import android.util.Log
import com.lbe.imsdk.provider.AppLifecycleObserver
import com.lbe.imsdk.service.http.HttpClient.logInterceptor
import okhttp3.*
import okio.*
import java.io.Closeable
import java.util.concurrent.*

/**
 *
 * @Author mocaris
 * @Date 2025-08-27
 */
interface SocketClient : Closeable {
    fun connectState(): ConnectState
    fun connect()
    fun disconnect()
    fun sendMessage(msg: ByteArray): Boolean
    fun sendMessage(msg: String): Boolean
    override fun close()

    companion object {
        fun create(
            hostUrl: String,
            pingInterval: Long = 15 * 1000L,
            lbeToken: String,
            lbeSession: String,
            callback: SocketCallback
        ): SocketClient {
            return SocketClientImpl(hostUrl, pingInterval, lbeToken, lbeSession, callback)
        }
    }

    enum class ConnectState {
        CONNECTING,
        OPENED,
        CLOSING,
        CLOSED,
        ERROR;

        val isConnected get() = this == OPENED
        val isClosed get() = this == CLOSED || this == ERROR
        val isConnecting get() = this == CONNECTING
    }

    interface SocketCallback {

        abstract fun getPingMessage(): ByteArray

        abstract fun onConnectStateChange(state: ConnectState)

        abstract fun onReceiveMessage(byteArray: ByteArray)

        abstract fun onReceiveMessage(text: String)

    }
}


private class SocketClientImpl(
    private val hostUrl: String,
    val pingInterval: Long = 15 * 1000L,
    val lbeToken: String,
    val lbeSession: String,
    val callback: SocketClient.SocketCallback
) : SocketClient, WebSocketListener() {
    companion object {
        const val retryInterval = 5 * 1000L

        const val PING_WHAT = 0X01
        private const val RETRY_WHAT = 0X02
    }

    private val okHttpClient = OkHttpClient
        .Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
//        .pingInterval(pingInterval, TimeUnit.MILLISECONDS)
        .addInterceptor(logInterceptor)
        .build()


    private var okSocket: WebSocket? = null

    private var state: SocketClient.ConnectState = SocketClient.ConnectState.CLOSED

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                PING_WHAT -> {
                    sendMessage(callback.getPingMessage())
                    this.sendEmptyMessageDelayed(PING_WHAT, pingInterval)
                }

                RETRY_WHAT -> {
                    if (!reTry) {
                        return
                    }
//                    ///app 处于后台时候不重连，避免性能消耗
//                    if (AppLifecycleObserver.appBackState.value) {
//                        // 继续发送重连请求
//                        retryHandle()
//                        return
//                    }
                    connectInternal()
                }
            }
        }
    }

    private var reTry = true

    private fun changeState(state: SocketClient.ConnectState) {
        this.state = state
        callback.onConnectStateChange(this.state)
    }

    override fun connectState(): SocketClient.ConnectState {
        return state
    }

    override fun connect() {
        reTry = true
        connectInternal()
    }

    private fun connectInternal() = synchronized(this) {
        Log.d("SocketClient", "connect: ${state.name}")
        if (state.isConnecting || state.isConnected) {
            return
        }
        release()
        changeState(SocketClient.ConnectState.CONNECTING)
        okSocket = okHttpClient.newWebSocket(
            request = Request.Builder()
                .also {
                    it.url(hostUrl)
                    it.addHeader("lbeToken", lbeToken)
                    it.addHeader("lbeSession", lbeSession)
                }
                .build(),
            this@SocketClientImpl
        )
    }

    override fun disconnect() {
        reTry = false
        Log.d("SocketClient", "disconnect")
        release()
    }

    private fun retryHandle() {
        handler.removeMessages(RETRY_WHAT)
        if (reTry) {
            handler.sendEmptyMessageDelayed(RETRY_WHAT, retryInterval)
        }
    }

    private fun cancelRetry() {
        handler.removeMessages(RETRY_WHAT)
    }

    private fun release() {
        reTry = false
        cancelRetry()
        handler.removeCallbacksAndMessages(null)
        if (null != okSocket) {
            okSocket?.cancel()
            okSocket?.close(1000, "releases")
            okSocket = null
        }
    }

    override fun sendMessage(msg: ByteArray): Boolean {
        val res = okSocket?.send(ByteString.of(*msg)) == true
        return res
    }

    override fun sendMessage(msg: String): Boolean {
        return okSocket?.send(msg) == true
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        cancelRetry()
        changeState(SocketClient.ConnectState.OPENED)
        handler.sendEmptyMessage(PING_WHAT)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        handler.removeCallbacksAndMessages(null)
        changeState(SocketClient.ConnectState.CLOSED)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        changeState(SocketClient.ConnectState.CLOSING)
    }

    override fun onFailure(
        webSocket: WebSocket,
        t: Throwable,
        response: Response?
    ) {
        webSocket.cancel()
        changeState(SocketClient.ConnectState.ERROR)
        retryHandle()
        t.printStackTrace()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        callback.onReceiveMessage(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        callback.onReceiveMessage(bytes.toByteArray())
    }

    override fun close() {
        release()
    }


}