package com.lbe.imsdk.repository.db.entry

import androidx.compose.runtime.Composable
import com.lbe.imsdk.provider.LocalSession
import com.lbe.imsdk.repository.db.IMDataBase
import com.lbe.imsdk.repository.local.insert
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
    return msgType == IMMsgContentType.ImgContentType
}

fun IMMessageEntry.isVideoType(): Boolean {
    return msgType == IMMsgContentType.VideoContentType
}