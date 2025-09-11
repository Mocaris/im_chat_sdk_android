package com.lbe.imsdk.theme

import androidx.compose.ui.graphics.Color


val primaryColor = Color(0xFFDAE6FF)
val backgroundColor = Color(0xFFF3F4F6)
val colorEBEBEB = Color(0xffEBEBEB)
val colorTip = Color(0xFF979797)


class ThemeColors {
    val conversationSelfBgColor: Color get() = primaryColor
    val conversationFromBgColor: Color get() = Color.White
    val conversationTextColor: Color get() = Color.Black
    val conversationTimeTextColor: Color get() = Color(0xFF979797)
    val conversationSystemTextColor: Color get() = Color(0xFF979797)
    val tipBackgroundColor: Color get() = colorEBEBEB
}