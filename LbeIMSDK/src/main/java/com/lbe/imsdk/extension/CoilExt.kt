package com.lbe.imsdk.extension

import coil3.disk.DiskCache
import coil3.disk.directory
import java.io.File

/**
 * coil 功能扩展
 * @Date 2025-09-05
 */

val coilDiskCache by lazy {
    DiskCache.Builder()
        .directory(File(coilImageDir))
        .build()
}

