package com.lbe.imsdk.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import com.lbe.imsdk.R

/**
 *
 * @Author mocaris
 * @Date 2025-09-10
 */
@Composable
fun IMLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    Image(
        painter = painterResource(id = R.drawable.ic_loding_new),
        contentDescription = null,
        modifier = modifier.graphicsLayer(rotationZ = rotation) // 应用旋转动画
    )
}

@Composable
fun IMProgressLoadingIndicator(modifier: Modifier = Modifier, progress: (() -> Float)? = null) {
    if (progress == null) {
        CircularProgressIndicator(
            modifier = modifier,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeWidth = 2.dp,
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            progress = progress,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeWidth = 2.dp,
        )
    }
}


@Composable
fun IMUploadIndicator(modifier: Modifier = Modifier, progress: (() -> Float)? = null) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val animation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Box(modifier = modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        IMProgressLoadingIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = R.drawable.ic_pending),
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth(0.3f)
                .sizeIn(maxWidth = 30.dp)
                .wrapContentSize()
                .graphicsLayer(alpha = animation) // 应用旋转动画
        )
    }

}
