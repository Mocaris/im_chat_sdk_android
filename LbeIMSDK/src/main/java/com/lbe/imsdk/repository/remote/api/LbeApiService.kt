package com.lbe.imsdk.repository.remote.api

import com.lbe.imsdk.repository.remote.model.*
import com.lbe.imsdk.service.http.body.*
import retrofit2.http.*

/**
 * sdk 基础 api
 *
 * @Date 2025-07-16
 */
interface LbeApiService {

    /**
     * 初始化配置
     */
    @POST("/miner-api/trans/nodes")
    suspend fun initConfig(
        @Body body: JsonBody
    ): LbeIMConfigModel



}