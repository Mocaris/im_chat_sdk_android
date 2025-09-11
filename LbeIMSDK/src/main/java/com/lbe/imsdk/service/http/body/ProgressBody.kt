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
        val count = contentLength()
        val forwardSink = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                writeCount += byteCount
                listener?.invoke((100 * writeCount / count) / 100f)
            }

        }.buffer()
        body.writeTo(forwardSink)
        forwardSink.flush()
    }

}


