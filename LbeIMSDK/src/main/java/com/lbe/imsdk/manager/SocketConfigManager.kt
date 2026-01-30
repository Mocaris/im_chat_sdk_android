package com.lbe.imsdk.manager

/**
 *
 * @Author mocaris
 * @Date 2026-01-30
 * @Since
 */
class SocketConfigManager(private val hostUrl: String) {

    fun newSocketManager(lbeToken: String, sessionId: String) =
        SocketManager(
            hostUrl = hostUrl,
            lbeToken = lbeToken,
            sessionId = sessionId,
        )
}