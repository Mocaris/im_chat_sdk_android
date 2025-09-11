package com.lbe.imsdk.repository.remote.api

import com.lbe.imsdk.repository.remote.model.CreateSessionResModel
import com.lbe.imsdk.repository.remote.model.SendMsgResModel
import com.lbe.imsdk.repository.remote.model.SessionListResModel
import com.lbe.imsdk.repository.remote.model.SessionMsgListResModel
import com.lbe.imsdk.repository.remote.model.SupportSessionListResModel
import com.lbe.imsdk.repository.remote.model.TimeOutConfigModel
import com.lbe.imsdk.repository.remote.model.VoidResponse
import com.lbe.imsdk.service.http.body.JsonBody
import retrofit2.http.Body
import retrofit2.http.POST

interface LbeIMAPiService {

    /**
     * 创建会话
     * {
     *   "device": "string",
     *   "email": "string",
     *   "extraInfo": "string",
     *   "groupID": "string",
     *   "headIcon": "string",
     *   "language": "string",
     *   "nickId": "string",
     *   "nickName": "string",
     *   "phone": "string",
     *   "source": "string",
     *   "uid": "string"
     * }
     */
    @POST("/miner-api/trans/session")
    suspend fun createSession(
        @Body body: JsonBody
    ): CreateSessionResModel


    /**
     * 获取会话列表
     * {
     *   "sessionIDs": [
     *     "string"
     *   ]
     * }
     */
    @POST("/miner-api/trans/get-sessions")
    suspend fun getSessionList(
        @Body body: JsonBody
    ): SessionListResModel


    /**
     * 获取当前支持的会话列表
     */
    @POST("/miner-api/trans/get-support-session-id")
    suspend fun getSupportSessionList(
        @Body body: JsonBody
    ): SupportSessionListResModel

    /**
     * 获取会话消息
     * {
     *   "seqCondition": {
     *     "endSeq": 0,
     *     "startSeq": 0
     *   },
     *   "sessionId": "string"
     * }
     */
    @POST("/miner-api/trans/history")
    suspend fun getSessionMessage(
        @Body body: JsonBody
    ): SessionMsgListResModel


    /**
     * 发送消息
     * {
     *   "clientMsgID": "string",
     *   "msgBody": "string",
     *   "msgSeq": 0,
     *   "msgType": 0,
     *   "sendTime": "string",
     *   "source": 0
     * }
     */
    @POST("/miner-api/trans/msg-send")
    suspend fun sendMsg(
        @Body body: JsonBody
    ): SendMsgResModel

    /**
     * 超时配置
     */
    @POST("/miner-api/trans/timeout-config")
    suspend fun getTimeoutConfig(
        @Body body: JsonBody
    ): TimeOutConfigModel

    /**
     * 转人工
     */
    @POST("/miner-api/trans/service-support")
    suspend fun serviceSupport(
        @Body body: JsonBody
    ): VoidResponse


    /**
     * 标记已读
     * {
     *   "seq": 0,
     *   "sessionID": "string"
     * }
     */
    @POST("/miner-api/trans/mark-msg-as-read")
    suspend fun markRead(
        @Body body: JsonBody
    ):VoidResponse

    /**
     * faq
     * {
     *   "faqType": 0,
     *   "id": "string"
     * }
     */
    @POST("/miner-api/trans/faq")
    suspend fun faq(
        @Body body: JsonBody
    ): VoidResponse

}