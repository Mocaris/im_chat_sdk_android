package com.lbe.imsdk.service.http.body

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONObject

/**
 *
 *
 * @Date 2023/3/16
 */

val JSONObject.asJsonBody: JsonBody get() = JsonBody.create(this)

val Map<String, Any?>.asJsonBody: JsonBody get() = JsonBody.create(this)

class JsonBody private constructor(private val json: String) : RequestBody() {

    private val jsonString get() = json.toByteArray(Charsets.UTF_8)

    override fun contentType(): MediaType? = "application/json; charset=UTF-8".toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        sink.write(jsonString, 0, jsonString.size)
    }

    companion object {
        fun create(json: JSONObject): JsonBody {
            return JsonBody(json.toString())
        }

        fun create(json: Map<String, Any?>): JsonBody {
            return JsonBody(JSONObject(json).toString())
        }

    }

}