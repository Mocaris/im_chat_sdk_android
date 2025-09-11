package com.lbe.imsdk.extension

import coil3.disk.DiskCache
import coil3.disk.directory
import java.io.File

/**
 * coil 功能扩展
 * @Date 2025-09-05
 */

// 取 md5
val String.md5Str: String
    get() = Encrypt.getMD5().digest(this.toByteArray()).joinToString("") {
        "%02x".format(it)
    }

val coilDiskCache by lazy {
    DiskCache.Builder()
        .directory(File(coilImageDir))
        .build()
}

