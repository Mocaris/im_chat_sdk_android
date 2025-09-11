package com.lbe.imsdk.extension

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 *
 * @Author mocaris
 * @Date 2025-09-09
 */

object Encrypt {
    fun getMD5() = MessageDigest.getInstance("MD5")

}


suspend fun File.md5(): String = withIOContext{
    val md = Encrypt.getMD5()
    val buffer = ByteArray(1024 * 8) // 8KB 缓冲区
    FileInputStream(this@md5).use { fis ->
        var bytesRead: Int
        while (fis.read(buffer).also { bytesRead = it } != -1) {
            md.update(buffer, 0, bytesRead)
        }
    }
    return@withIOContext md.digest().joinToString("") { "%02x".format(it) }
}


suspend fun ByteArray.md5(): String = withIOContext{
    val md = Encrypt.getMD5()
    md.update(this@md5)
    return@withIOContext md.digest().joinToString("") { "%02x".format(it) }
}