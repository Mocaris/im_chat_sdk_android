package com.lbe.imsdk.components

import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import com.lbe.imsdk.provider.LocalDialogManager

/**
 *
 * @Author mocaris
 * @Date 2025-09-05
 */

typealias OnDismissRequest = (dismiss: () -> Unit) -> Unit

internal data class DialogComponent(
    val key: String?,
    val onDismissRequest: OnDismissRequest,
    val properties: DialogProperties,
    val content: @Composable () -> Unit
)

class DialogManager {
    internal val dialogQueue = mutableStateListOf<DialogComponent>()

    fun show(
        key: String? = null,
        onDismissRequest: OnDismissRequest,
        properties: DialogProperties = DialogProperties(),
        content: @Composable () -> Unit
    ) {
        val dialog = DialogComponent(key, onDismissRequest, properties, content)
        dialogQueue.add(dialog)
    }

    fun dismiss(key: String? = null) {
        if (key == null) {
            dialogQueue.firstOrNull()?.let {
                it.onDismissRequest {
                    dialogQueue.remove(it)
                }
            }
        } else {
            for (dialog in dialogQueue.filter { it.key == key }) {
                dialog.onDismissRequest {
                    dialogQueue.remove(dialog)
                }
            }
        }
    }

    fun dismissAll() {
        for (dialog in dialogQueue) {
            dialog.onDismissRequest {
                dialogQueue.remove(dialog)
            }
        }
    }
}

@Composable
fun DialogHost(hostContent: @Composable () -> Unit) {
    hostContent()
    val dialogManager = LocalDialogManager.current
    dialogManager.dialogQueue.firstOrNull()?.let {
        Dialog(
            onDismissRequest = {
                it.onDismissRequest({
                    dialogManager.dialogQueue.remove(it)
                })
            },
            properties = it.properties,
            content = it.content
        )
    }
}