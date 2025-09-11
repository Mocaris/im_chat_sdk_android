package com.lbe.imsdk.extension

import okio.buffer
import okio.sink
import okio.source
import java.io.File
import kotlin.math.min

/**
 *
 * @Author mocaris
 * @Date 2025-09-09
 */

const val BIG_FILE_THRESHOLD = 1024 * 1024 * 5L

fun File.needChunk(): Boolean {
    return length()>=BIG_FILE_THRESHOLD
}

// 文件分片读取 写到 缓存目录
fun File.readCacheChunk(start: Long, size: Long): File {
    val chunkFile = File(cacheTempDir, "chunk_${nameWithoutExtension}_${start}_${size}")
    if (chunkFile.exists()) {
        chunkFile.delete()
    }
    chunkFile.createNewFile()
    val bufferedSource = inputStream().source().buffer()
    val bufferedSink = chunkFile.sink().buffer()
    bufferedSource.use { source ->
        bufferedSink.use {
            bufferedSource.skip(start)
            var remaining = size
            while (remaining > 0) {
                val read = source.read(
                    bufferedSink.buffer,
                    remaining.coerceAtLeast(8192)
                )
                if (read == -1L) break // EOF
                remaining -= read
            }
            it.flush()
        }
    }
    return chunkFile
}