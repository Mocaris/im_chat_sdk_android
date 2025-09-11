package com.lbe.imsdk.extension

import androidx.compose.runtime.*
import androidx.navigation3.runtime.*
import com.lbe.imsdk.pages.navigation.NavigationBackStack
import com.lbe.imsdk.pages.navigation.PageRoute
import com.lbe.imsdk.provider.*
import com.lbe.imsdk.provider.LocalNavBackStack

/**
 * [PageRoute] Extension
 *
 * @Date 2025-08-18
 */

val Navigator
    @Composable
    get() = LocalNavBackStack.current

fun NavigationBackStack.current(): PageRoute? {
    return this.lastOrNull()
}

fun NavigationBackStack.offAll(key: PageRoute) {
    if (!contains(key)) {
        this.add(0, key)
    }
    removeAll { it != key }
}

fun NavigationBackStack.off(key: PageRoute) {
    add(key)
    val index = indexOf(key)
    if (index > 0) {
        removeAt(index - 1)
    }
}


/**
 * 导航到指定页面
 */
fun <T : PageRoute> NavigationBackStack.navigate(navKey: T, duplicate: Boolean = false) {
    if (!duplicate) {
        if (this.contains(navKey)) {
            return
        }
    }
    this.add(navKey)
}

/**
 * 弹出 页面
 * @param navKey 待弹出的页面, null 表示弹出栈顶页面
 */
fun NavigationBackStack.pop(navKey: PageRoute? = null) {
    if (null != navKey) {
        if (this.contains(navKey)) {
            this.remove(navKey)
        }
        return
    }
    if (this.size > 1) {
        this.removeLastOrNull()
    }
}