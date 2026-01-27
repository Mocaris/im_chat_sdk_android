package com.lbe.imsdk.service.http.body

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

/**
 *
 *
 * @Date 2023/3/16
 */

typealias ProgressListener = (Float) -> Unit

class ProgressBody(private val body: RequestBody, private val listener: ProgressListener? = null) :
    RequestBody() {

    private var writeCount = 0L
    override fun contentLength(): Long = body.contentLength()

    override fun contentType(): MediaType? = body.contentType()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        if (totalBytes <= 0L) {
            body.writeTo(sink)
            return
        }

        var bytesWritten = 0L
        var lastProgress = -1f

        val progressSink = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                bytesWritten += byteCount

                // 1. 直接计算比例，toFloat() 会自动处理 Long 到 Float 的转换
                val currentProgress = if (totalBytes > 0L) {
                    bytesWritten.toFloat() / totalBytes
                } else 0f

                // 2. 增加安全检查：确保不是 NaN 或 Infinity
                if (currentProgress.isNaN() || currentProgress.isInfinite()) return

                // 3. 进度变化过滤 (1% 阈值)
                if (currentProgress - lastProgress >= 0.01f || currentProgress >= 1f) {
                    lastProgress = currentProgress
                    // 4. 使用 coerceIn 确保最终值严格在 [0, 1]
                    listener?.invoke(currentProgress.coerceIn(0f, 1f))
                }
            }
        }

        val bufferedSink = progressSink.buffer()
        body.writeTo(bufferedSink)
        bufferedSink.flush()
    }

}


