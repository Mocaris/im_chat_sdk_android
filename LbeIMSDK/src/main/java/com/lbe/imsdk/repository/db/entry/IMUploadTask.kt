package com.lbe.imsdk.repository.db.entry

import com.lbe.imsdk.repository.remote.model.UploadNode
import kotlinx.serialization.Serializable

/**
 *
 * @Date 2025-09-10
 */
@Serializable
data class IMUploadTask(
    var width: Int,
    var height: Int,

    // 缩略图 路径
    var thumbnail: String,

    /** 上传的文件本地路径 */
    var filePath: String,

    var uploadId: String = "",

    var nodes: List<UploadNode> = emptyList(),

    var uploadChunkMD5: Map<Int, String> = emptyMap(),
) {

}