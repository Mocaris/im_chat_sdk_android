package com.lbe.imsdk.repository.db.dao

import androidx.room.*
import com.lbe.imsdk.repository.db.entry.*
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgReadStatus

/**
 * 会话 消息 Dao
 * @Date 2025-08-19
 */
@Dao
interface IMMsgDao {

    @Transaction
    @Insert(entity = IMMessageEntry::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: IMMessageEntry): Long

    @Transaction
    @Upsert(entity = IMMessageEntry::class)
    suspend fun upsert(model: IMMessageEntry): Long

    @Transaction
    @Insert(entity = IMMessageEntry::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<IMMessageEntry>): List<Long>


    /**
     * 查询本地 最新消息 seq
     */
    @Query("SELECT MAX(msg_seq) FROM tb_msg WHERE session_id = :sessionId")
    suspend fun findMaxSeq(sessionId: String): Long?

    /**
     * 查询最新的 count 条 消息
     * 时间正序排序，取最后 count 条
     *
     * 根据 sentime
     */
    @Query("SELECT * FROM (SELECT * FROM  tb_msg WHERE session_id = :sessionId AND (msg_type != ${IMMsgContentType.InvalidContentType}) ORDER BY send_time DESC LIMIT :count) ORDER BY send_time ASC")
    suspend fun findLastest(sessionId: String, count: Int): List<IMMessageEntry>

    /**
     * 根据 seq 查询历史消息
     * 正序
     * @param startSeq 起始 seq 包括
     * @param endSeq 结束 seq 包括
     */
    @Query("SELECT * FROM (SELECT * FROM  tb_msg WHERE session_id = :sessionId AND msg_seq <= :endSeq AND msg_seq >= :startSeq AND (client_msg_id !='' OR server_msg_id !='') ORDER BY send_time DESC ) ORDER BY send_time ASC")
    suspend fun findBySeq(sessionId: String, startSeq: Long, endSeq: Long): List<IMMessageEntry>

    /**
     * 根据 seq 查询历史消息
     * 正序
     */
    @Query("SELECT * FROM  tb_msg WHERE session_id in (:sessionIds) AND (client_msg_id !='' OR server_msg_id !='') ORDER BY send_time ASC ")
    suspend fun findBySessionIds(sessionIds: List<String>): List<IMMessageEntry>


    @Query("SELECT * FROM tb_msg WHERE client_msg_id = :clientMsgId ORDER BY send_time, msg_seq ASC")
    suspend fun findByClientMsgId(clientMsgId: String): List<IMMessageEntry>


    /**
     * 根据 seq 查询消息
     */
    @Query("SELECT * FROM tb_msg WHERE session_id = :sessionId AND msg_seq = :msgSeq")
    suspend fun findByMsgSeq(sessionId: String, msgSeq: Long): IMMessageEntry?

    /**
     * 更新消息已读状态
     */
    @Query("UPDATE tb_msg SET status = ${IMMsgReadStatus.READ} WHERE session_id = :sessionId AND msg_seq IN (:msgSeq)")
    suspend fun updateReadStatus(sessionId: String, msgSeq: List<Long>): Int

}