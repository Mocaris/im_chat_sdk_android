package com.lbe.imsdk.repository.remote.model

import androidx.room.Entity
import com.lbe.imsdk.service.http.model.NetResponseData
import kotlinx.serialization.Serializable

/**
 * 会话列表
 */
@Serializable
data class SessionListResModel(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    override val data: SessionListDataModel? = null,
) : NetResponseData<SessionListResModel.SessionListDataModel>() {

    @Serializable
    data class SessionListDataModel(
        ///trans/session-list 为 0
        val total: Long = 0,
        val sessionList: List<SessionListModel> = emptyList()
    )
}

/**
 * 受支持的会话列表
 */
@Serializable
data class SupportSessionListResModel(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    override val data: SupportSessionDataModel? = null,
) : NetResponseData<SupportSessionListResModel.SupportSessionDataModel>() {

    @Serializable
    data class SupportSessionDataModel(
        val sessionIDs: List<String> = emptyList()
    )
}

@Serializable
@Entity
data class SessionListModel(
    val createTime: Long = 0,
    //1-游客 2-非游客
    val customerType: Int = 0,
    val devNo: String = "",
    val extra: String = "",
    val headIcon: String = "",
    val language: String = "",
    val latestMsg: IMMsgModel? = null,
    val nickID: String = "",
    val nickName: String = "",
    val sessionId: String = "",
    val source: String = "",
    //// 0-等待接入 1-开始 2-结束 3排队中
    val status: Int = 0,
    val uid: String = "",
    val unreadCount: Int = 0,

    ) {
    val isCustomService: Boolean get() = status ==1
}