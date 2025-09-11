package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.service.http.model.NetResponseData
import kotlinx.serialization.Serializable

/**
 *
 * @Author mocaris
 * @Date 2025-08-21
 */
@Serializable
data class SendMsgResModel(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    override val data: SendData? = null,
) : NetResponseData<SendMsgResModel.SendData>() {

    @Serializable
    data class SendData(
        val msgReq: Long
    )
}