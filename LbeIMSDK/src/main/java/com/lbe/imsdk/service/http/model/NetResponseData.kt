package com.lbe.imsdk.service.http.model

/**
 *
 *
 * @Date 2025-07-16
 */
abstract class NetResponseData<T> {
    abstract val code: Int
    abstract val msg: String
    abstract val dlt: String
    abstract val data: T?

    val isSuccess get() = code == 0
}