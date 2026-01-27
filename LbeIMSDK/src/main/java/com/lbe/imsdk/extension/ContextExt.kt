package com.lbe.imsdk.extension

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.lbe.imsdk.provider.ContextProvider
import kotlinx.coroutines.*

/**
 *
 *
 * @Date 2025-07-16
 */

val appContext: Context get() = ContextProvider.appContext


val Int.tr: String get() = appContext.getString(this)


@Composable
fun Int.px2Dp(): Dp {
    return with(LocalDensity.current) {
        toDp()
    }
}

@Composable
fun Dp.dp2Px(): Float {
    return with(LocalDensity.current) {
        toPx()
    }
}

fun CoroutineScope.launchIO(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = launch(Dispatchers.IO, start, block)

fun CoroutineScope.launchMain(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = launch(Dispatchers.Main, start, block)

fun CoroutineScope.launchAsync(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = launch(Dispatchers.Default, start, block)

suspend fun <T> withIOContext(
    block: suspend CoroutineScope.() -> T
): T = withContext<T>(Dispatchers.IO, block)

suspend fun <T> withMainContext(
    block: suspend CoroutineScope.() -> T
): T = withContext<T>(Dispatchers.Main, block)

suspend fun <T> withAsyncContext(
    block: suspend CoroutineScope.() -> T
): T = withContext<T>(Dispatchers.Default, block)


fun Context.runOnUiThread(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        Handler(Looper.getMainLooper()).post {
            block()
        }
    }
}

fun String.showToast() {
    appContext.let {
        it.runOnUiThread {
            Toast.makeText(it, this, Toast.LENGTH_SHORT).show()
        }
    }
}