package com.lbe.imsdk.repository.remote

import com.lbe.imsdk.repository.remote.api.*
import com.lbe.imsdk.repository.remote.api.params.*
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.UploadSignType
import com.lbe.imsdk.service.http.*
import com.lbe.imsdk.service.http.body.*
import com.lbe.imsdk.service.http.extension.accept
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*

//
///**
// *
// *
// * @Date 2025-07-16
// */
class LbeOssApiRepository(private val ossBaseUrl: String) {
    val apiService: LbeOssApiService by lazy {
        HttpClient.retrofitFactory(ossBaseUrl)
            .create(LbeOssApiService::class.java)
    }

    suspend fun singleUpload(
        signType: UploadSignType,
        file: File,
        fileName: String,
        listener: ProgressListener? = null,
    ) = withContext(Dispatchers.IO) {
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = fileName,
            body = ProgressBody(body = file.asRequestBody(), listener = listener)
        )
        return@withContext apiService.singleUpload(
            signType = signType.value,
            file = part
        ).accept()!!
    }

    suspend fun initMultiPartUpload(
        size: Long,
        name: String,
        contentType: String = "",
    ): UploadBigFileResData.FileNodeData {
        return apiService.initMultiPartUpload(
            body = mapOf(
                "size" to size,
                "name" to name,
                "contentType" to contentType
            ).asJsonBody
        ).accept()!!
    }

    suspend fun uploadBinary(
        url: String,
        header: UploadNode.Header?,
        requestBody: RequestBody
    ) {
        apiService.uploadBinary(
            url = url,
            headerMap = header?.let {
                hashMapOf(
                    "Authorization" to (it.authorization.firstOrNull() ?: ""),
                    "X-Amz-Content-Sha256" to (it.xAmzContentSha256.firstOrNull() ?: ""),
                    "X-Amz-Date" to (it.xAmzDate.firstOrNull() ?: "")
                )
            } ?: emptyMap(),
            requestBody = requestBody
        )
    }

    suspend fun completeMultiPartUpload(
        uploadId: String,
        name: String,
        part: List<BlockPart>,
    ): CompleteUploadResData.MergeData? {
        return apiService.completeMultiPartUpload(
            body = CompleteUploadParams(
                uploadId = uploadId,
                name = name,
                part = part.sortedBy { it.partNumber }
            )
        ).accept()
    }

}