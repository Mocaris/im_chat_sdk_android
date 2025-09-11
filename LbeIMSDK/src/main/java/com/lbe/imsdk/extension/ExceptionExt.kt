package com.lbe.imsdk.extension

import android.widget.Toast
import com.lbe.imsdk.R
import com.lbe.imsdk.service.http.NetException
import okio.IOException

/**
 *
 * @Author mocaris
 * @Date 2025-09-05
 */

fun Exception.catchException(
    block: (Pair<Int, String>) -> Unit = {
        Toast.makeText(appContext, this.message, Toast.LENGTH_SHORT).show()
    }
) {
    block(
        when (this) {
            is IOException -> {
                Pair(-1, appContext.getString(R.string.chat_session_status_9))
            }

            is NetException -> {
                Pair(this.code, this.message)
            }

            else -> {
                Pair(-1, appContext.getString(R.string.chat_session_status_9))
            }
        }
    )
}