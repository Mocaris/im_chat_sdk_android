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
    val language: LanguageType,
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

}

enum class LanguageType(val type: Int, val locale: String) {
    ZH(0, "zh"), EN(1, "en");

    companion object {
        fun fromType(type: Int): LanguageType {
            return when (type) {
                1 -> EN
                else -> ZH
            }
        }
    }

}