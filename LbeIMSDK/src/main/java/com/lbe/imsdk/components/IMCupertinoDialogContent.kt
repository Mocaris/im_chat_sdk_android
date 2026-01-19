package com.lbe.imsdk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lbe.imsdk.R
import com.lbe.imsdk.theme.colorTip

/**
 *
 * @Author mocaris
 * @Date 2025-09-11
 */
@Composable
fun IMCupertinoDialogContent(
    title: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (null != title) {
                CompositionLocalProvider(
                    LocalTextStyle provides TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                ) { title() }
            }
            if (null != content) {
                CompositionLocalProvider(
                    LocalTextStyle provides TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorTip
                    )
                ) { content() }
            }
        }
        HorizontalDivider(thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            content = actions
        )
    }
}

@Composable
fun RowScope.DialogAction(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    TextButton(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        onClick = onClick,
        content = {
            CompositionLocalProvider(
                LocalTextStyle provides TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                content = content
            )
        }
    )
}