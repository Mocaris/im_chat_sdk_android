package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.R
import com.lbe.imsdk.provider.LocalThemeColors
import com.valentinilk.shimmer.shimmer

/**
 *
 * @Author mocaris
 * @Date 2026-01-29
 * @Since
 */

//@Composable
//fun TimeoutTipFloat() {
//    val conversationVM = LocalCurrentConversationViewModel.current
//    val timeoutReply = conversationVM.timeOutReply.value
//
//    if (timeoutReply) {
//        Text(
//            text = stringResource(
//                R.string.chat_session_status_3,
//                "${conversationVM.timeOutConfig.value?.timeout ?: 5}"
//            ),
//            textAlign = TextAlign.Center,
//            style = TextStyle(
//                fontSize = 12.sp, color = LocalThemeColors.current.conversationSystemTextColor
//            ),
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(LocalThemeColors.current.tipBackgroundColor)
//                .padding(horizontal = 15.dp, vertical = 14.dp)
//        )
//    }
//}


@Composable
fun StartCustomerServiceButton(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, LocalThemeColors.current.customButtonBorder, RoundedCornerShape(12.dp))
            .clickable(
                onClick = onClick
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_cs),
            contentDescription = "service"
        )
        Text(text = stringResource(R.string.chat_session_status_34), fontSize = 12.sp)

    }
}

@Composable
fun CloseCustomerServiceButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier.size(34.dp),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = CircleShape
    ) {
        Image(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "close"
        )

    }
}


@Composable
fun ConversationShimmer(shimmerCount: Int = 5) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        repeat(shimmerCount) {
            ShimmerItem(left = it % 2 == 0)
        }
    }
}

@Composable
fun ShimmerItem(left: Boolean = true) {
    @Composable
    fun HeaderShimmer() {
        Box(modifier = Modifier.shimmer()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (left) {
            HeaderShimmer()
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = if (left) Alignment.Start else Alignment.End
        ) {
            Box(Modifier.shimmer()) {
                Box(
                    modifier = Modifier
                        .height(15.dp)
                        .width(50.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.LightGray)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
                    .padding(10.dp)
                    .shimmer(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .height(15.dp)
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
        if (!left) {
            HeaderShimmer()
        }
    }
}