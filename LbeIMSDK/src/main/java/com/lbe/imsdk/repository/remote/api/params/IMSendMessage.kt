package com.lbe.imsdk.repository.remote.api.params

import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.util.*

/**
 * 发送消息 参数
 * @Date 2025-08-21
 */
//{
//  "clientMsgID": "string",
//  "msgBody": "string",
//  "msgSeq": 0,
//  "msgType": 0,
//  "sendTime": "string",
//  "source": 0
//}
@Serializable
class SendMessageBody private constructor(
    val clientMsgID: String,
    var msgBody: String,
//    val msgSeq: Long,

    @field:IMMsgContentType val msgType: Int,
    val sendTime: String,
    @param:ReqSource
    val source: Int = ReqSource.APP,
) {

    companion object Factory {
        /**
         * 创建文本消息 临时
         *  msgSeq -1
         */
        fun createTextMessage(text: String): SendMessageBody {
            return SendMessageBody(
                clientMsgID = UUID.randomUUID().toString(),
                msgBody = text,
                msgType = IMMsgContentType.TEXT_CONTENT_TYPE,
                sendTime = System.currentTimeMillis().toString(),
                source = ReqSource.APP
            )
        }

        fun createMediaMessage(mediaBody: MediaMessageContent, isVideo: Boolean): SendMessageBody {
            return SendMessageBody(
                clientMsgID = UUID.randomUUID().toString(),
                msgBody = Json.encodeToString(mediaBody),
                msgType = if (isVideo) IMMsgContentType.VIDEO_CONTENT_TYPE else IMMsgContentType.IMAGE_CONTENT_TYPE,
                sendTime = System.currentTimeMillis().toString(),
                source = ReqSource.APP
            )
        }

        /**
         *  创建图片消息 临时
         *   msgSeq -1
         */
        fun createImageMessage(mediaBody: MediaMessageContent): SendMessageBody {
            return SendMessageBody(
                clientMsgID = UUID.randomUUID().toString(),
                msgBody = Json.encodeToString(mediaBody),
                msgType = IMMsgContentType.IMAGE_CONTENT_TYPE,
                sendTime = System.currentTimeMillis().toString(),
                source = ReqSource.APP
            )
        }

        /**
         * 视频消息 临时
         * msgSeq -1
         */
        fun createVideoMessage(mediaBody: MediaMessageContent): SendMessageBody {
            return SendMessageBody(
                clientMsgID = UUID.randomUUID().toString(),
                msgBody = Json.encodeToString(mediaBody),
                msgType = IMMsgContentType.VIDEO_CONTENT_TYPE,
                sendTime = System.currentTimeMillis().toString(),
                source = ReqSource.APP
            )
        }

        fun createByMessageEntry(imMsgEntry: IMMessageEntry): SendMessageBody {
            return SendMessageBody(
                clientMsgID = imMsgEntry.clientMsgID,
                msgBody = imMsgEntry.msgBody,
                msgType = imMsgEntry.msgType,
                sendTime = imMsgEntry.sendTime,
                source = ReqSource.APP
            )
        }
    }

}