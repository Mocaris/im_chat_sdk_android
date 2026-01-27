package com.lbe.imsdk.repository.remote

import com.lbe.imsdk.repository.remote.api.*
import com.lbe.imsdk.repository.remote.api.params.*
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.SessionMsgListResModel.SessionMsgListDataModel
import com.lbe.imsdk.repository.remote.model.enumeration.FaqType
import com.lbe.imsdk.repository.remote.model.enumeration.UserType
import com.lbe.imsdk.service.http.*
import com.lbe.imsdk.service.http.body.*
import com.lbe.imsdk.service.http.extension.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.json.*
import retrofit2.http.Body
import retrofit2.http.POST


class LbeImApiRepository(private val imBaseUrl: String) {
    private val apiService by lazy {
        HttpClient.retrofitFactory(imBaseUrl).create(LbeIMAPiService::class.java)
    }

    suspend fun createSession(
        device: String,
        email: String,
        extraInfo: String,
        groupID: String,
        headIcon: String,
        language: String,
        nickId: String,
        nickName: String,
        phone: String,
        source: String,
        uid: String
    ): CreateSessionResModel.SessionData = withContext(Dispatchers.IO) {
        apiService.createSession(
            body = mutableMapOf(
                "device" to device.ifEmpty {
                    // 获取设备信息
                    "${android.os.Build.BRAND} ${android.os.Build.MODEL}"
                },
                "email" to email,
                "extraInfo" to extraInfo,
                "groupID" to groupID,
                "headIcon" to headIcon,
                "language" to language,
                "nickId" to nickId,
                "nickName" to nickName,
                "phone" to phone,
                "source" to source,
                "uid" to uid
            ).asJsonBody
        ).accept()!!
    }

    /**
     * 获取会话列表
     * @param sessionIds 会话ID列表
     */
    suspend fun getSessionList(sessionIds: List<String>) = withContext(Dispatchers.IO) {
        apiService.getSessionList(
            body = mutableMapOf(
                "sessionIDs" to sessionIds
            ).asJsonBody
        ).accept()
    }

    /**
     * 获取历史会话列表
     * @param  page 页码
     * @param size 页大小
     * @param sessionType 会话类型 1 历史，2 全部
     */
    suspend fun getHistorySessionList(page: Int, size: Int, sessionType: Int) =
        withContext(Dispatchers.IO) {
            apiService.getHistorySessionList(
                body = mutableMapOf(
                    "sessionType" to sessionType,
                    "pagination" to mutableMapOf(
                        "pageNumber" to page,
                        "showNumber" to size
                    )
                ).asJsonBody
            ).accept()
        }

    /**
     * 获取当前支持的会话列表
     */
    suspend fun getSupportSessionList(sessionIds: List<String>) = withContext(Dispatchers.IO) {
        apiService.getSupportSessionList(
            body = emptyMap<String, Any>().asJsonBody
        ).accept()
    }

    /**
     * 获取会话消息
     * @param sessionId 会话ID
     * @param startSeq 起始序号 0 拉取最新
     * @param endSeq 结束序号
     */
    suspend fun getSessionMessage(
        sessionId: String,
        startSeq: Long,
        endSeq: Long,
    ): SessionMsgListDataModel? = withContext(Dispatchers.IO) {
        apiService.getSessionMessage(
            body = mutableMapOf(
                "sessionId" to sessionId,
                "seqCondition" to mutableMapOf(
                    "startSeq" to startSeq,
                    "endSeq" to endSeq,
                ),
            ).asJsonBody
        ).accept()
    }

    /**
     * 获取受支持的会话列表
     */
//
//    suspend fun fetchSessionList(
//        lbeToken: String, lbeIdentity: String, body: SessionListParams
//    ): SessionListRep {
//        return apiService.fetchSessionList(
//            lbeToken = lbeToken, lbeIdentity = lbeIdentity, body
//        )
//    }
//
//    suspend fun createSession(lbeSign: String, lbeIdentity: String, body: SessionBody): Session {
//        return apiService.createSession(lbeSign = lbeSign, lbeIdentity = lbeIdentity, body)
//    }
//
//    suspend fun fetchHistory(
//        lbeSign: String, lbeToken: String, lbeIdentity: String, body: HistoryBody
//    ): History {
//        return apiService.fetchHistory(
//            lbeSign = lbeSign, lbeToken = lbeToken, lbeIdentity = lbeIdentity, body = body
//        )
//    }

    /**
     * 发送消息
     *
     */
    suspend fun sendMsg(
        message: SendMessageBody
    ): SendMsgResModel {
        return apiService.sendMsg(
            body = JSONObject(
                Json.encodeToString(message)
            ).asJsonBody
        )
    }

    /**
     * 获取超时配置
     */
    suspend fun getTimeoutConfig(
        @UserType userType: Int = UserType.APP
    ): TimeOutConfigModel.TimeOutConfigData? {
        return apiService.getTimeoutConfig(
            body = JSONObject(
                mapOf(
                    "userType" to userType
                )
            ).asJsonBody
        ).accept()
    }

    /**
     * 转人工
     */
    suspend fun serviceSupport() {
        apiService.serviceSupport(
            body = JSONObject().asJsonBody
        ).accept()
    }


    /**
     * 标记已读
     * {
     *   "seq": 0,
     *   "sessionID": "string"
     * }
     */
    suspend fun markRead(
        seq: Long,
        sessionID: String,
    ) {
        apiService.markRead(
            body = JSONObject(
                mapOf(
                    "seq" to seq,
                    "sessionID" to sessionID
                )
            ).asJsonBody
        ).accept()

    }

    /**
     * faq
     * {
     *   "faqType": 0,
     *   "id": "string"
     * }
     */
    suspend fun faq(
        faqType: FaqType,
        id: String,
    ) {
        apiService.faq(
            body = JSONObject(
                mapOf(
                    "faqType" to faqType.value,
                    "id" to id
                )
            ).asJsonBody
        ).accept()
    }

}