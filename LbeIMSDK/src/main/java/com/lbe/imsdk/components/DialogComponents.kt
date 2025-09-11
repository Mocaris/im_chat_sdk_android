package com.lbe.imsdk.components

import androidx.compose.runtime.*
import androidx.compose.ui.window.*

/**
 *
 * @Author mocaris
 * @Date 2025-09-05
 */

internal data class DialogComponent(
    val key: String?,
    val onDismissRequest: () -> Unit,
    val properties: DialogProperties,
    val content: @Composable () -> Unit
)

object DialogManager {
    internal val dialogQueue = mutableStateListOf<DialogComponent>()

    fun show(
        key: String? = null,
        onDismissRequest: () -> Unit,
        properties: DialogProperties = DialogProperties(),
        content: @Composable () -> Unit
    ) {
        val dialog = DialogComponent(key, onDismissRequest, properties, content)
        dialogQueue.add(dialog)
    }

    fun requestDismiss(key: String? = null) {
        if (null != key) {
            dialogQueue.filter { it.key == key }.forEach {
                it.onDismissRequest()
            }
        } else {
            dialogQueue.firstOrNull()?.onDismissRequest()
        }
    }

    fun dismiss(key: String? = null) {
        if (key == null) {
            dialogQueue.removeFirstOrNull()
        } else {
            dialogQueue.removeAll { it.key == key }
        }
    }

}

@Composable
fun DialogHost(content: @Composable () -> Unit) {
    content()
    if (DialogManager.dialogQueue.isNotEmpty()) {
        val dialog = DialogManager.dialogQueue.first()
        Dialog(dialog.onDismissRequest, dialog.properties, content = dialog.content)
    }
}