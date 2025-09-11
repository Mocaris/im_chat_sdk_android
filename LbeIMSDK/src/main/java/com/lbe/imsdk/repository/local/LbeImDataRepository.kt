package com.lbe.imsdk.repository.local

import com.lbe.imsdk.extension.*
import com.lbe.imsdk.repository.db.*
import com.lbe.imsdk.repository.db.entry.IMMessageEntry

/**
 * 本地数据仓库
 * @Author mocaris
 * @Date 2025-08-19
 */
object LbeImDataRepository {

    private val msgDao get() = IMDataBase.get().imMsgDao()

    /**
     * 获取会话列表
     */
    suspend fun getSessionList(sessionId: String) = withIOContext {

    }

    suspend fun insertMsg(msg: IMMessageEntry) = withIOContext {
        msgDao.insert(msg)
    }

    suspend fun upsertMsg(msg: IMMessageEntry) = withIOContext {
        msgDao.upsert(msg)
    }

    suspend fun findMaxSeq(sessionId: String): Long? = withIOContext {
        return@withIOContext msgDao.findMaxSeq(sessionId)
    }

    suspend fun insertMsgList(list: List<IMMessageEntry>) = withIOContext {
        return@withIOContext msgDao.insert(list)
    }

    /**
     * 获取会话消息列表
     */
    suspend fun findSessionMsgList(sessionId: String, startSeq: Long, endSeq: Long) = withIOContext {
        return@withIOContext msgDao.findBySeq(sessionId, startSeq.coerceAtLeast(0), endSeq)
    }

    suspend fun findLastestSessionMsgList(sessionId: String, count: Int) = withIOContext {
        return@withIOContext msgDao.findLastest(sessionId, count)
    }

    suspend fun findByClientMsgId(clientMsgId: String) = withIOContext {
        return@withIOContext msgDao.findByClientMsgId(clientMsgId)
    }

    suspend fun findByMsgSeq(sessionId: String,seq:Long) = withIOContext {
        return@withIOContext msgDao.findByMsgSeq(sessionId,seq)
    }

    suspend fun updateReadStatus(sessionId: String, msgSeq: List<Long>): Int= withIOContext {
        return@withIOContext msgDao.updateReadStatus(sessionId,msgSeq)
    }

}

suspend fun IMMessageEntry.insert(): Long {
   return LbeImDataRepository.insertMsg(this)
}

suspend fun IMMessageEntry.upsert(): Long {
    return LbeImDataRepository.upsertMsg(this)
}
