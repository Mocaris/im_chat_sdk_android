package com.lbe.imsdk.manager

import com.lbe.imsdk.extension.withIOContext
import kotlinx.coroutines.withContext


/**
 *
 *
 * @Date 2025-08-18
 */
class ConversationListManager(private val sessionId: String) {
    val imApiRepository get() = LbeIMSDKManager.imApiRepository


    suspend fun getSessionList(sessionIds: List<String>) = withIOContext {
        try {
            val list = imApiRepository?.getSessionList(sessionIds) ?: return@withIOContext

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}