package com.lbe.imsdk.manager

import android.util.Log
import coil3.util.Logger
import com.lbe.imsdk.extension.launchAsync
import com.lbe.imsdk.extension.launchMain
import com.lbe.imsdk.repository.db.entry.*
import com.lbe.imsdk.repository.local.LbeImDataRepository
import com.lbe.imsdk.repository.local.insert
import com.lbe.imsdk.repository.model.proto.*
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.service.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.*
import java.security.Identity

/**
 * socket manager
 * @Date 2025-08-18
 */
class SocketManager(
    private val hostUrl: String,
    session: CreateSessionResModel.SessionData
) : Closeable, SocketClient.SocketCallback {

    private val scope = CoroutineScope(Dispatchers.IO)

    val connectState = MutableStateFlow(SocketClient.ConnectState.CLOSED)

    private val socketEventCallbacks = mutableListOf<SocketEventCallback>()

    private val socket by lazy {
        SocketClient.create(
            hostUrl = hostUrl,
            lbeToken = session.token,
            lbeSession = session.sessionId,
            callback = this@SocketManager
        )
    }

    private val pingMsg = IMMsg.MsgEntityToServer.newBuilder().setMsgType(IMMsg.MsgType.TextMsgType)
        .setMsgBody(IMMsg.MsgBody.newBuilder().setMsgBody("ping").build()).build()

    init {
        socket.connect()
    }

    fun addSocketEventCallback(callback: SocketEventCallback) {
        socketEventCallbacks.add(callback)
    }

    fun removeSocketEventCallback(callback: SocketEventCallback) {
        socketEventCallbacks.remove(callback)
    }

    fun connect() {
        socket.connect()
    }

    fun disconnect() {
        socket.disconnect()
    }

    override fun getPingMessage(): ByteArray {
        return pingMsg.toByteArray()
    }

    override fun onConnectStateChange(state: SocketClient.ConnectState) {
        connectState.value = state
        Log.d("SocketManager", "onConnectStateChange: $state")
    }

    override fun onReceiveMessage(byteArray: ByteArray) {
        scope.launch {
            try {
                val msg = IMMsg.MsgEntityToFrontEnd.parseFrom(byteArray)
                Log.d("SocketManager", "onReceiveMessage: ${msg.msgBody}")
                when (msg.msgType) {
                    IMMsg.MsgType.TextMsgType -> {
                        /// parse to IMMessage, save to database
                        val imMessage = IMMessageEntry.fromProtobuf(msg.msgBody)
                        scope.launch {
                            for (callback in socketEventCallbacks) {
                                callback.onReceiveMessage(imMessage)
                            }
                        }
                        imMessage.insert()
                    }

                    //客服接入
                    IMMsg.MsgType.AgentUserJoinSessionMsgType -> {
                        with(msg.agentUserJoinSessionMsg) {

                        }
                    }

                    //消息已读
                    IMMsg.MsgType.HasReadReceiptMsgType -> {
                        val receiptMsg = msg.hasReadReceiptMsg
                        with(receiptMsg) {
                            scope.launch {
                                for (callback in socketEventCallbacks) {
                                    callback.onReadMessage(sessionID, hasReadSeqsList)
                                }
                            }
                            val count =
                                LbeImDataRepository.updateReadStatus(sessionID, hasReadSeqsList)
                            Log.d("SocketManager", "updateReadStatus: $count")
                        }
                    }

                    IMMsg.MsgType.KickOffLineMsgType -> {
                        scope.launch {
                            for (callback in socketEventCallbacks) {
                                callback.onKickOffLine()
                            }
                        }
                        Log.d("SocketManager", "onKickOffLine: ")
                    }

                    IMMsg.MsgType.EndSessionMsgType -> {
                        with(msg.endSessionMsg) {
                            scope.launch {
                                for (callback in socketEventCallbacks) {
                                    callback.onEndSession(sessionID)
                                }
                            }
                            Log.d("SocketManager", "onEndSession: $sessionID")
                        }
                    }

                    IMMsg.MsgType.JoinServer -> {

                    }

                    IMMsg.MsgType.CreateSessionMsgType -> {
                        with(msg.createSessionMsg) {

                        }
                    }

                    IMMsg.MsgType.UNRECOGNIZED -> {

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onReceiveMessage(text: String) {
        Log.d("SocketClient", "onReceiveMessage text: $text")
//        val msg = IMMsg.MsgEntityToFrontEnd.parseFrom(text.toByteArray())
    }

    override fun close() {
        socket.close()
    }

}

interface SocketEventCallback {
    fun onReceiveMessage(message: IMMessageEntry)
    fun onReadMessage(sessionId: String, seqList: List<Long>)
    fun onKickOffLine()
    fun onEndSession(sessionId: String)

}