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
import androidx.compose.ui.unit.*
import com.lbe.imsdk.extension.*
import com.lbe.imsdk.manager.LbeIMSDKManager
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
                .padding(15.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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

