package com.lbe.imsdk.repository.remote.model.enumeration

import androidx.annotation.IntDef
import kotlin.intArrayOf

/**
 * 消息状态
 * @Date 2025-08-19
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [IMMsgReadStatus.UNREAD, IMMsgReadStatus.READ])
annotation class IMMsgReadStatus {
    companion object {
        /**
         *  0-未读，
         */
        const val UNREAD = 0

        /**
         *  1-已读
         */
        const val READ = 1
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [IMMsgSendStatus.SUCCESS, IMMsgSendStatus.SENDING, IMMsgSendStatus.FAILURE])
annotation class IMMsgSendStatus {
    companion object {
        /**
         *  0-成功
         */
        const val SUCCESS = 0

        /**
         *  1-发送中
         */
        const val SENDING = 1

        /**
         *  1-失败
         */
        const val FAILURE = 2
    }
}
