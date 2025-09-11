package com.lbe.imsdk.repository.db.entry.convert

import androidx.room.*
import com.lbe.imsdk.repository.db.entry.IMUploadTask
import kotlinx.serialization.json.*

/**
 *
 * @Author mocaris
 */
class UploadTaskConvert {

    @TypeConverter
    fun fromString(value: String): IMUploadTask? {
        return try {
            if (value.isEmpty()) {
                return null
            }
            Json.decodeFromString<IMUploadTask>(value)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun toString(value: IMUploadTask?): String? {
        return value?.let {
            Json.encodeToString(it)
        } ?: ""
    }
}