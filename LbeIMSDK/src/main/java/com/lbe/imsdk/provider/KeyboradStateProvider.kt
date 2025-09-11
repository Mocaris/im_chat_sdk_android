package com.lbe.imsdk.provider

/**
 *
 * 监控键盘状态
 * @Date 2025-09-04
 */
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity

/**
 * 键盘状态枚举
 */
enum class KeyboardState {
    Opened, Closed;

    fun isOpened() = this == Opened
}

/**
 * 监听键盘状态的 Composable
 */
@Composable
fun rememberKeyboardState(): KeyboardState {
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current

    // 根据 IME 高度判断键盘是否弹起
    val isImeVisible by remember {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    return if (isImeVisible) KeyboardState.Opened else KeyboardState.Closed
}