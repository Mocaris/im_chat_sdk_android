package com.lbe.imsdk.pages.conversation.widgets.message.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.provider.LocalThemeColors

/**
 *
 * 知识库 答案
 * @Date 2025-08-29
 */
@Composable
fun MessageSystemContentView(content: String) {
    val themeColors = LocalThemeColors.current
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = content,
            style = TextStyle(fontSize = 10.sp, color = themeColors.conversationSystemTextColor)
        )
    }
}