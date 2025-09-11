package com.lbe.imsdk.widgets

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.paging.*
import androidx.paging.compose.*

/**
 *
 * @Author
 * @Date 2023/9/12
 */
@Composable
fun IMLazyColumn(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<*>,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
    ) {
        content()
        item {
            val loadState = pagingItems.loadState
            val itemCount = pagingItems.itemCount
            var text = ""
            var progress = false
            if (itemCount == 0) {
                when (loadState.refresh) {
                    is LoadState.NotLoading -> {
                        text = "暂无数据"
                    }

                    is LoadState.Error -> {
                        text = "加载失败"
                    }

                    is LoadState.Loading -> {
                        text = "加载中..."
                        progress = true
                    }
                }
            } else {
                when (loadState.append) {
                    is LoadState.NotLoading -> {
                        text = if (loadState.append.endOfPaginationReached) {
                            "没有更多了"
                        } else {
                            "加载完成"
                        }
                    }

                    is LoadState.Error -> {
                        text = "加载失败"
                    }

                    is LoadState.Loading -> {
                        text = "加载中..."
                        progress = true
                    }
                }
            }

            if (itemCount != 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (progress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp), strokeWidth = 1.dp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(text = text)
                }
            }
        }
    }

}