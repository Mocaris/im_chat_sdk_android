package com.lbe.imsdk.repository.remote

import com.lbe.imsdk.repository.remote.api.*
import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.repository.remote.model.enumeration.*
import com.lbe.imsdk.service.http.*
import com.lbe.imsdk.service.http.HttpClient.BASE_URL
import com.lbe.imsdk.service.http.body.*
import com.lbe.imsdk.service.http.extension.*
import kotlinx.coroutines.*

/**
 * 基础配置接口
 *
 * @Date 2025-07-16
 */
class LbeApiRepository(val domain: String = BASE_URL) {
    val apiService by lazy {
        HttpClient.retrofitFactory(domain.ifEmpty { BASE_URL })
            .create(LbeApiService::class.java)
    }

    suspend fun initConfig(
        @RoleType roleType: Int = RoleType.NORMAL_USER,
        @ReqSource source: Int = ReqSource.APP,
    ): LbeIMConfigModel.HostData = withContext(Dispatchers.IO) {
        apiService.initConfig(
            body = mutableMapOf(
                "roleType" to roleType,
                "source" to source
            ).asJsonBody
        ).accept()!!
    }

}