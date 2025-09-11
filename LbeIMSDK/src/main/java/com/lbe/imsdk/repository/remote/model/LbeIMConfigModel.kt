package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.service.http.model.*
import kotlinx.serialization.*

/**
 *
 *
 * @Date 2025-07-16
 */
@Serializable
data class LbeIMConfigModel(
    override val code: Int,
    override val msg: String = "",
    override val dlt: String = "",
    @SerialName("data")
    override val data: HostData? = null,
) : NetResponseData<LbeIMConfigModel.HostData>() {

    @Serializable
    data class HostData(
        val oss: List<String> = emptyList(),
        val ws: List<String> = emptyList(),
        val rest: List<String> = emptyList(),
    ) {
        fun hasConfigEmpty(): Boolean {
            return oss.isEmpty() || ws.isEmpty() || rest.isEmpty()
        }
    }
}
