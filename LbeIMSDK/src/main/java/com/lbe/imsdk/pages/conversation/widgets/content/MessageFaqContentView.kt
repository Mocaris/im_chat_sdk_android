package com.lbe.imsdk.pages.conversation.widgets.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.R
import com.lbe.imsdk.provider.LocalConversationStateViewModel
import com.lbe.imsdk.provider.LocalCurrentConversationViewModel
import com.lbe.imsdk.repository.remote.model.FaqMessageContent
import com.lbe.imsdk.repository.remote.model.enumeration.FaqType
import com.lbe.imsdk.widgets.IMImageView

/**
 *
 * @Author mocaris
 * @Date 2025-08-29
 */
@Composable
fun MessageFaqContentView(content: FaqMessageContent) {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = content.knowledgeBaseTitle.ifEmpty {
            stringResource(R.string.faq_default_greeting)
        }, style = TextStyle(fontSize = 14.sp))
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            for (item in content.knowledgeBaseList) {
                FaqItem(item)
            }
        }
    }
}

@Composable
private fun FaqItem(item: FaqMessageContent.KnowledgeList) {
    val conversationVM = LocalCurrentConversationViewModel.current
    Column(
        modifier = Modifier
            .fillMaxWidth(0.3f)
            .sizeIn(maxWidth = 150.dp)
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(5.dp))
            .background(
                color = MaterialTheme.colorScheme.background,

                )
            .clickable(onClick = {
                conversationVM.getFaq(FaqType.KNOWLEDGE_POINT, id = item.id)
            })
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(
            5.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (item.parseUrl != null) {
                IMImageView(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(5.dp)),
                    key = item.parseUrl.key,
                    url = item.parseUrl.url
                )
            }
        }
        Text(
            text = item.knowledgeBaseName,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Blue
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}