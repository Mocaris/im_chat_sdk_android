package com.lbe.imsdk.extension

import android.R.attr.path
import android.annotation.*
import android.content.*
import android.graphics.*
import android.media.*
import android.net.*
import android.os.*
import android.provider.*
import android.util.*
import androidx.activity.result.contract.*
import androidx.core.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.buffer
import okio.sink
import okio.source
import java.io.*

/**
 *
 * @Author mocaris
 * @Date 2025-08-25
 */
data class UriFileInfo(
    val uri: Uri,
//    val path: String,
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

    fun getInputStream(): InputStream? {
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

@SuppressLint("Range")
suspend fun Uri.toUriFile(): UriFileInfo? {
//    suspend fun copyToCache(
//        uri: Uri,
//        fName: String
//    ): String = withContext(
//        Dispatchers.IO
//    ) {
//        val inputStream =
//            appContext.contentResolver.openInputStream(uri)
//                ?: throw Exception("inputStream is null")
//        val tempFile = File(appContext.cacheDir, fName)
//
//        tempFile.outputStream().use { output ->
//            inputStream.copyTo(output)
//        }
//        return@withContext tempFile.absolutePath
//    }
    return appContext.contentResolver?.let { resolver ->
        resolver.query(this, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                val size = it.getLong(it.getColumnIndex(MediaStore.MediaColumns.SIZE))
                val width = it.getInt(it.getColumnIndex(MediaStore.MediaColumns.WIDTH))
                val height = it.getInt(it.getColumnIndex(MediaStore.MediaColumns.HEIGHT))
                val mimeType = it.getString(it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
                val orientation = it.getInt(it.getColumnIndex(MediaStore.MediaColumns.ORIENTATION))
                val duration =
                    it.getLongOrNull(it.getColumnIndex(MediaStore.MediaColumns.DURATION))
                val land = orientation == 90 || orientation == 270

//                val path: String = try {
//                    it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATA))
//                } catch (e: Exception) {
//                    copyToCache(this, name)
//                } ?: return null

                return@use UriFileInfo(
                    this,
//                    path,
                    name,
                    size,
                    if (land) height else width,
                    if (land) width else height,
                    mimeType,
                    duration
                )
            }
            return@use null
        }
    }
}

fun Uri.exists(): Boolean {
    return try {
        return appContext.contentResolver.query(this, null, null, null, null)?.use {
            it.moveToFirst()
            (it.getLongOrNull(it.getColumnIndex(MediaStore.MediaColumns.SIZE)) ?: 0) > 0
        } ?: false
    } catch (e: Exception) {
        false
    }
}

fun String.getFileName(): String {
    return this.substringAfterLast("/").substringBefore("?").substringBefore("#")
}


// 保存文件到相册
@Throws(Exception::class)
suspend fun File.saveToGallery(): Uri? = withIOContext {
    // 通过文件扩展名判断 MIME 类型
    val mimeType = when (this@saveToGallery.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "mp4" -> "video/mp4"
        else -> throw IllegalArgumentException("不支持的文件类型: ${this@saveToGallery.extension}")
    }

    return@withIOContext if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10 及以上
        val resolver = appContext.contentResolver
        val collection = if (mimeType.startsWith("image")) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }

        val subFolder = if (mimeType.startsWith("image")) "Pictures" else "Movies"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, this@saveToGallery.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, subFolder)
            put(MediaStore.MediaColumns.IS_PENDING, 1) // 写入中标志
        }

        val uri = resolver.insert(collection, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                this@saveToGallery.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            // 写入完成，清除 IS_PENDING
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }
        uri
    } else {
        // Android 9 及以下
        val targetDir = if (mimeType.startsWith("image")) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        }
        if (!targetDir.exists()) targetDir.mkdirs()

        val destFile = File(targetDir, this@saveToGallery.name)
        this@saveToGallery.copyTo(destFile, overwrite = true)

        val uri = Uri.fromFile(destFile)
        // 通知媒体库刷新
        appContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
        uri
    }
}