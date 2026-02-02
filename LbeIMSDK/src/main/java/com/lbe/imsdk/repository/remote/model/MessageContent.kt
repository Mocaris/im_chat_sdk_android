package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * 消息体
 * @Date 2025-08-29
 */
interface MessageContent {
}

@Serializable
data class SourceUrl(
    val key: String,
    val url: String,
) : java.io.Serializable
/**
 * 文本消息
 */
typealias TextMessageBody = String


/**
type 5 客服接入 [IMMsgContentType.AGENT_USER_CONTENT_TYPE]
 */
@Serializable
data class AgentUser(
    val username: String,
    val faceUrl: String,
    val joinTime: Long,
) : MessageContent {
    companion object {
        fun fromJson(json: String): AgentUser {
            return Json.decodeFromString<AgentUser>(json)
        }
    }

    val parseFaceUrl = try {
        Json.decodeFromString<SourceUrl>(faceUrl)
    } catch (e: Exception) {
        null
    }
}


/**
 *媒体消息 (图片/视频)
 * type 2/3 [IMMsgContentType.IMAGE_CONTENT_TYPE],[IMMsgContentType.VIDEO_CONTENT_TYPE]
 *
 * {
 *   "width" : 516,
 *   "height" : 1120,
 *   "thumbnail" : {
 *     "key" : "v1:lbe5b6b5a7ca9fc5ccfcdec0041d4",
 *     "url" : "https://oss.imsz.online/private/openimttt/lbe_bb202061d86ef16fba8583011d8edc.jpg"
 *   },
 *   "resource" : {
 *     "key" : "v1:lbedd8204c9acbe3cd09eb6d480ab",
 *     "url" : "https://oss.imsz.online/private/openimttt/lbe_a4c97b4c522ddd00f3ad4c7289106e.jpg"
 *   }
 */
@Serializable
data class MediaMessageContent(
    val width: Int,
    val height: Int,
    var thumbnail: SourceUrl,
    var resource: SourceUrl,
) : MessageContent {
    companion object {

        fun fromJson(json: String): MediaMessageContent {
            return Json.decodeFromString<MediaMessageContent>(json)
        }
    }
}

/**
 * 排队消息
 * type 7 [IMMsgContentType.RANKING_CONTENT_TYPE]
 */
@Serializable
data class RankingMessageContent(
    val number: Int,
) : MessageContent {
    companion object {
        fun fromJson(json: String): RankingMessageContent {
            return Json.decodeFromString<RankingMessageContent>(json)
        }
    }
}

/**
 * 问答 faq 消息
 * type 8 [IMMsgContentType.FAQ_CONTENT_TYPE]
 * {"knowledgeBaseTitle":"猜你想问，点下面问题试试吧","knowledgeBaseList":[{"id":"678f4b02e0c9b0740748f247","language":"zh","knowledgeBaseName":"售前问题","url":"{\"key\":\"v1:lbee7f7c77f73cdad31c215abe8db\",\"url\":\"https://oss.imsz.online/private/openimttt/lbe_255f189300869cdc33b126dbbeda06.png\"}"},{"id":"678f4b29e0c9b0740748f249","language":"zh","knowledgeBaseName":"售后问题","url":"{\"key\":\"v1:lbe0715108408f30f5c8d5294c57c\",\"url\":\"https://oss.imsz.online/private/openimttt/lbe_51bfbff4c7d4525fc16e8330d32c0b.png\"}"},{"id":"678f4b3ee0c9b0740748f24a","language":"zh","knowledgeBaseName":"知识库","url":"{\"key\":\"v1:lbe65bb0b7302e09de072dda3fa4c\",\"url\":\"https://oss.imsz.online/private/openimttt/lbe_f8a5bd76817b5e546dccf9498e8a9d.png\"}"}]}
 */
@Serializable
data class FaqMessageContent(
    val knowledgeBaseTitle: String,
    val knowledgeBaseList: List<KnowledgeList>,
) : MessageContent {
    companion object {
        fun fromJson(json: String): FaqMessageContent {
            return Json.decodeFromString<FaqMessageContent>(json)
        }
    }

    @Serializable
    data class KnowledgeList(
        val id: String,
        val language: String,
        val knowledgeBaseName: String,
        val url: String
    ) {

        @Transient
        val parseUrl = try {
            Json.decodeFromString<SourceUrl>(url)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * faq答案消息
 * type 10 [IMMsgContentType.KNOWLEDGE_ANSWER_CONTENT_TYPE]
 * [{"type":0,"content":"我们的系统支持PC端和移动端，兼容Windows、MacOS、iOS、Android等操作系统，确保各类设备都能顺利使用。","contents":""}]
 */
@Serializable
data class FaqAnswerMessageContent(
    /*
    0 文本 String
    1 图片 SourceUrl
    2 链接 2 LinkText
     */
    val type: Int,
    val content: String,
    /// type=2
    val contents: String
) : MessageContent {
    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_IMAGE = 1
        const val TYPE_LINK = 2
        fun fromJson(json: String): List<FaqAnswerMessageContent> {
            return Json.decodeFromString<List<FaqAnswerMessageContent>>(json)
        }
    }

    val imageUrl by lazy {
        try {
            Json.decodeFromString<SourceUrl>(content)
        } catch (e: Exception) {
            null
        }
    }

    val linkList by lazy {
        try {
            Json.decodeFromString<List<LinkText>>(contents)
        } catch (e: Exception) {
            null
        }
    }

    @Serializable
    data class LinkText(
        val content: String,
        val url: String,
    )
}

/**
 * 知识点消息 list
 * type 9 [IMMsgContentType.KNOWLEDGE_POINT_CONTENT_TYPE]
 * [{"id":"678f4c9fe0c9b0740748f24b","knowledgePointName":"产品介绍"},{"id":"678f4ccde0c9b0740748f24c","knowledgePointName":"产品功能"},{"id":"678f4d4de0c9b0740748f256","knowledgePointName":"你们的系统是否稳定？"},{"id":"678f4d4de0c9b0740748f257","knowledgePointName":"你们的系统支持哪些设备或操作系统？"},{"id":"678f4d4de0c9b0740748f24f","knowledgePointName":"适合哪些行业使用你们的客服系统？"},{"id":"678f4d4de0c9b0740748f250","knowledgePointName":"你们的系统能与其他平台集成吗？"},{"id":"678f4d4de0c9b0740748f252","knowledgePointName":"是否可以根据我们的需求定制功能？"},{"id":"678f4d4de0c9b0740748f24d","knowledgePointName":"你们的客服系统是什么？"},{"id":"678f4d4de0c9b0740748f24e","knowledgePointName":"你们的系统有哪些主要功能？"},{"id":"678f4d4de0c9b0740748f254","knowledgePointName":"你们的系统收费标准是什么？"},{"id":"678f4d4de0c9b0740748f251","knowledgePointName":"你们的系统安全吗？"},{"id":"678f4d4de0c9b0740748f253","knowledgePointName":"能不能试用你们的客服系统？"},{"id":"678f4d4de0c9b0740748f255","knowledgePointName":"你们的客服系统提供什么支持？"}]
 */
@Serializable
data class KnowledgePointMessageContent(
    val id: String,
    val knowledgePointName: String
) : MessageContent {
    companion object {
        fun fromJson(json: String): List<KnowledgePointMessageContent> {
            return Json.decodeFromString<List<KnowledgePointMessageContent>>(json)
        }
    }

}

/**
 * 超时未回复
 * type 14 [IMMsgContentType.ANWSER_TIMEOUT_CONTENT_TYPE]
 */
@Serializable
data class AnswerTimeoutMessageContent(
    /// 超时时间 分
    val timeout: Int,
) : MessageContent {
    companion object {
        fun fromJson(json: String): AnswerTimeoutMessageContent {
            return Json.decodeFromString<AnswerTimeoutMessageContent>(json)
        }
    }

//    fun getTimeoutMinutesText(): String {
//        if (timeout <= 60) {
//            return "1"
//        }
//        return "%.1f".format(timeout / 60f)
//    }

}
