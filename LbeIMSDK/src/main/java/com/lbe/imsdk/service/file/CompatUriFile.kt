package com.lbe.imsdk.service.file

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import com.lbe.imsdk.extension.*
import okio.FileNotFoundException
import okio.buffer
import okio.sink
import okio.source
import java.io.File

/**
 *
 * @Author mocaris
 * @Date 2026-01-27
 * @Since
 */
class UriFileMetaData(
    val absolutePath: String,
    val name: String,
    val mimeType: String?,
    val length: Long,
    val extension: String,
    val extraMetadata: ExtraMetadata,
    /// only media file image, video
    val mediaMetadata: MediaMetadata?,
) {
    class ExtraMetadata(
        val md5: String,
    )

    class MediaMetadata(
        val width: Int,
        val height: Int,
        val duration: Long,
    )

    fun isImage(): Boolean {
        return mimeType?.startsWith("image") == true
    }

    fun isVideo(): Boolean {
        return mimeType?.startsWith("video") == true
    }
}

class CompatUriFile(val uri: Uri) {

    /// 原始 uri
    private val documentFile = DocumentFile.fromSingleUri(appContext, uri)

    private var uriFileMetaData: UriFileMetaData? = null

    private var thumbnail: ThumbnailInfo? = null

    private fun isImage(mimeType: String?): Boolean {
        return mimeType?.startsWith("image") == true
    }

    private fun isVideo(mimeType: String?): Boolean {
        return mimeType?.startsWith("video") == true
    }

    suspend fun thumbnailImage(): ThumbnailInfo? = withIOContext {
        if (null == thumbnail) {
            try {
                val metaData = getFileMetaData()
                val originFile = File(metaData.absolutePath)
                if (metaData.isImage()) {
                    val thumbnailFile =
                        File(
                            cacheDir,
                            "thum_${metaData.extraMetadata.md5}.${originFile.extension}"
                        )
                    thumbnail = originFile.generateImageThumbnail(
                        originSize = metaData.mediaMetadata?.let {
                            Size(it.width, it.height)
                        },
                        thumbnailFile = thumbnailFile,
                        maxSize = 500
                    )
                } else if (metaData.isVideo()) {
                    val thumbnailFile = File(cacheDir, "thum_${metaData.extraMetadata.md5}.jpg")
                    thumbnail = originFile.generateVideoThumbnail(
                        originSize = metaData.mediaMetadata?.let {
                            Size(it.width, it.height)
                        },
                        thumbnailFile = thumbnailFile,
                        maxSize = 500
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return@withIOContext thumbnail
    }

    @Throws(Exception::class)
    suspend fun getFileMetaData(): UriFileMetaData = withIOContext {
        if (null != uriFileMetaData) {
            return@withIOContext uriFileMetaData!!
        }
        documentFile ?: throw FileNotFoundException("media file can not read")

        val name = documentFile.name ?: throw FileNotFoundException("media file can not read")
        val mimeType = documentFile.type
        val cacheFile = getCacheSourceFile()
        var mediaMetadata: UriFileMetaData.MediaMetadata? = null
        if (isImage(mimeType)) {
            mediaMetadata = getImageMetaData(cacheFile.first)

        }
        if (isVideo(mimeType)) {
            mediaMetadata = getVideoMetaData(cacheFile.first)
        }

        uriFileMetaData = UriFileMetaData(
            name = name,
            absolutePath = cacheFile.first.absolutePath,
            length = documentFile.length(),
            mimeType = mimeType,
            extraMetadata = cacheFile.second,
            extension = cacheFile.first.extension,
            mediaMetadata = mediaMetadata
        )
        return@withIOContext uriFileMetaData!!
    }

    /// 获取图片尺寸
    @SuppressLint("ExifInterface")
    private fun getImageMetaData(file: File): UriFileMetaData.MediaMetadata {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        var width = options.outWidth
        var height = options.outHeight
        try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            // 如果图片被旋转了 90 度或 270 度，需要交换宽高
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270
            ) {
                val temp = width
                width = height
                height = temp
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return UriFileMetaData.MediaMetadata(
            width = width,
            height = height,
            duration = 0L
        )
    }

    private fun getVideoMetaData(file: File): UriFileMetaData.MediaMetadata {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)

            // 1. 获取原始宽高
            val rawWidth =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                    ?: 0
            val rawHeight =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                    ?: 0

            // 2. 获取时长 (毫秒)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0L

            // 3. 处理旋转角度 (重要：手机录制的视频可能带有 90/270 度旋转标记)
            val rotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toInt() ?: 0

            return if (rotation == 90 || rotation == 270) {
                // 如果旋转了 90 或 270 度，需要交换宽高，否则 UI 坑位会出错
                UriFileMetaData.MediaMetadata(rawHeight, rawWidth, duration)
            } else {
                UriFileMetaData.MediaMetadata(rawWidth, rawHeight, duration)
            }
        } finally {
            retriever.release() // 必须释放资源
        }
    }

    /**
     * 缓存到 app cache
     * @return first file md5, second file
     */
    @SuppressLint("Range")
    @Throws(Exception::class)
    private suspend fun getCacheSourceFile(): Pair<File, UriFileMetaData.ExtraMetadata> =
        withIOContext {
            val resolver = appContext.contentResolver
            // 先读取 file md5
            val fileMD5 = resolver.openInputStream(uri)?.source()?.getMD5()
                ?: throw FileNotFoundException("media file can not read")
            documentFile ?: throw FileNotFoundException("media file can not read")
            val extension = android.webkit.MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(documentFile.type)
            val extraMetadata = UriFileMetaData.ExtraMetadata(md5 = fileMD5)
            // 是否存在 该缓存文件
            val cacheFile = File(cacheDir, "${fileMD5}.${extension}")
            if (cacheFile.exists() && documentFile.length() == cacheFile.length()) {
                return@withIOContext cacheFile to extraMetadata
            }
            val fileSource = resolver.openInputStream(uri)?.source()
                ?: throw FileNotFoundException("media file can not read")
            cacheFile.deleteRecursively()
            cacheFile.createNewFile()
            val targetSink = cacheFile.sink()
            fileSource.buffer().use { bufferSource ->
                targetSink.buffer().use { sink ->
                    sink.writeAll(bufferSource)
                    sink.flush()
                }
            }
            return@withIOContext cacheFile to extraMetadata
        }


}
