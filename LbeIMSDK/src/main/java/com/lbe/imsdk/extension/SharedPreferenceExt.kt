package com.lbe.imsdk.extension

import android.content.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.content.edit
import com.lbe.imsdk.provider.*
import com.lbe.imsdk.repository.model.SDKInitConfig
import com.lbe.imsdk.repository.remote.model.LbeIMConfigModel.HostData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 *
 *
 * @Date 2025-07-16
 */

private const val sharedPreferencesName = "lbe_sdk_preferences"

val appSharedPreferences by lazy {
    appContext.getSharedPreferences(
        sharedPreferencesName,
        Context.MODE_PRIVATE
    )
}

fun saveHostConfig(domain: String = "DEFAULT", config: HostData) {
    appSharedPreferences.edit {
        putString("${domain}_host", Json.encodeToString(config))
    }
}

fun getHostConfig(domain: String = "DEFAULT"): HostData? {
    val str = appSharedPreferences.getString("${domain}_host", null)
    if (str.isNullOrEmpty()) {
        return null
    }
    return try {
        Json.decodeFromString<HostData>(str)
    } catch (e: Exception) {
        null
    }
}