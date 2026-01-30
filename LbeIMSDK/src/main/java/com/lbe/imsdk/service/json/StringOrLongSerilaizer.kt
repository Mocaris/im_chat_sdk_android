package com.lbe.imsdk.service.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

/**
 *
 * @Author mocaris
 * @Date 2026-01-30
 * @Since
 */
object StringOrLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrLong", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long {
        val input = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content
        if (input.isBlank()) return 0L // 如果是 ""，返回默认值 0
        return input.toLongOrNull() ?: 0L // 如果是 "123" 或 123，转为 Long
    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }
}