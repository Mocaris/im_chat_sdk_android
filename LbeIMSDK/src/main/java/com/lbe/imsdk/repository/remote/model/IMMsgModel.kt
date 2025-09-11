package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.repository.model.proto.IMMsg
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 消息
 *
 * @Date 2025-08-19
 */
@Serializable
data class IMMsgModel(
    //客户端自定义消息ID
    val clientMsgID: String = UUID.randomUUID().toString(),
    /**
     * 时间
     */
    val createTime: Long = 0,
    /**
     *消息体内容
     */
    val msgBody: String = "",
    /**
     * 消息序号
     */
    val msgSeq: Long = 0,
    /**
     * 消息类型
     * [IMMsgContentType]
     */
    val msgType: Int = IMMsg.ContentType.InvalidContentType_VALUE,
    /**
     * 接收者ID
     */
    val receiverUid: String = "",
    /**
     * 发送时间
     */
    val sendTime: Long = 0,
    /**
     * 发送者头像
     */
    val senderFaceURL: String = "",
    /**
     * 发送者昵称
     */
    val senderNickname: String = "",
    /**
     * 发送者ID
     */
    val senderUid: String = "",
    /**
     * 服务器消息ID
     */
    val serverMsgID: String = "",
    /**
     * 会话ID
     */
    val sessionId: String = "",
    /**
     * 消息状态
     * [MsgStatus]
     */
    val status: Int = IMMsgReadStatus.UNREAD,
    /**
     * 消息标题
     */
    val title: String = "",
) : Comparable<IMMsgModel> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IMMsgModel

        return clientMsgID == other.clientMsgID
    }

    override fun hashCode(): Int {
        return clientMsgID.hashCode()
    }

    override fun compareTo(other: IMMsgModel): Int {
        return msgSeq.compareTo(other.msgSeq)
    }
}


