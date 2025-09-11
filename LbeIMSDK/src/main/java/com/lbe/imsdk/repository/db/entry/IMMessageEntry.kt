package com.lbe.imsdk.repository.db.entry

import androidx.compose.runtime.*
import androidx.room.*
import com.lbe.imsdk.repository.db.entry.convert.TempLocalSourceCovert
import com.lbe.imsdk.repository.db.entry.convert.UploadTaskConvert
import com.lbe.imsdk.repository.model.proto.IMMsg
import com.lbe.imsdk.repository.remote.api.params.SendMessageBody
import com.lbe.imsdk.repository.remote.model.AgentUser
import com.lbe.imsdk.repository.remote.model.FaqAnswerMessageContent
import com.lbe.imsdk.repository.remote.model.FaqMessageContent
import com.lbe.imsdk.repository.remote.model.IMMsgModel
import com.lbe.imsdk.repository.remote.model.KnowledgePointMessageContent
import com.lbe.imsdk.repository.remote.model.MediaMessageContent
import com.lbe.imsdk.repository.remote.model.RankingMessageContent
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgSendStatus
import kotlinx.serialization.json.*
import java.util.*

/**
 * im message entry
 * @Date 2025-08-21
 */
@Entity(
    tableName = "tb_msg",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["client_msg_id"]),
        Index(value = ["server_msg_id"]),
        Index(value = ["session_id", "msg_seq"]),
        Index(value = ["session_id", "client_msg_id"]),
        Index(value = ["session_id", "server_msg_id"]),
    ]
)
@TypeConverters(
    TempLocalSourceCovert::class,
    UploadTaskConvert::class
)
data class IMMessageEntry(

    @PrimaryKey(autoGenerate = false) @ColumnInfo(
        name = "client_msg_id", typeAffinity = ColumnInfo.TEXT
    ) val clientMsgID: String = UUID.randomUUID().toString(),

    /**
     * 消息序号
     */
    @ColumnInfo(name = "msg_seq", typeAffinity = ColumnInfo.INTEGER) var msgSeq: Long = 0,
    //客户端自定义消息ID


    /**
     * 时间
     */
    @ColumnInfo(name = "create_time", typeAffinity = ColumnInfo.INTEGER) val createTime: Long = 0,
    /**
     *消息体内容
     */
    @ColumnInfo(name = "msg_body", typeAffinity = ColumnInfo.TEXT) var msgBody: String = "",

    /**
     * 消息类型
     * [IMMsgContentType]
     */
    @ColumnInfo(
        name = "msg_type", typeAffinity = ColumnInfo.INTEGER
    ) val msgType: Int = IMMsgContentType.InvalidContentType,
    /**
     * 接收者ID
     */
    @ColumnInfo(name = "receiver_uid", typeAffinity = ColumnInfo.TEXT) val receiverUid: String = "",
    /**
     * 发送时间
     */
    @ColumnInfo(name = "send_time", typeAffinity = ColumnInfo.TEXT) var sendTime: String = "",
    /**
     * 发送者头像
     */
    @ColumnInfo(
        name = "sender_face_url", typeAffinity = ColumnInfo.TEXT
    ) val senderFaceURL: String = "",
    /**
     * 发送者昵称
     */
    @ColumnInfo(
        name = "sender_nickname", typeAffinity = ColumnInfo.TEXT
    ) val senderNickname: String = "",
    /**
     * 发送者ID
     */
    @ColumnInfo(name = "sender_uid", typeAffinity = ColumnInfo.TEXT) val senderUid: String = "",
    /**
     * 服务器消息ID
     */
    @ColumnInfo(
        name = "server_msg_id", typeAffinity = ColumnInfo.TEXT
    ) val serverMsgID: String = "",
    /**
     * 会话ID
     */
    @ColumnInfo(name = "session_id", typeAffinity = ColumnInfo.TEXT) val sessionId: String = "",
    /**
     * 消息状态
     * [MsgStatus]
     */
    @ColumnInfo(
        name = "status", typeAffinity = ColumnInfo.INTEGER
    ) var status: Int = IMMsgReadStatus.UNREAD,

    /**
     * 消息标题
     */
    @ColumnInfo(name = "title", typeAffinity = ColumnInfo.TEXT) val title: String = "",

    /**
     * 发送状态
     */
    @ColumnInfo(name = "send_status") var sendStatus: Int = IMMsgSendStatus.SUCCESS,

    /**
     * 本地临时媒体文件 (图片/视频/音频)
     * 本地路径
     */
    @ColumnInfo(name = "temp_source") var localTempSource: MediaMessageContent? = null,

    /**
     * 上传任务
     * 只保存 原始文件进度
     */
    @ColumnInfo(name = "upload_task") var uploadTask: IMUploadTask? = null,

    ) : Comparable<IMMessageEntry> {

    companion object {

        fun createByMsgBody(
            msgBody: SendMessageBody,
            // 发送方uid
            uid: String,
            sessionId: String,
        ): IMMessageEntry {
            return IMMessageEntry(
                clientMsgID = msgBody.clientMsgID,
                msgBody = msgBody.msgBody,
                msgType = msgBody.msgType,
                sendTime = msgBody.sendTime,
                senderUid = uid,
                sessionId = sessionId,
            )
        }


        fun fromProtobuf(msgBody: IMMsg.MsgBody): IMMessageEntry {
            return IMMessageEntry(
                clientMsgID = msgBody.clientMsgID,
                createTime = msgBody.createTime,
                msgBody = msgBody.msgBody,
                msgSeq = msgBody.msgSeq.toLong(),
                msgType = msgBody.msgType.number,
                receiverUid = msgBody.receiverUid,
                sendTime = msgBody.sendTime,
                senderFaceURL = msgBody.senderFaceURL,
                senderNickname = msgBody.senderNickname,
                senderUid = msgBody.senderUid,
                serverMsgID = msgBody.serverMsgID,
                sessionId = msgBody.sessionId,
                title = msgBody.title,
            )
        }

        fun fromIMMsgModel(model: IMMsgModel): IMMessageEntry {
            return IMMessageEntry(
                clientMsgID = model.clientMsgID,
                createTime = model.createTime,
                msgBody = model.msgBody,
                msgSeq = model.msgSeq,
                msgType = model.msgType,
                receiverUid = model.receiverUid,
                sendTime = model.sendTime.toString(),
                senderFaceURL = model.senderFaceURL,
                senderNickname = model.senderNickname,
                senderUid = model.senderUid,
                serverMsgID = model.serverMsgID,
                sessionId = model.sessionId,
                title = model.title,
            )
        }

    }


    val readMutableState by lazy { mutableIntStateOf(this.status) }

    fun updateReadStatus(@IMMsgReadStatus status: Int) {
        this.status = status
        readMutableState.intValue = this.status
    }


    val sendMutableState by lazy { mutableIntStateOf(this.sendStatus) }

    fun updateSendStatus(@IMMsgSendStatus sendStatus: Int) {
        this.sendStatus = sendStatus
        sendMutableState.intValue = this.sendStatus
    }

    val sendProgress by lazy {
        mutableFloatStateOf(0f)
    }

    fun updateSendProgress(progress: Float) {
        sendProgress.floatValue = progress
    }


    @Ignore
    val sendDate = sendTime.toLongOrNull()?.let { Date(it) }

    @Ignore
    val senderFaceUrl = try {
        Json.decodeFromString<SourceUrl>(senderFaceURL)
    } catch (e: Exception) {
        senderFaceURL
    }


    // type 2 3
    val mediaBodyContent by lazy {
        mutableStateOf(
            try {
                MediaMessageContent.fromJson(msgBody)
            } catch (e: Exception) {
                null
            }
        )
    }

    //type 5
    val userArentContent by lazy {
        try {
            AgentUser.fromJson(msgBody)
        } catch (e: Exception) {
            null
        }
    }

    //type 8
    val faqBodyContent by lazy {
        try {
            FaqMessageContent.fromJson(msgBody)
        } catch (e: Exception) {
            null
        }
    }

    //type 9
    val faqKnowledgePointContent by lazy {
        try {
            KnowledgePointMessageContent.fromJson(msgBody)
        } catch (e: Exception) {
            null
        }
    }

    // type 10
    val faqAnswerBodyContent by lazy {
        try {
            FaqAnswerMessageContent.fromJson(msgBody)
        } catch (e: Exception) {
            null
        }
    }

    // type 7
    val rankingBodyContent by lazy {
        try {
            RankingMessageContent.fromJson(msgBody)
        } catch (e: Exception) {
            null
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IMMessageEntry

        return clientMsgID == other.clientMsgID
    }

    override fun hashCode(): Int {
        return clientMsgID.hashCode()
    }

    override fun compareTo(other: IMMessageEntry): Int {
        return msgSeq.compareTo(other.msgSeq)
    }

}