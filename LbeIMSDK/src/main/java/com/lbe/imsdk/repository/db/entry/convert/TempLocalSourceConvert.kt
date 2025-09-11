package com.lbe.imsdk.repository.db.entry.convert

import androidx.room.*
import com.lbe.imsdk.repository.remote.model.MediaMessageContent
import kotlinx.serialization.json.*

/**
 *
 * @Author mocaris
 * @Date 2025-09-08
 */
class TempLocalSourceCovert {

    @TypeConverter
    fun fromString(value: String): MediaMessageContent? {
        return try {
            if (value.isEmpty()) {
                return null
            }
            Json.decodeFromString<MediaMessageContent>(value)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun toString(value: MediaMessageContent?): String? {
        return value?.let {
            Json.encodeToString(it)
        } ?: ""
    }
}