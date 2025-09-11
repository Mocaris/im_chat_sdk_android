package com.lbe.imsdk.manager

import android.net.Uri
import com.lbe.imsdk.extension.cacheDir
import com.lbe.imsdk.extension.saveToGallery
import com.lbe.imsdk.extension.withIOContext
import com.lbe.imsdk.service.http.HttpClient
import okhttp3.Request
import java.io.File

/**
 *
 * @Date 2025-09-08
 */
object DownloadManager {

    // 下载文件
    @Throws(Exception::class)
    suspend fun downloadFile(url: String, fileName: String, onProgress: (Int) -> Unit): File? =
        withIOContext {
            val request = Request.Builder().url(url)
                .build()
            val response = HttpClient.okHttpClient.newBuilder()
//            .addInterceptor(HttpProgressInterceptor(onProgress))
                .build()
                .newCall(request)
                .execute()
            val body = response.body
            // 创建临时文件
            val tempFile = File(cacheDir, fileName)
            val totalBytes = body.contentLength()
            var bytesRead = 0L
            body.byteStream().use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read
                        if (totalBytes > 0) {
                            val progress = (bytesRead * 100 / totalBytes).toInt()
                            onProgress(progress)
                        }
                    }
                }
            }
            onProgress(100) // 确保完成时回调 100%
            return@withIOContext tempFile
        }


    // 下载并保存文件到相册
    @Throws(Exception::class)
    suspend fun downloadSaveToGallery(
        url: String,
        fileName: String,
        deleteCache: Boolean = true,
        onProgress: (Int) -> Unit,
    ): Uri? {
        try {
            val file =
                downloadFile(url, fileName, onProgress) ?: throw Exception("download file error")
            return file.saveToGallery() ?: throw Exception("save file error")
        } finally {
            if (deleteCache) {
                File(cacheDir, fileName).delete()
            }
        }
    }


}