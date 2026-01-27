package com.lbe.imsdk.service.file

import android.net.Uri
import android.util.Size
import com.lbe.imsdk.extension.ThumbnailInfo
import com.lbe.imsdk.extension.appContext
import com.lbe.imsdk.extension.cacheDir
import com.lbe.imsdk.extension.generateImageThumbnail
import com.lbe.imsdk.extension.generateVideoThumbnail
import com.lbe.imsdk.extension.withIOContext
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 *
 * @Author mocaris
 * @Date 2026-01-27
 * @Since
 */


class CompatUriFile(
    /// 原始 uri
    val uri: Uri,
    val name: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String?,
    val duration: Long?
) {

    private val cacheFile by lazy { File(cacheDir, name) }

    private var thumbnail: ThumbnailInfo? = null

    fun isImage(): Boolean {
        return mimeType?.startsWith("image") == true
    }

    fun isVideo(): Boolean {
        return mimeType?.startsWith("video") == true
    }

    private fun thumbnailName(): String {
        if (isImage()) {
            return "thumbnail_$name"
        }
        return "thumbnail_${name}.jpg"
    }

    private fun getInputStream(): InputStream? {
        return appContext.contentResolver.openInputStream(uri)
    }

    /**
     * 缓存到 app cache
     */
    suspend fun cacheSourceFile(): File? = withIOContext {
        try {
            if (cacheFile.exists() && cacheFile.length() == size) {
                return@withIOContext cacheFile
            }
            val source =
                getInputStream()?.source()?.buffer() ?: throw IOException("media file can not read")
            cacheFile.delete()
            cacheFile.createNewFile()
            source.use {
                cacheFile.sink().buffer().use {
                    it.writeAll(source)
                    it.flush()
                }
            }
            return@withIOContext cacheFile
        } catch (e: Exception) {
            return@withIOContext null
        }
    }


    fun clearCache() {
        cacheFile.delete()
        val thumbnailFile = File(cacheDir, thumbnailName())
        thumbnailFile.deleteOnExit()
        thumbnail = null
    }

    suspend fun thumbnailImage(maxSize: Int = 500): ThumbnailInfo? = withIOContext {
        if (null == thumbnail) {
            try {
                val file = cacheSourceFile() ?: return@withIOContext null
                if (isImage()) {
                    val thumbnailFile = File(cacheDir, thumbnailName())
                    thumbnail = file.generateImageThumbnail(
                        originSize = Size(width, height),
                        thumbnailFile = thumbnailFile,
                        maxSize = maxSize
                    )
                } else if (isVideo()) {
                    val thumbnailFile = File(cacheDir, thumbnailName())
                    thumbnail = file.generateVideoThumbnail(
                        thumbnailFile = thumbnailFile,
                        maxSize = maxSize
                    )
                }
            } catch (e: Exception) {
                return@withIOContext null
            }
        }
        return@withIOContext thumbnail
    }


}
