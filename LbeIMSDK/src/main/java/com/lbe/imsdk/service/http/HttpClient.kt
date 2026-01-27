package com.lbe.imsdk.service.http

import com.lbe.imsdk.service.http.interceptor.*
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.*
import retrofit2.*
import retrofit2.converter.kotlinx.serialization.*
import java.util.concurrent.*

/**
 *
 *
 * @Date 2025-07-16
 */
object HttpClient {
    // uat
    const val BASE_URL = "https://mob.imsz.online"

    // sit
//    private const val BASE_URL = "http://www.im-sit-dreaminglife.cn/"

    // dev
//     private const val BASE_URL = "http://www.im-dreaminglife.cn/"

    val contentType_json = "application/json".toMediaType()
    val contentType_stream = "application/octet-stream".toMediaType()

    val logInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    val okHttpClient by lazy {
        OkHttpClient
            .Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(SignInterceptor())
//            .addInterceptor(LoopHostInterceptor())
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .addInterceptor(logInterceptor)
            .build()
    }


    fun retrofitFactory(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                Json.asConverterFactory(contentType_json)
            ).build()
    }

}