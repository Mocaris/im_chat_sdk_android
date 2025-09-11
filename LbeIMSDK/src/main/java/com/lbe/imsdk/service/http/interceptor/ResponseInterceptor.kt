package com.lbe.imsdk.service.http.interceptor

import coil3.network.NetworkRequestBody
import coil3.network.httpBody
import coil3.request.ImageResult
import com.lbe.imsdk.manager.*
import com.lbe.imsdk.service.http.HttpClient
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

/**
 *
 *
 * @Date 2025-07-16
 */
class ResponseInterceptor : Interceptor {

    private val sdkConfig get() = LbeIMSDKManager.sdkInitConfig
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        if (response.code == 200) {
            // 解析数据 json string
            try {
                val json = Json.parseToJsonElement(response.body.string()).jsonObject
                val code = "${json["code"]}".toIntOrNull() ?: -1
                val msg = json["msg"]
                val dlt = json["dlt"]
                val data = Json.encodeToString(json["data"])
                response = response.newBuilder().body(
                    data.toResponseBody(HttpClient.contentType_json)
                ).build()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return response
    }
}

class HttpProgressInterceptor(
    private val onProgress: (Int) -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
            .body(ProgressResponseBody(originalResponse.body, onProgress))
            .build()
    }
}

class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val onProgress: (Int) -> Unit
) : ResponseBody() {
    override fun contentType() = responseBody.contentType()
    override fun contentLength() = responseBody.contentLength()

    override fun source(): BufferedSource {
        return source(responseBody.source()).buffer()
    }

    private fun source(source: Source): Source {
        var totalBytesRead = 0L
        val contentLength = contentLength()
        return object : ForwardingSource(source) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                if (bytesRead != -1L) {
                    totalBytesRead += bytesRead
                    val progress = if (contentLength > 0) {
                        (100 * totalBytesRead / contentLength).toInt()
                    } else 0
                    onProgress(progress)
                }
                return bytesRead
            }
        }.buffer()
    }
}