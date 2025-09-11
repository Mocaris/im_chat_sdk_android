package com.lbe.imsdk.repository.remote.api.params


data class InitMultiPartUploadParams(
    val size: Long,
    val name: String,
    val contentType: String,
)
