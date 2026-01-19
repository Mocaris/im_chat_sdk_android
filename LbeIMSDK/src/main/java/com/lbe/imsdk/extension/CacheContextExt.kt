package com.lbe.imsdk.extension

import java.io.File

/**
 *
 * @Date 2025-09-05
 */


val cacheDir: String by lazy {
    File(appContext.cacheDir.path).also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val coilImageDir: String by lazy {
    File("${cacheDir}/coil").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val cacheImageDir: String by lazy {
    File("${cacheDir}/images").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val cacheVideoDir: String by lazy {
    File("${cacheDir}/video").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}

val cacheTempDir: String by lazy {
    File("${cacheDir}/temp").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }.path
}