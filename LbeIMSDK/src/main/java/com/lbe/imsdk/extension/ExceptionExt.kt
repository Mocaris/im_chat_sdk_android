package com.lbe.imsdk.extension

import android.widget.Toast
import com.lbe.imsdk.R
import com.lbe.imsdk.service.http.NetException
import com.lbe.imsdk.service.http.extension.supportMessage
import okio.IOException

/**
 *
 * @Author mocaris
 * @Date 2025-09-05
 */

fun Exception.catchException(
    block: (Pair<Int, String>) -> Unit = {
        it.second.showToast()
    }
) {
    block(
        when (this) {
            is IOException -> {
                Pair(-1, appContext.getString(R.string.chat_session_status_9))
            }

            is NetException -> {
                Pair(this.code, this.supportMessage())
            }

            else -> {
                Pair(-1, appContext.getString(R.string.chat_session_status_9))
            }
        }
    )
}

suspend fun <T> tryCatchCoroutine(
    catch: ((Pair<Int, String>) -> Unit)? = null,
    block: suspend () -> T
): T? {
    try {
        return block()
    } catch (e: Exception) {
        e.printStackTrace()
        if (null != catch) {
            e.catchException(catch)
        } else {
            e.catchException()
        }
    }
    return null
}

fun <T> tryCatch(
    catch: ((Pair<Int, String>) -> Unit)? = null,
    block: () -> T
): T? {
    try {
        return block()
    } catch (e: Exception) {
        e.printStackTrace()
        if (null != catch) {
            e.catchException(catch)
        } else {
            e.catchException()
        }
    }
    return null
}