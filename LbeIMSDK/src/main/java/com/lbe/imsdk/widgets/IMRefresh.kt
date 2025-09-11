package com.lbe.imsdk.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.PositionalThreshold
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 *
 * @Author
 * @Date 2023/9/12
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IMRefresh(
    modifier: Modifier = Modifier,
    refreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        state = refreshState,
        modifier = modifier,
        onRefresh = onRefresh,
        isRefreshing = refreshing,
        indicator = {
            /*   Box(
                   modifier = Modifier
                       .align(Alignment.TopCenter)
                       .pullToRefreshIndicator(
                           state = refreshState,
                           isRefreshing = refreshing,
                           elevation = 0.dp
                       ),
                   contentAlignment = Alignment.Center
               ) {
                   IMLoadingIndicator(Modifier.size(30.dp))
               }
   */
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = refreshing,
                state = refreshState,
                color = MaterialTheme.colorScheme.primary,
                containerColor = Color.White
            )
        },
        content = content,
    )
}