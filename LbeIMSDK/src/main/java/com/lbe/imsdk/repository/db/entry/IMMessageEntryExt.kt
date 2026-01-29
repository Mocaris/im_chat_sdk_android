package com.lbe.imsdk.repository.db.entry

import androidx.compose.runtime.Composable
import com.lbe.imsdk.provider.LocalSession
import com.lbe.imsdk.repository.remote.model.enumeration.IMMsgContentType

/**
 *
 * @Date 2025-08-21
 */

@Composable
fun IMMessageEntry.isSelfSender(): Boolean {
    return LocalSession.current.uid == this.senderUid
}

fun IMMessageEntry.isImageType(): Boolean {
    return msgType == IMMsgContentType.IMAGE_CONTENT_TYPE
}

fun IMMessageEntry.isVideoType(): Boolean {
    return msgType == IMMsgContentType.VIDEO_CONTENT_TYPE
}