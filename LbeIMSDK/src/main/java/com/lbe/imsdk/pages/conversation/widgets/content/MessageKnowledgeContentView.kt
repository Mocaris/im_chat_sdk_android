package com.lbe.imsdk.pages.conversation.widgets.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.provider.LocalSessionViewModel
import com.lbe.imsdk.repository.remote.model.KnowledgePointMessageContent
import com.lbe.imsdk.repository.remote.model.enumeration.FaqType

/**
 *
 * @Author mocaris
 * @Date 2025-08-29
 */
@Composable
fun MessageKnowledgeContentView(title: String, content: List<KnowledgePointMessageContent>) {
    val conversationVM = LocalSessionViewModel.current
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (title.isNotEmpty()) {
            Text(text = title, style = TextStyle(fontSize = 14.sp))
        }
        for (item in content) {
            Text(
                text = item.knowledgePointName,
                style = TextStyle(fontSize = 12.sp, color = Color.Blue),
                modifier = Modifier.clickable(onClick = {
                    conversationVM.getFaq(FaqType.KNOWLEDGE_ANSWER, id = item.id)
                })
            )
        }

    }
}