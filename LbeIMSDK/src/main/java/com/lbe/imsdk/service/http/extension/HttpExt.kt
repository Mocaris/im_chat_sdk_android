package com.lbe.imsdk.service.http.extension

import com.lbe.imsdk.R
import com.lbe.imsdk.extension.appContext
import com.lbe.imsdk.service.http.NetException
import com.lbe.imsdk.service.http.model.NetResponseData

/**
 *
 *
 * @Date 2025-07-16
 */

fun <T : NetResponseData<R>, R> T.accept(): R? {
    if (code != 0) {
        throw NetException(this.msg, code)
    }
    return this.data
}

fun NetException.supportMessage(): String {
    return when (code) {
        20006 -> appContext.getString(R.string.chat_session_status_28)
        else -> message
    }
}