package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.service.http.model.NetResponseData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 会话消息列表
 */
@Serializable
data class SessionMsgListResModel(
    override val code: Int,
    override val msg: String = "",
    override val dlt: String = "",
    @SerialName("data")
    override val data: SessionMsgListDataModel? = null,
) : NetResponseData<SessionMsgListResModel.SessionMsgListDataModel>() {

    @Serializable
    data class SessionMsgListDataModel(
        val total: Long = 0L,
        val content: List<IMMsgModel> = emptyList()
    )


}