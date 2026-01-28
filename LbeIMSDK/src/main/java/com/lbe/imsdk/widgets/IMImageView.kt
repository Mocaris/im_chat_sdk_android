package com.lbe.imsdk.widgets

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State
import coil3.decode.BitmapFactoryDecoder
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.crossfade
import com.lbe.imsdk.extension.coilDiskCache
import com.lbe.imsdk.extension.md5Str
import com.lbe.imsdk.service.http.interceptor.HttpProgressInterceptor
import com.lbe.imsdk.service.http.interceptor.SignInterceptor
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

/**
 *
 * @Date 2025-08-29
 */

@Composable
fun IMImageView(
    modifier: Modifier = Modifier,
    key: String? = null,
    url: String,
    // 是否加载缩率图
    @FloatRange(from = 0.0, to = 1.0)
    thumbnail: Float = 1f,
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
    val density = LocalDensity.current
    BoxWithConstraints(
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
                    .also { r ->
                        if (thumbnail > 0 && thumbnail < 1) {
                            with(density) {
                                r.size(maxWidth.times(thumbnail).roundToPx())
                            }
                        }
                    }
                    .crossfade(true)
                    .listener(listener)
                    .httpHeaders(
                        NetworkHeaders.Builder()
                            .add("lbeToken", SignInterceptor.lbeToken ?: "")
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
        if (progress.intValue in 1..<100) {
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
    ): Decoder {
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
//        return ImageLoader.Builder(options.context).build().components.newDecoder(
//            newResult,
//            options,
//            imageLoader
//        )?.first
        return BitmapFactoryDecoder(source = newResult.source, options = options)
    }

}