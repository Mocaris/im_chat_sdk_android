package com.lbe.imsdk.pages.init

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.lbe.imsdk.extension.*
import com.valentinilk.shimmer.shimmer

/**
 * 初始化SDK页面
 *
 * @Date 2025-07-16
 */

@Composable
fun InitSdkPage() {
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(15.dp)
        ) {
            InitShimmer()
        }
        /* Column(
             Modifier
                 .fillMaxSize()
                 .padding(padding),
             verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             CircularProgressIndicator()
             Text(text = "正在初始化SDK...")
         }*/
    }
}

@Composable
fun InitShimmer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        repeat(5) {
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