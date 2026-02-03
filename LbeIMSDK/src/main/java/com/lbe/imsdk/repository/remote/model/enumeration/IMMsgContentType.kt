package com.lbe.imsdk.repository.remote.model.enumeration

import androidx.annotation.IntDef
import com.lbe.imsdk.repository.model.proto.IMMsg

/**
 *
 *  消息体类型
 * @Date 2025-08-19
 */
//@Retention(AnnotationRetention.SOURCE)
//@IntDef(
//    value = [
//        IMMsgContentType.INVALID_CONTENT_TYPE,
//        // 文本
//        IMMsgContentType.TEXT_CONTENT_TYPE,
//        // 图片
//        IMMsgContentType.IMAGE_CONTENT_TYPE,
//        // 视频
//        IMMsgContentType.VIDEO_CONTENT_TYPE,
//        //  C端接入(只推送给B端)
//        IMMsgContentType.CREATE_SESSION_CONTENT_TYPE,
//        //  客服接入(只推送给C端)
//        IMMsgContentType.AGENT_USER_JOIN_SESSION_CONTENT_TYPE,
//        // 会话结束(只推送给C端)
//        IMMsgContentType.END_SESSION_CONTENT_TYPE,
//        //排队
//        IMMsgContentType.RANKING_CONTENT_TYPE,
//        // faq(原协议8)
//        IMMsgContentType.FAQ_CONTENT_TYPE,
//        // 知识点(原协议9)
//        IMMsgContentType.KNOWLEDGE_POINT_CONTENT_TYPE,
//        // 答案(原协议10)
//        IMMsgContentType.KNOWLEDGE_ANSWER_CONTENT_TYPE,
//        // 转接
//        IMMsgContentType.TRANSFER_CONTENT_TYPE,
//        // 系统文本消息(欢迎语、引导语等系统发送的文本消息,原协议12)
//        IMMsgContentType.SYSTEM_CONTENT_TYPE,
//        // 无客服在线
//        IMMsgContentType.UNSUPPORTED_CONTENT_TYPE,
//    ]
//)

//annotation class IMMsgContentType {
//    companion object {
//
//        const val INVALID_CONTENT_TYPE = IMMsg.ContentType.InvalidContentType_VALUE               //
//        const val TEXT_CONTENT_TYPE = IMMsg.ContentType.TextContentType_VALUE                  // 文本
//        const val IMAGE_CONTENT_TYPE = IMMsg.ContentType.ImgContentType_VALUE                   // 图片
//        const val VIDEO_CONTENT_TYPE = IMMsg.ContentType.VideoContentType_VALUE                 // 视频
//        const val CREATE_SESSION_CONTENT_TYPE =
//            IMMsg.ContentType.CreateSessionContentType_VALUE         // C端接入(只推送给B端)
//        const val AGENT_USER_JOIN_SESSION_CONTENT_TYPE =
//            IMMsg.ContentType.AgentUserJoinSessionContentType_VALUE  // 客服接入(只推送给C端)
//        const val END_SESSION_CONTENT_TYPE =
//            IMMsg.ContentType.EndSessionContentType_VALUE            // 会话结束(只推送给C端)
//        const val  RANKING_CONTENT_TYPE =
//            IMMsg.ContentType.RankingContentType_VALUE               // 排队
//        const val FAQ_CONTENT_TYPE =
//            IMMsg.ContentType.FaqContentType_VALUE                   // faq(原协议8)
//        const val KNOWLEDGE_POINT_CONTENT_TYPE =
//            IMMsg.ContentType.KnowledgePointContentType_VALUE        // 知识点(原协议9)
//        const val KNOWLEDGE_ANSWER_CONTENT_TYPE =
//            IMMsg.ContentType.KnowledgeAnswerContentType_VALUE       // 答案(原协议10)
//        const val TRANSFER_CONTENT_TYPE =
//            IMMsg.ContentType.TransferContentType_VALUE              // 转接
//        const val SYSTEM_CONTENT_TYPE =
//            IMMsg.ContentType.SystemContentType_VALUE                // 系统文本消息(欢迎语、引导语等系统发送的文本消息,原协议12)
//        const val UNSUPPORTED_CONTENT_TYPE =
//            IMMsg.ContentType.UnsupportedContentType_VALUE           // 无客服在线
//        const val  ANWSER_TIMEOUT_CONTENT_TYPE =
//            IMMsg.ContentType.AnswerMsgTimeoutContentType_VALUE
//    }
//}

typealias IMMsgContentType = IMMsg.ContentType
