package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.service.http.model.NetResponseData
import kotlinx.serialization.Serializable

/**
 * 超时配置
 * @Date 2025-09-04
 */
@Serializable
data class TimeOutConfigModel(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    override val data: TimeOutConfigData? = null,
) : NetResponseData<TimeOutConfigModel.TimeOutConfigData>() {


    @Serializable
    data class TimeOutConfigData(
        val isOpen: Boolean,
        //分钟
        val timeout: Int,
    ) {
    }
}