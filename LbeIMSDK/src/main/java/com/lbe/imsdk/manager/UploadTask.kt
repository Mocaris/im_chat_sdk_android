package com.lbe.imsdk.manager

import androidx.compose.runtime.*
import com.lbe.imsdk.extension.md5
import com.lbe.imsdk.extension.readCacheChunk
import com.lbe.imsdk.extension.withIOContext
import com.lbe.imsdk.repository.remote.LbeOssApiRepository
import com.lbe.imsdk.repository.remote.api.params.BlockPart
import com.lbe.imsdk.repository.remote.model.UploadBigFileResData
import com.lbe.imsdk.repository.remote.model.UploadNode
import com.lbe.imsdk.repository.remote.model.enumeration.UploadSignType
import com.lbe.imsdk.service.http.HttpClient
import com.lbe.imsdk.service.http.body.ProgressBody
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 *
 * @Author mocaris
 * @Date 2025-09-09
 */

enum class UploadState {
    IDE, PROCESS, SUCCESS, FAILED,
}

abstract class UploadTask<T> {
    val state = mutableStateOf<UploadState>(UploadState.IDE)
    abstract suspend fun upload(): T

}


class UploadSingleFileTask(
    private val sourceFile: File,
    private val signType: UploadSignType,
    private val apiRepository: LbeOssApiRepository,
    val onProgress: (Float) -> Unit
) : UploadTask<Pair<String, String>>() {

    /**
     * 上传文件
     */
    override suspend fun upload(): Pair<String, String> = withIOContext {
        if (state.value == UploadState.PROCESS) {
            throw IllegalStateException("上传任务正在处理中")
        }
        val data = apiRepository.singleUpload(
            signType = signType,
            sourceFile,
            fileName = "${System.currentTimeMillis() / 1000}_${sourceFile.name}",
            listener = onProgress
        )!!
        val res = data.paths.first()
        return@withIOContext res.key to res.url
    }
}

class UploadBigFileTask(
    private val sourceFile: File,
    private val reUploadInfo: ReUploadInfo? = null,
    private val apiRepository: LbeOssApiRepository,
    // 分片上传完成监听
    val onUploadedChunk: ((Map<Int, String>) -> Unit)? = null,
    val onInitChunkNodes: suspend () -> UploadBigFileResData.FileNodeData,
    val onProgress: (Float) -> Unit
) : UploadTask<String>() {

    // 重新上传信息
    data class ReUploadInfo(
        val uploadId: String,
        // 已上传的分片 md5
        val uploadedChunk: Map<Int, String> = emptyMap(),
        // 分片节点
        val nodes: List<UploadNode> = emptyList(),
    )

    private var nodeProgressCount: Float = 0f

    //分片进度
    private val chunkProgress = mutableStateMapOf<Int, Float>()
    private val jobs = mutableListOf<Deferred<*>>()

    private val updateResult = mutableMapOf<Int, Boolean>()

    /// 已上传的分片 md5
    private val uploadedChunk = mutableMapOf<Int, String>()

    init {
        if (null != reUploadInfo) {
            nodeProgressCount = reUploadInfo.nodes.size.toFloat()
            uploadedChunk.putAll(reUploadInfo.uploadedChunk)
            chunkProgress.putAll(
                reUploadInfo.uploadedChunk.map {
                    it.key to 1f
                })
            onProgress(chunkProgress.values.sum() / nodeProgressCount)
        }
    }

    override suspend fun upload(): String = coroutineScope {
        if (state.value == UploadState.SUCCESS) {
            throw IllegalStateException("上传任务已结束")
        }
        if (state.value == UploadState.PROCESS) {
            throw IllegalStateException("上传任务正在处理中")
        }
        try {
            state.value = UploadState.PROCESS
            updateResult.clear()
            val semaphore = Semaphore(2)
            var uploadId: String = ""
            var nodes: List<UploadNode> = emptyList()
            if (null != reUploadInfo) {
                uploadId = reUploadInfo.uploadId
                nodes = reUploadInfo.nodes
            }
            if (uploadId.isEmpty() || nodes.isEmpty()) {
                val fileNode = onInitChunkNodes()
                uploadId = fileNode.uploadId
                nodes = fileNode.node
            }
            nodeProgressCount = nodes.size.toFloat()
            for (node in nodes.withIndex()) {
                //跳过已上传的分片
                val md5str = uploadedChunk[node.index]
                if (md5str != null && md5str.isNotEmpty()) {
                    chunkProgress[node.index] = 1f
                    updateResult[node.index] = true
                    continue
                }
                //继续重新上传未完成的分片
                jobs.add(async(Dispatchers.IO) {
                    semaphore.withPermit {
                        val uploadUrl = node.value.url
                        val blockSize = node.value.size
                        //上传分片 按 size 大小拆分
                        val skipSize = nodes.take(node.index).sumOf { it.size }
                        // 获取 当前分片的起始位置
                        val chunkFile = sourceFile.readCacheChunk(skipSize, blockSize)
                        try {
                            //计算 md5
                            val md5 = chunkFile.md5()
                            apiRepository.uploadBinary(
                                url = uploadUrl,
                                header = node.value.header,
                                requestBody = ProgressBody(
                                    chunkFile.asRequestBody(
                                        contentType = HttpClient.contentType_stream
                                    )
                                ) {
                                    updateNodeProgress(node.index, it)
                                })
                            uploadedChunk[node.index] = md5
                            updateResult[node.index] = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            updateResult[node.index] = false
                        } finally {
                            updateChunkState()
                            //上传完成 删除 缓存文件
                            chunkFile.delete()
                        }
                    }
                })
            }
            jobs.awaitAll()
            updateChunkState()
            if (!updateResult.values.let { it.isNotEmpty() && it.all { it } }) {
                throw Exception("分片上传失败")
            }
            val resData = apiRepository.completeMultiPartUpload(
                uploadId = uploadId,
                name = sourceFile.name,
                part = uploadedChunk.map { BlockPart(partNumber = it.key + 1, it.value) }.toList()
            )!!
            state.value = UploadState.SUCCESS
            return@coroutineScope resData.location
        } catch (e: Exception) {
            state.value = UploadState.FAILED
            throw e
        }
    }

    private fun updateChunkState() {
        onUploadedChunk?.invoke(uploadedChunk)
    }

    private fun updateNodeProgress(
        index: Int, p: Float
    ) {
        chunkProgress[index] = p
        onProgress(chunkProgress.values.sum() / nodeProgressCount)
    }
}