package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.service.http.model.NetResponseData
import kotlinx.serialization.Serializable

/**
 *
 * @Date 2025-09-04
 */
@Serializable
data class VoidResponse(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    override val data: String? = null,
) : NetResponseData<String>() {


}