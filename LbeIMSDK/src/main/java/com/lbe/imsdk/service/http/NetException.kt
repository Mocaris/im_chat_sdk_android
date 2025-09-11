package com.lbe.imsdk.service.http

/**
 *
 *
 * @Date 2025-07-16
 */
class NetException(override val message: String, val code: Int) : Exception(message) {
}