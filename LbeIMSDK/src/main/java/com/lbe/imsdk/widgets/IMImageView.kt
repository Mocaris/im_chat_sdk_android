package com.lbe.imsdk.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.painter.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.Options
import com.lbe.imsdk.R
import com.lbe.imsdk.extension.coilDiskCache
import com.lbe.imsdk.extension.md5Str
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.service.http.interceptor.HttpProgressInterceptor
import kotlinx.coroutines.*
import okhttp3.*
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.*

/**
 *
 * @Date 2025-08-29
 */

@Composable
fun IMImageView(
    modifier: Modifier = Modifier,
    key: String? = null,
    url: String,
    placeholder: Painter? = null,
    error: Painter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    listener: ImageRequest.Listener? = null,
    onProgress: (Int) -> Unit = { }
) {
    val progress = remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        val imageLoader = remember {
            ImageLoader.Builder(context)
                .diskCache(coilDiskCache)
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = {
                        OkHttpClient.Builder()
                            .addNetworkInterceptor(HttpProgressInterceptor {
                                progress.intValue = it
                                onProgress(it)
                            })
                            .build()
                    }))
                }
                .build()
        }
        AsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(url)
                    .listener(listener)
                    .httpHeaders(
                        NetworkHeaders.Builder()
                            .add("lbeToken", LbeIMSDKManager.lbeToken ?: "")
                            .build()
                    )
                    .also {
                        if (!key.isNullOrEmpty()) {
                            it.decoderFactory(ImageDecoderFactory(key, url))
                        }
                    }
                    .decoderCoroutineContext(Dispatchers.IO)
                    .diskCacheKey(url.md5Str)
                    .build(),
            modifier = modifier,
            error = error,
            placeholder = placeholder,
            contentDescription = "",
            contentScale = contentScale,
            onLoading = onLoading,
            onSuccess = onSuccess,
            onError = onError,
            imageLoader = imageLoader
//        imageLoader = ImageLoader.Builder(LocalContext.current)
//            .components {
//                add(BitmapFactoryDecoder.Factory())
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    add(StaticImageDecoder.Factory())
//                }
//                add(VideoFrameDecoder.Factory())
//                add(GifDecoder.Factory())
//            }
//            .build(),
        )
        if (progress.intValue > 0 && progress.intValue < 100) {
//            IMLoadingIndicator(
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(30.dp)
//            )
            IMProgressLoadingIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp),
                progress = {
                    progress.intValue / 100f
                })
        }
    }

}

class ImageDecoderFactory(val key: String? = null, val url: String) : Decoder.Factory {
    override fun create(
        result: SourceFetchResult,
        options: Options,
        imageLoader: ImageLoader
    ): Decoder? {
        var newResult = result
        if (!key.isNullOrEmpty()) {
            val source = result.source
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val iv = IvParameterSpec(ByteArray(16))
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
            val respData = source.source().readByteArray()
            val decryptedData = cipher.doFinal(Base64.decode(respData))
            val bufferSource = ByteArrayInputStream(decryptedData).source().buffer()
            val decodeSource = ImageSource(bufferSource, source.fileSystem, source.metadata)
            newResult = SourceFetchResult(decodeSource, result.mimeType, result.dataSource)
        }
        return ImageLoader.Builder(options.context).build().components.newDecoder(
            newResult,
            options,
            imageLoader
        )?.first
//        return BitmapFactoryDecoder(source = newResult.source, options = options)
    }

}