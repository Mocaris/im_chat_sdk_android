package com.lbe.imsdk.manager

import com.lbe.imsdk.extension.withIOContext
import com.lbe.imsdk.repository.db.entry.IMMessageEntry
import com.lbe.imsdk.repository.local.LbeImDataRepository
import com.lbe.imsdk.repository.remote.api.params.SendMessageBody

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
    suspend fun loadLocalNewest(count: Int): List<IMMessageEntry> =
        withIOContext {
            return@withIOContext LbeImDataRepository.findLastestSessionMsgList(sessionId, count)
        }

    /**
     * 拉取服务器 最新消息并保存到本地
     * @param startSeq 本地最新消息序号
     * @param endSeq 服务器最新消息序号
     */
    suspend fun loadRemoteNewest(startSeq: Long? = null, endSeq: Long? = null) =
        withIOContext {
            try {
                val startSeq = startSeq ?: LbeImDataRepository.findMaxSeq(sessionId) ?: 0
                val endSeq = endSeq ?: getRemoteLastestSeq()
                if (startSeq >= endSeq) {
                    return@withIOContext
                }
                val newCount = endSeq - startSeq.coerceAtLeast(0)
                if (newCount > 0) {
                    /// 每页 50 条
                    val pageSize = (newCount / 50).toInt() + 1
                    for (page in 0.until(pageSize)) {
                        val start = startSeq + 1 + page * 50
                        val end = (start + 50).coerceAtMost(endSeq)
                        val list = getRemoteMsgList(start, end)
                        if (list.isEmpty()) {
                            continue
                        }
                        LbeImDataRepository.insertMsgList(list)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    suspend fun localMsgList(
        startSeq: Long,
        endSeq: Long,
    ): List<IMMessageEntry> = withIOContext {
        return@withIOContext LbeImDataRepository.findSessionMsgList(sessionId, startSeq, endSeq)
    }

    /**
     * 获取历史消息
     * 本地不存在则从服务器拉取 并保存至本地
     * @param endSeq
     * @param count
     */
    suspend fun loadHistory(endSeq: Long, count: Int): List<IMMessageEntry> =
        withIOContext {
            try {
                val startSeq = (endSeq - count).coerceAtLeast(1)
                var list = LbeImDataRepository.findSessionMsgList(
                    sessionId,
                    startSeq,
                    endSeq
                )
                if (list.isNotEmpty()) {
                    return@withIOContext list
                }
                // 获取服务器消息
                list = getRemoteMsgList(startSeq, endSeq)
                if (list.isNotEmpty()) {
                    LbeImDataRepository.insertMsgList(list)
                }
                return@withIOContext LbeImDataRepository.findSessionMsgList(
                    sessionId,
                    startSeq,
                    endSeq
                )
            } catch (e: Exception) {
                return@withIOContext emptyList()
            }
        }

    /**
     * 获取远程最新消息序号
     */
    suspend fun getRemoteLastestSeq(): Long = withIOContext {
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
    ): List<IMMessageEntry> =
        withIOContext {
            return@withIOContext imApiRepository?.getSessionMessage(
                sessionId,
                startSeq.coerceAtLeast(0),
                endSeq
            )?.content?.map { IMMessageEntry.fromIMMsgModel(it) } ?: emptyList()
        }
}