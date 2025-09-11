package com.lbe.imsdk.pages.unknown

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 *
 * @Author mocaris
 * @Date 2025-08-20
 */
@Composable
fun  UnknownPage() {
    Scaffold() {
        Text(
            "404", modifier = Modifier
                .fillMaxSize()
                .padding(it), textAlign = TextAlign.Center
        )
    }
}