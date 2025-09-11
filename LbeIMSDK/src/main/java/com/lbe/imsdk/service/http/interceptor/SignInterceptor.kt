package com.lbe.imsdk.service.http.interceptor

import com.lbe.imsdk.manager.*
import okhttp3.*

/**
 *
 *
 * @Date 2025-07-16
 */
class SignInterceptor : Interceptor {

    private val sdkConfig get() = LbeIMSDKManager.sdkInitConfig

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val header = hashMapOf<String, String>()
        if (null == request.header("lbeSign")) {
            header["lbeSign"] = sdkConfig?.lbeSign ?: ""
        }
        if (null == request.header("lbeIdentity")) {
            header["lbeIdentity"] = sdkConfig?.lbeIdentity ?: ""
        }
        LbeIMSDKManager.lbeToken?.let {
            header["lbeToken"] = it
        }
        LbeIMSDKManager.lbeSession?.let {
            header["lbeSession"] = it
        }
        if (header.isNotEmpty()) {
            request = request.newBuilder().apply {
                header.forEach {
                    addHeader(it.key, it.value)
                }
            }.build()
        }

        return chain.proceed(request)

    }
}