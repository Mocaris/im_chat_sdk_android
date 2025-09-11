package com.lbe.imsdk.repository.remote.api

import com.lbe.imsdk.repository.remote.api.params.CompleteUploadParams
import com.lbe.imsdk.repository.remote.model.CompleteUploadResData
import com.lbe.imsdk.repository.remote.model.UploadBigFileResData
import com.lbe.imsdk.repository.remote.model.UploadSingleResData
import com.lbe.imsdk.repository.remote.model.VoidResponse
import com.lbe.imsdk.service.http.body.JsonBody
import okhttp3.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Url

/**
 *
 *
 * @Date 2025-07-16
 */
interface LbeOssApiService {

    /**
     * 单个文件上传
     */
    @POST("/api/single/fileupload")
    @Multipart
    suspend fun singleUpload(
        @Part file: MultipartBody.Part,
        @Part("sign_type") signType: Int,
    ): UploadSingleResData

    /**
     * 初始化大文件上传
     */
    @POST("/api/multi/initiate-multipart_upload")
    suspend fun initMultiPartUpload(
        @Body body: JsonBody
    ): UploadBigFileResData

    /**
     * 大文件 部分 上传
     */
    @PUT
    suspend fun uploadBinary(
        @Url url: String,
        @HeaderMap headerMap: Map<String, String>,
        @Body requestBody: RequestBody
    )

    /**
     * 大文件上传 完成
     */
    @POST("/api/multi/complete-multipart-upload")
    suspend fun completeMultiPartUpload(
        @Body body: CompleteUploadParams
    ): CompleteUploadResData

}