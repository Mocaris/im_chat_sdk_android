package com.lbe.imsdk.repository.remote.model

import com.lbe.imsdk.repository.remote.model.CompleteUploadResData.MergeData
import com.lbe.imsdk.service.http.model.NetResponseData
import kotlinx.serialization.*

/**
 *
 *
 * @Date 2025-07-16
 */


/*单个文件上传*/
@Serializable
data class UploadSingleResData(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    @SerialName("data") override val data: UpSingleData? = null
) : NetResponseData<UploadSingleResData.UpSingleData>() {

    @Serializable
    data class UpSingleData(
        val paths: List<FileData> = emptyList(),
    )

    @Serializable
    data class FileData(
        val url: String,
        val key: String,
    )

}

/*大文件上传*/
@Serializable
data class UploadBigFileResData(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    @SerialName("data") override val data: FileNodeData? = null
) : NetResponseData<UploadBigFileResData.FileNodeData>() {

    @Serializable
    data class FileNodeData(
        val uploadId: String,
        val node: List<UploadNode> = emptyList(),
    )
}

@Serializable
data class UploadNode(
    val url: String,
    val header: Header? = null,
    val size: Long,
) {

    @Serializable
    data class Header(
        @SerialName("Authorization") val authorization: List<String>,
        @SerialName("X-Amz-Content-Sha256") val xAmzContentSha256: List<String>,
        @SerialName("X-Amz-Date") val xAmzDate: List<String>,
    )

}

/*上传合并完成*/
@Serializable
data class CompleteUploadResData(
    override val code: Int,
    override val msg: String,
    override val dlt: String,
    @SerialName("data") override val data: MergeData? = null,
) : NetResponseData<MergeData>() {

    @Serializable
    data class MergeData(
        val location: String,
        val etag: String,
    )
}
