package com.lbe.imsdk.pages

import android.*
import android.os.*
import androidx.activity.compose.*
import androidx.activity.result.*
import androidx.activity.result.contract.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.lbe.imsdk.extension.toUriFile
import com.lbe.imsdk.pages.conversation.widgets.*
import kotlinx.coroutines.launch
import java.io.File

/**
 *
 * @Author mocaris
 * @Date 2025-08-22
 */


@Composable
fun TestPage() {
    val thumbnail = remember { mutableStateOf<File?>(null) }
    val scope = rememberCoroutineScope()
    val requestPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { it ->
            println("------------------->> $it")
            scope.launch {
                val uriFile = it?.toUriFile()
                val file = uriFile?.path
                thumbnail.value = uriFile?.thumbnailImage()?.let { File(it.path) }
                println("------------------->> ${file}")
            }
        },
    )
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { it ->
            if (it.any { !it.value }) {
                return@rememberLauncherForActivityResult
            }
            requestPhotoLauncher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo,
                )
            )
        },
    )

    fun pickPhoto() {
        requestPermissionLauncher.launch(
            mutableStateListOf<String>().apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    plus(Manifest.permission.READ_MEDIA_IMAGES)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        plus(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    }
                }
            }
                .toTypedArray(),
        )
    }

    val fieldValue = remember { mutableStateOf(TextFieldValue()) }
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = thumbnail.value,
                            modifier = Modifier
                                .align(alignment = Alignment.Center)
                                .size(200.dp),
                            contentDescription = ""
                        )
                    }
                }

            }
            KeyboardInputBox(
                value = fieldValue.value,
                onValueChange = {
                    fieldValue.value = it
                },
                onSend = {

                },
                onPickPhoto = { pickPhoto() }
            )
        }
    }
}