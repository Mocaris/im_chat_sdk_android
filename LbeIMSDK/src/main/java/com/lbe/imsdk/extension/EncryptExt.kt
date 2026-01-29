package com.lbe.imsdk.extension

import okio.HashingSource
import okio.Source
import okio.blackholeSink
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/**
 *
 * @Author mocaris
 * @Date 2025-09-09
 */

object Encrypt {
    fun getMD5() = MessageDigest.getInstance("MD5")

}


// 取 md5
fun String.getMD5(): String = this.toByteArray().getMD5()
//    get() = Encrypt.getMD5().digest(this.toByteArray()).joinToString("") {
//        "%02x".format(it)
//    }

suspend fun File.getMD5(): String {
    return this.source().getMD5()

//    val md = Encrypt.getMD5()
//    val buffer = ByteArray(1024 * 8) // 8KB 缓冲区
//    FileInputStream(this@md5).use { fis ->
//        var bytesRead: Int
//        while (fis.read(buffer).also { bytesRead = it } != -1) {
//            md.update(buffer, 0, bytesRead)
//        }
//    }
//    return@withIOContext md.digest().joinToString("") { "%02x".format(it) }
}

fun InputStream.getMD5(): String {
    return this.source().getMD5()
}

fun Source.getMD5(): String {
    val hashingSource = HashingSource.md5(this@getMD5)
    hashingSource.buffer().use { hashBuffer ->
        hashBuffer.readAll(blackholeSink())
    }
    val md5 = hashingSource.hash.hex()
    return md5
}


fun ByteArray.getMD5(): String {
    return ByteArrayInputStream(this@getMD5).source().getMD5()

//    val md = Encrypt.getMD5()
//    md.update(this@md5)
//    return@withIOContext md.digest().joinToString("") { "%02x".format(it) }
}