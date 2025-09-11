package com.lbe.imsdk.repository.remote.model.enumeration

import androidx.annotation.IntDef
import com.lbe.imsdk.repository.model.proto.IMMsg

/**
 *
 *  消息体类型
 * @Date 2025-08-19
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    value = [
        IMMsgContentType.InvalidContentType,
        // 文本
        IMMsgContentType.TextContentType,
        // 图片
        IMMsgContentType.ImgContentType,
        // 视频
        IMMsgContentType.VideoContentType,
        //  C端接入(只推送给B端)
        IMMsgContentType.CreateSessionContentType,
        //  客服接入(只推送给C端)
        IMMsgContentType.AgentUserJoinSessionContentType,
        // 会话结束(只推送给C端)
        IMMsgContentType.EndSessionContentType,
        //排队
        IMMsgContentType.RankingContentType,
        // faq(原协议8)
        IMMsgContentType.FaqContentType,
        // 知识点(原协议9)
        IMMsgContentType.KnowledgePointContentType,
        // 答案(原协议10)
        IMMsgContentType.KnowledgeAnswerContentType,
        // 转接
        IMMsgContentType.TransferContentType,
        // 系统文本消息(欢迎语、引导语等系统发送的文本消息,原协议12)
        IMMsgContentType.SystemContentType,
        // 无客服在线
        IMMsgContentType.UnsupportedContentType,
    ]
)
annotation class IMMsgContentType {
    companion object {

        const val InvalidContentType = IMMsg.ContentType.InvalidContentType_VALUE;               //
        const val TextContentType = IMMsg.ContentType.TextContentType_VALUE;                  // 文本
        const val ImgContentType = IMMsg.ContentType.ImgContentType_VALUE;                   // 图片
        const val VideoContentType = IMMsg.ContentType.VideoContentType_VALUE;                 // 视频
        const val CreateSessionContentType =
            IMMsg.ContentType.CreateSessionContentType_VALUE;         // C端接入(只推送给B端)
        const val AgentUserJoinSessionContentType =
            IMMsg.ContentType.AgentUserJoinSessionContentType_VALUE;  // 客服接入(只推送给C端)
        const val EndSessionContentType =
            IMMsg.ContentType.EndSessionContentType_VALUE;            // 会话结束(只推送给C端)
        const val RankingContentType =
            IMMsg.ContentType.RankingContentType_VALUE;               // 排队
        const val FaqContentType =
            IMMsg.ContentType.FaqContentType_VALUE;                   // faq(原协议8)
        const val KnowledgePointContentType =
            IMMsg.ContentType.KnowledgePointContentType_VALUE;        // 知识点(原协议9)
        const val KnowledgeAnswerContentType =
            IMMsg.ContentType.KnowledgeAnswerContentType_VALUE;       // 答案(原协议10)
        const val TransferContentType =
            IMMsg.ContentType.TransferContentType_VALUE;              // 转接
        const val SystemContentType =
            IMMsg.ContentType.SystemContentType_VALUE;                // 系统文本消息(欢迎语、引导语等系统发送的文本消息,原协议12)
        const val UnsupportedContentType =
            IMMsg.ContentType.UnsupportedContentType_VALUE;           // 无客服在线
    }
}
