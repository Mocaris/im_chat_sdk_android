package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.service.http.model.*
import kotlinx.serialization.*

@Serializable
data class CreateSessionResModel(
    override val code: Int,
    override val msg: String = "",
    override val dlt: String = "",
    @SerialName("data")
    override val data: SessionData? = null
) : NetResponseData<CreateSessionResModel.SessionData>() {

    @Serializable
    data class SessionData(
        val nickId: String = "",
        val sessionId: String = "",
        val token: String = "",
        val uid: String = ""
    )
}
