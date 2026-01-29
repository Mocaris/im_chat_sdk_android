package com.lbe.imsdk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 *
 * @Author mocaris
 * @Date 2025-09-11
 */

data class DialogAction(
    val onClick: (() -> Unit),
    val content: @Composable RowScope.() -> Unit,
)

@Composable
fun IMCupertinoDialogContent(
    title: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    actions: List<DialogAction>? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (null != title) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme
                        .typography
                        .bodyLarge
                        .copy(fontWeight = FontWeight.SemiBold),
                    title
                )
            }
            if (null != content) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    content
                )
            }
        }
        if (null != actions) {
            HorizontalDivider(thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                content = {
                    actions.forEachIndexed { index, action ->
                        DialogsAction(
                            onClick = action.onClick,
                            content = action.content
                        )
                        if (index < actions.lastIndex) {
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight(),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RowScope.DialogsAction(
    onClick: () -> Unit, content: @Composable RowScope.() -> Unit
) {
    TextButton(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        content = content,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}