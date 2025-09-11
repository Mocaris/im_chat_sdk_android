package com.lbe.imsdk.theme

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import com.lbe.imsdk.provider.LocalThemeColors


private val LightColorScheme = lightColorScheme(
    primary = primaryColor,
    background = backgroundColor,
    surfaceTint = Color.Transparent,
    onPrimary = Color.Black,
    onSurface = Color.Black,
    surface = Color.White,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)
private val DarkColorScheme = LightColorScheme.copy()

@Composable
fun LbeIMTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val focusManager = LocalFocusManager.current
    val themeColor = remember { mutableStateOf(ThemeColors()) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    focusManager.clearFocus()
                },
            )
    ) {
        CompositionLocalProvider(
            LocalThemeColors provides themeColor.value
        ) {
            MaterialTheme(
                colorScheme = colorScheme, typography = Typography
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    content = content
                )
            }
        }
    }
}