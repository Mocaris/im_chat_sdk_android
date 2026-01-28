package com.lbe.imsdk.repository.model

import com.lbe.imsdk.repository.remote.model.SourceUrl
import kotlinx.serialization.json.Json
import java.io.Serializable

/***
 * sdk 初始化参数
 */
data class SDKInitConfig(
    val lbeSign: String,
    val lbeIdentity: String,
    val nickId: String,
    val nickName: String,
    val phone: String,
    val email: String,
    val headerIcon: String,
    val language: String,
    val device: String,
    val source: String,
    val extraInfo: String,
    var groupID: String,
    var domain: String,
) : Serializable {

    val parseHeaderIcon = try {
        Json.decodeFromString<SourceUrl>(headerIcon)
    } catch (e: Exception) {
        headerIcon
    }

    val supportLanguage: String
        get() = when (language) {
            "zh" -> "zh"
            "en" -> "en"
            else -> "en"
        }
}