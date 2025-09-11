package com.lbe.imsdk.service.http.extension

import com.lbe.imsdk.*
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.repository.remote.model.TimeOutConfigModel
import com.lbe.imsdk.service.http.*
import com.lbe.imsdk.service.http.model.*
import retrofit2.*

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
