package com.lbe.imsdk.pages.conversation.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.lbe.imsdk.R
import com.lbe.imsdk.extension.px2Dp
import com.lbe.imsdk.widgets.*

/**
 * 键盘输入框
 * @Date 2025-08-22
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardInputBox(
    value: TextFieldValue,
    maxLength: Int = 500,
    onValueChange: (TextFieldValue) -> Unit,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    keyboardActions: KeyboardActionScope.() -> Unit = {},
    onSend: () -> Unit,
    onPickPhoto: () -> Unit,
    onFocusChanged: (FocusState) -> Unit = {}
) {
    val focusRequester = focusRequester ?: remember { FocusRequester() }
    val showExpandedEdit = remember { mutableStateOf(false) }
    val lineCount = remember { mutableIntStateOf(0) }

    IMEditText(
        value.let {
            if (it.text.length > maxLength) {
                it.copy(text = value.text.take(maxLength))
            } else it
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(maxHeight = 120.dp)
            .focusRequester(focusRequester)
            .onFocusChanged(onFocusChanged),
        onValueChange = { v ->
            if (v.text.length > maxLength) {
                onValueChange(v.copy(text = v.text.take(maxLength)))
            } else {
                onValueChange(v)
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Unspecified,
            keyboardType = KeyboardType.Text,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardActions = KeyboardActions(keyboardActions),
        onTextLayout = { res ->
            lineCount.intValue = res.lineCount
        },
        hint = stringResource(R.string.chat_session_status_12),
        decorationBox = { inputBox ->
            InputDecorationBox(
                enableSend = value.text.isNotEmpty(),
                lineCount = lineCount.intValue,
                onSend = onSend,
                onPickPhoto = onPickPhoto,
                onExpanded = {
                    showExpandedEdit.value = true
                },
            ) {
                inputBox()
            }
        }
    )
    if (showExpandedEdit.value) {
        BasicAlertDialog(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)),
            onDismissRequest = {
                showExpandedEdit.value = false
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
        ) {
            LaunchedEffect(showExpandedEdit) {
                focusRequester.requestFocus()
            }
            IMEditText(
                value,
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 200.dp)
                    .padding(15.dp)
                    .focusRequester(focusRequester),
                enabled = enabled,
                onValueChange = onValueChange,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardActions = KeyboardActions(keyboardActions),
                hint = stringResource(R.string.chat_session_status_12),
            )
        }
    }
}

@Composable
private fun InputDecorationBox(
    enableSend: Boolean = false,
    lineCount: Int = 1,
    onSend: () -> Unit,
    onPickPhoto: () -> Unit,
    onExpanded: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val layoutHeight = remember { mutableStateOf<Int?>(null) }
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .then(layoutHeight.value?.let { Modifier.defaultMinSize(minHeight = layoutHeight.value!!.px2Dp()) }
                    ?: Modifier),
        ) {
            InputBoxActionWidget(
                painterResource(R.drawable.ic_open_file),
                contentDescription = "选择图片",
                modifier = Modifier.align(Alignment.BottomStart),
                onClick = onPickPhoto
            )

            this@Row.AnimatedVisibility(
                modifier = Modifier.align(Alignment.TopStart),
                visible = lineCount >= 4
            ) {
                InputExpandedWidget(onClick = onExpanded)
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(corner = CornerSize(12.dp)))
                .background(Color.White)
                .onSizeChanged {
                    if (it.height > 0) {
                        layoutHeight.value = it.height
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(10.dp)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.CenterStart,
                content = content
            )
            InputBoxActionWidget(
                painterResource(R.drawable.ic_send),
                modifier = Modifier.align(Alignment.Bottom),
                contentDescription = "发送",
                onClick = if (enableSend) onSend else null
            )
        }
    }
}


@Composable
private fun InputBoxActionWidget(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .then(modifier)
            .size(42.dp)
            .clip(shape = CircleShape)
            .background(Color.White)
            .clickable(enabled = null != onClick, onClick = { onClick?.invoke() })
            .padding(10.dp),
        contentScale = ContentScale.Inside,
        alignment = Alignment.Center,
    )
}


@Composable
fun InputExpandedWidget(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Image(
        painter = painterResource(R.drawable.ic_expanded),
        contentDescription = "展开",
        modifier = Modifier
            .then(modifier)
            .size(24.dp)
            .clip(shape = CircleShape)
            .background(Color.White)
            .clickable(enabled = null != onClick, onClick = {
                onClick?.invoke()
            })
            .padding(5.dp),
        alignment = Alignment.Center
    )
}
