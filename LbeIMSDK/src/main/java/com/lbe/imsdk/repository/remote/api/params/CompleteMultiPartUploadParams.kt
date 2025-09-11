package com.lbe.imsdk.repository.remote.api.params

import kotlinx.serialization.Serializable


@Serializable
data class CompleteUploadParams(
    val uploadId: String,
    val name: String,
    val part: List<BlockPart>,
) {
}

@Serializable
data class BlockPart(
    val partNumber: Int,
    val etag: String,
)
