package com.lbe.imsdk.manager

import com.lbe.imsdk.extension.withIOContext
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.local.LbeImDataRepository
import com.lbe.imsdk.repository.remote.api.params.SendMessageBody
import kotlin.jvm.Throws

/**
 *
 * @Author mocaris
 * @Date 2026-01-26
 * @Since
 */
class ConversationMsgManager(private val sessionId: String) {
    val imApiRepository get() = LbeIMSDKManager.imApiRepository

    suspend fun markRead(seq: Long) {
        imApiRepository?.markRead(seq, sessionId)
    }

    /**
     * 发送消息
     */
    suspend fun sendMessage(message: SendMessageBody): Long? {
        return imApiRepository?.sendMsg(message)?.data?.msgReq
    }

    /**
     * 获取本地最新消息
     */
//    suspend fun loadLocalNewest(
//        count: Int,
//        sessionId: String = this.sessionId
//    ): List<IMMessageEntry> =
//        withIOContext {
//            return@withIOContext LbeImDataRepository.findLastestSessionMsgList(sessionId, count)
//        }

    /**
     * 获取断网后本地 丢失的消息
     */
    suspend fun loadLocalLostMsgList(sessionId: String=this.sessionId): List<IMMessageEntry> = withIOContext {
        val maxSeq = LbeImDataRepository.findMaxSeq(sessionId)?.coerceAtLeast(1) ?: return@withIOContext emptyList()
        val rMaxSeq = getRemoteLastestSeq(sessionId)
        if (rMaxSeq <= maxSeq) {
            return@withIOContext emptyList()
        }
        loadAndCacheRemoteMsgList(maxSeq + 1, rMaxSeq, sessionId)
        return@withIOContext LbeImDataRepository.findSessionMsgList(sessionId, maxSeq + 1, rMaxSeq)
    }


    /**
     * 获取最新消息
     */
    suspend fun loadNewest(
        count: Int,
        sessionId: String = this.sessionId
    ) = withIOContext {
        try {
            var maxSeq = LbeImDataRepository.findMaxSeq(sessionId)
            if (null == maxSeq) {
                //获取服务器最新
                maxSeq = getRemoteLastestSeq(sessionId)
                loadAndCacheRemoteMsgList(maxSeq - count, maxSeq, sessionId)
            } else {
                val minSeq = LbeImDataRepository.findMinSeq(sessionId)
                val startSeq = (maxSeq - count).coerceAtLeast(1)
                if (null != minSeq && minSeq > startSeq) {
                    loadAndCacheRemoteMsgList(startSeq, minSeq - 1, sessionId)
                }
                val rMaxSeq = getRemoteLastestSeq(sessionId)
                if (rMaxSeq > maxSeq) {
                    loadAndCacheRemoteMsgList(maxSeq + 1, rMaxSeq, sessionId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withIOContext LbeImDataRepository.findLastestSessionMsgList(sessionId, count)
    }


    /**
     * 获取消息列表
     * 本地没有的话则从服务器拉取
     */
    @Throws(Exception::class)
    suspend fun loadMessageList(
        startSeq: Long,
        endSeq: Long,
        sessionId: String = this.sessionId
    ): List<IMMessageEntry> = withIOContext {
        val startSeq = startSeq.coerceAtLeast(1)
        val endSeq = endSeq.coerceAtLeast(1)
        val minSeq = LbeImDataRepository.findMinSeq(sessionId)
        val maxSeq = LbeImDataRepository.findMaxSeq(sessionId)
        //本地 没有 消息
        if (null == minSeq || null == maxSeq) {
            loadAndCacheRemoteMsgList(startSeq, endSeq, sessionId)
        } else {
            //从服务器拉取 差额
            if (minSeq > startSeq) {
                loadAndCacheRemoteMsgList(startSeq, minSeq - 1, sessionId)
            }
            //从服务器拉取 差额
            if (maxSeq < endSeq) {
                loadAndCacheRemoteMsgList(maxSeq + 1, endSeq, sessionId)
            }
        }
        return@withIOContext LbeImDataRepository.findSessionMsgList(sessionId, startSeq, endSeq)
    }


    /**
     * 分页获取远程消息列表
     */
    private suspend fun loadAndCacheRemoteMsgList(
        startSeq: Long,
        endSeq: Long,
        sessionId: String = this.sessionId
    ) = withIOContext {
        val startSeq = startSeq.coerceAtLeast(1)
        val endSeq = endSeq.coerceAtLeast(1)
        if (startSeq >= endSeq) {
            return@withIOContext
        }
        val newCount = endSeq - startSeq.coerceAtLeast(1)
        if (newCount > 0) {
            /// 每页 50 条
            val pageSize = (newCount / 50).toInt() + 1
            for (page in 0.until(pageSize)) {
                val start = startSeq + 1 + page * 50
                val end = (start + 50).coerceAtMost(endSeq)
                val list = getRemoteMsgList(start, end, sessionId)
                if (list.isEmpty()) {
                    continue
                }
                LbeImDataRepository.insertMsgList(list)
            }
        }
    }


    /**
     * 拉取服务器 最新消息并保存到本地
     * @param startSeq 本地最新消息序号
     * @param endSeq 服务器最新消息序号
     */
//    private suspend fun loadRemoteNewest(
//        startSeq: Long? = null,
//        endSeq: Long? = null,
//        sessionId: String = this.sessionId
//    ) =
//        withIOContext {
//            try {
//                val startSeq =
//                    (startSeq ?: LbeImDataRepository.findMaxSeq(sessionId) ?: 1).coerceAtLeast(1)
//                val endSeq = (endSeq ?: getRemoteLastestSeq(sessionId)).coerceAtLeast(1)
//
//                if (startSeq >= endSeq) {
//                    return@withIOContext
//                }
//                val newCount = endSeq - startSeq.coerceAtLeast(1)
//                if (newCount > 0) {
//                    /// 每页 50 条
//                    val pageSize = (newCount / 50).toInt() + 1
//                    for (page in 0.until(pageSize)) {
//                        val start = startSeq + 1 + page * 50
//                        val end = (start + 50).coerceAtMost(endSeq)
//                        val list = getRemoteMsgList(start, end, sessionId)
//                        if (list.isEmpty()) {
//                            continue
//                        }
//                        LbeImDataRepository.insertMsgList(list)
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//    suspend fun localMsgList(
//        startSeq: Long,
//        endSeq: Long,
//        sessionId: String = this.sessionId
//    ): List<IMMessageEntry> = withIOContext {
//        return@withIOContext LbeImDataRepository.findSessionMsgList(
//            sessionId,
//            startSeq.coerceAtLeast(1),
//            endSeq.coerceAtLeast(1)
//        )
//    }

    /**
     * 获取历史消息
     * 本地不存在则从服务器拉取 并保存至本地
     * @param endSeq
     * @param count
     */
//    suspend fun loadHistory(
//        startSeq: Long,
//        endSeq: Long,
//        sessionId: String = this.sessionId
//    ): List<IMMessageEntry> =
//        withIOContext {
//            try {
//                val startSeq = (startSeq).coerceAtLeast(1)
//                val endSeq = endSeq.coerceAtLeast(1)
//                if (startSeq >= endSeq) {
//                    return@withIOContext emptyList()
//                }
//                val minSeq = LbeImDataRepository.findMinSeq(sessionId)
//                val maxSeq = LbeImDataRepository.findMinSeq(sessionId)
//                var list: List<IMMessageEntry>
//                if (null == minSeq || null == maxSeq) {
//                    list = getRemoteMsgList(startSeq, endSeq, sessionId)
//                } else {
//                    list = LbeImDataRepository.findSessionMsgList(
//                        sessionId,
//                        startSeq,
//                        endSeq
//                    )
//                    if (list.isEmpty()) {
//                        // 获取服务器消息
//                        list = getRemoteMsgList(startSeq, endSeq, sessionId)
//                    }
//                }
//                return@withIOContext list
//            } catch (e: Exception) {
//                return@withIOContext emptyList()
//            }
//        }

    /**
     * 获取远程最新消息序号
     */
    private suspend fun getRemoteLastestSeq(sessionId: String = this.sessionId): Long =
        withIOContext {
            //获取当前会话最新消息
            val lastMessage =
                imApiRepository?.getSessionList(listOf(sessionId))?.sessionList?.firstOrNull()
            return@withIOContext if (null != lastMessage) {
                lastMessage.latestMsg?.msgSeq ?: 50
            } else {
                //先查询 消息 数量
                imApiRepository?.getSessionMessage(sessionId, 0, 0)?.total ?: 50
            }
        }

    /**
     * 获取服务器消息
     */
    private suspend fun getRemoteMsgList(
        startSeq: Long,
        endSeq: Long,
        sessionId: String = this.sessionId
    ): List<IMMessageEntry> =
        withIOContext {
            val startSeq = startSeq.coerceAtLeast(1)
            val endSeq = endSeq.coerceAtLeast(1)
            if (startSeq >= endSeq) {
                return@withIOContext emptyList()
            }
            return@withIOContext imApiRepository?.getSessionMessage(
                sessionId,
                startSeq,
                endSeq
            )?.content?.map { IMMessageEntry.fromIMMsgModel(it) } ?: emptyList()
        }
}