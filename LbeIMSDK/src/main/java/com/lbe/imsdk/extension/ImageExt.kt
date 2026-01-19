package com.lbe.imsdk.extension

import android.graphics.*
import android.media.*
import android.util.*
import java.io.*

/**
 *
 * @Author mocaris
 * @Date 2025-08-25
 */
data class ThumbnailInfo(
    val width: Int,
    val height: Int,
    val size: Long,
    val path: String,
)

/**
 * 获取图片缩略图
 */
suspend fun File.generateImageThumbnail(
    originSize: Size? = null,
    maxSize: Int = 500,
    thumbnailFile: File,
): ThumbnailInfo = withIOContext {
    var bitmap: Bitmap? = null
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    val width: Int
    val height: Int
    if (originSize == null) {
        bitmap = BitmapFactory.decodeFile(this@generateImageThumbnail.absolutePath, options)
        width = bitmap.width
        height = bitmap.height
    } else {
        width = originSize.width
        height = originSize.height
    }
    //最大 maxSize 200
    //计算宽高比
    val ratio = width.toDouble() / height.toDouble()
    var targetWidth: Int = width
    var targetHeight: Int = height
    val minSize = width.coerceAtLeast(maxSize)
    if (width > minSize) {
        targetWidth = minSize
        targetHeight = (minSize / ratio).toInt()
    }
    options.inSampleSize = targetWidth / width
    options.inJustDecodeBounds = false
    var thumbnail: Bitmap? = null
    try {
        bitmap = BitmapFactory.decodeFile(this@generateImageThumbnail.absolutePath, options)
        thumbnail = ThumbnailUtils.extractThumbnail(bitmap, targetWidth, targetHeight)
        val outputStream = FileOutputStream(thumbnailFile)
        thumbnail.compress(
            when (this@generateImageThumbnail.extension.lowercase()) {
                "png" -> Bitmap.CompressFormat.PNG
                "webp" -> Bitmap.CompressFormat.WEBP
                "jpg" -> Bitmap.CompressFormat.JPEG
                else -> Bitmap.CompressFormat.JPEG
            }, 100, outputStream
        )
        return@withIOContext ThumbnailInfo(
            targetWidth,
            targetHeight,
            thumbnailFile.length(),
            thumbnailFile.path
        )
    } finally {
        bitmap?.recycle()
        thumbnail?.recycle()
    }
}


/**
 * 生成视频缩略图
 */
suspend fun File.generateVideoThumbnail(
    originSize: Size? = null,
    maxSize: Int = 500,
    thumbnailFile: File,
): ThumbnailInfo? = withIOContext {
    val metadataRetriever = MediaMetadataRetriever()
    metadataRetriever.setDataSource(absolutePath)
    metadataRetriever.embeddedPicture
    val bitmap = metadataRetriever.frameAtTime ?: return@withIOContext null
    val width: Int
    val height: Int
    if (originSize == null) {
        width = bitmap.width
        height = bitmap.height
    } else {
        width = originSize.width
        height = originSize.height
    }
    //最大 maxSize 200
    //计算宽高比
    val ratio = width.toDouble() / height.toDouble()
    var targetWidth: Int
    val targetHeight: Int
    val minSize = width.coerceAtLeast(maxSize)
    if (ratio > 1) {
        targetWidth = minSize
        targetHeight = (minSize / ratio).toInt()
    } else {
        targetWidth = (minSize * ratio).toInt()
        targetHeight = minSize
    }
    var thumbnail: Bitmap? = null
    try {
        thumbnail = ThumbnailUtils.extractThumbnail(bitmap, targetWidth, targetHeight)
        val outputStream = FileOutputStream(thumbnailFile)
        thumbnail.compress(
            Bitmap.CompressFormat.JPEG, 100, outputStream
        )
        return@withIOContext ThumbnailInfo(
            targetWidth,
            targetHeight,
            thumbnailFile.length(),
            thumbnailFile.path
        )
    } finally {
        bitmap.recycle()
        thumbnail?.recycle()
    }
}
/*

@Deprecated("")
suspend fun File.generateVideoThumbnail(
    originSize: Size? = null,
    maxSize: Int = 300,
    thumbnailFile: File,
): ThumbnailInfo? = withIOContext {
    val metadataRetriever = MediaMetadataRetriever()
    metadataRetriever.setDataSource(absolutePath)
    metadataRetriever.embeddedPicture
    val bitmap = metadataRetriever.frameAtTime
    if (null == bitmap) {
        return@withIOContext null
    }
    val width: Int
    val height: Int
    if (originSize == null) {
        width = bitmap.width
        height = bitmap.height
    } else {
        width = originSize.width
        height = originSize.height
    }
    //最大 maxSize 200
    //计算宽高比
    val ratio = width.toDouble() / height.toDouble()
    var targetWidth: Int
    val targetHeight: Int
    val minSize = width.coerceAtLeast(maxSize)
    if (ratio > 1) {
        targetWidth = minSize
        targetHeight = (minSize / ratio).toInt()
    } else {
        targetWidth = (minSize * ratio).toInt()
        targetHeight = minSize
    }
    var thumbnail: Bitmap? = null
    try {
        thumbnail = ThumbnailUtils.extractThumbnail(bitmap, targetWidth, targetHeight)
        val outputStream = FileOutputStream(thumbnailFile)
        thumbnail.compress(
            Bitmap.CompressFormat.JPEG, 100, outputStream
        )
        return@withIOContext ThumbnailInfo(
            targetWidth,
            targetHeight,
            thumbnailFile.length(),
            thumbnailFile.path
        )
    } finally {
        bitmap.recycle()
        thumbnail?.recycle()
    }
}*/
