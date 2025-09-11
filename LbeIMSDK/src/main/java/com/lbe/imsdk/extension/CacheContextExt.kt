package com.lbe.imsdk.extension

import java.io.File

/**
 *
 * @Date 2025-09-05
 */


val cacheDir by lazy {
    File(appContext.cacheDir.path).also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val coilImageDir by lazy {
    File("${cacheDir}/coil").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val cacheImageDir by lazy {
    File("${cacheDir}/images").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val cacheVideoDir by lazy {
    File("${cacheDir}/video").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val cacheTempDir by lazy {
    File("${cacheDir}/temp").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}