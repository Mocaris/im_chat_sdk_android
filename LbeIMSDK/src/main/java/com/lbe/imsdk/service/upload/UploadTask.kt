package com.lbe.imsdk.service.upload

import androidx.compose.runtime.mutableStateMapOf
import com.lbe.imsdk.extension.md5
import com.lbe.imsdk.extension.readCacheChunk
import com.lbe.imsdk.manager.LbeIMSDKManager
import com.lbe.imsdk.repository.remote.LbeOssApiRepository
import com.lbe.imsdk.repository.remote.api.params.BlockPart
import com.lbe.imsdk.repository.remote.model.SourceUrl
import com.lbe.imsdk.repository.remote.model.UploadBigFileResData
import com.lbe.imsdk.repository.remote.model.UploadNode
import com.lbe.imsdk.repository.remote.model.enumeration.UploadSignType
import com.lbe.imsdk.service.http.HttpClient
import com.lbe.imsdk.service.http.body.ProgressBody
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 *
 * @Author mocaris
 * @Date 2026-01-27
 * @Since
 */
//进度回调
typealias OnProgress = (Float) -> Unit

// 分片上传 完成 回调
typealias OnChunkUpload = (Map<Int, String>) -> Unit

enum class UploadState {
    IDE, PROCESS, SUCCESS, FAILED,
}

data class UploadResult(
    val localPath: String,
    val uploadUrl: SourceUrl,
)

abstract class UploadTask {
    abstract val sourceFile: File
    abstract val onProgress: OnProgress
    val state = MutableStateFlow<UploadState>(UploadState.IDE)

    @Throws(Exception::class)
    abstract suspend fun upload(): UploadResult

}

//单个文件上传
class SmallFileUploadTask(
    override val sourceFile: File,
    val signType: UploadSignType,
    override val onProgress: OnProgress
) : UploadTask() {
    override suspend fun upload(): UploadResult {
        try {
            if (state.value == UploadState.PROCESS) {
                throw IllegalStateException("上传任务正在处理中")
            }
            state.value = UploadState.PROCESS
            val apiRepository =
                LbeIMSDKManager.ossApiRepository
                    ?: throw IllegalStateException("api service 未初始化")
            val result = apiRepository.singleUpload(
                signType = signType,
                sourceFile,
                fileName = "${System.currentTimeMillis() / 1000}_${sourceFile.name}",
                listener = onProgress
            )
            val fileData = result.paths.first()
            state.value = UploadState.SUCCESS
            return UploadResult(
                localPath = sourceFile.path,
                uploadUrl = SourceUrl(fileData.key, fileData.url)
            )
        } catch (e: Exception) {
            state.value = UploadState.FAILED
            throw e
        }
    }
}

/// 大文件上传
class BigFileUploadTask(
    override val sourceFile: File,
    private val reUploadInfo: ReUploadInfo? = null,
    // 分片上传完成监听
    val onChunkUpload: OnChunkUpload? = null,
    val onInitChunkNodes: suspend (UploadBigFileResData.FileNodeData) -> Unit,
    override val onProgress: OnProgress
) : UploadTask() {

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

    private val semaphore = Semaphore(2)

    init {
        if (null != reUploadInfo) {
            nodeProgressCount = reUploadInfo.nodes.size.toFloat()
            uploadedChunk.putAll(reUploadInfo.uploadedChunk)
            chunkProgress.putAll(
                reUploadInfo.uploadedChunk.map {
                    it.key to 1f
                })
            onProgress((chunkProgress.values.sum() / nodeProgressCount).let { if (it.isNaN() || it.isInfinite()) 0f else it })
        }
    }

    override suspend fun upload(): UploadResult {
        try {
            if (state.value == UploadState.PROCESS) {
                throw IllegalStateException("任务正在处理中")
            }
            state.value = UploadState.PROCESS
            val apiRepository =
                LbeIMSDKManager.ossApiRepository
                    ?: throw IllegalStateException("api service 未初始化")
            updateResult.clear()

            var uploadId: String = ""
            var nodes: List<UploadNode> = emptyList()
            if (null != reUploadInfo) {
                uploadId = reUploadInfo.uploadId
                nodes = reUploadInfo.nodes
            }
            if (uploadId.isEmpty() || nodes.isEmpty()) {
                val chunkNodes = initChunkNodes(apiRepository)
//                val fileNode = onInitChunkNodes()
                uploadId = chunkNodes.uploadId
                nodes = chunkNodes.node
            }
            nodeProgressCount = nodes.size.toFloat()
            uploadChunkFile(nodes, apiRepository)
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
            return UploadResult(
                localPath = sourceFile.path,
                uploadUrl = SourceUrl("", resData.location),
            )
        } catch (e: Exception) {
            state.value = UploadState.FAILED
            throw e
        } finally {
            jobs.clear()
        }
    }

    ///上传分片文件
    private suspend fun uploadChunkFile(
        nodes: List<UploadNode>,
        apiRepository: LbeOssApiRepository
    ) = coroutineScope {
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
    }

    private suspend fun initChunkNodes(
        apiRepository: LbeOssApiRepository
    ): UploadBigFileResData.FileNodeData {
        return apiRepository.initMultiPartUpload(
            size = sourceFile.length(),
            name = sourceFile.name,
        ).also {
            onInitChunkNodes(it)
        }
    }

    private fun updateChunkState() {
        onChunkUpload?.invoke(uploadedChunk)
    }

    private fun updateNodeProgress(
        index: Int, p: Float
    ) {
        chunkProgress[index] = p
        onProgress((chunkProgress.values.sum() / nodeProgressCount).let { if (it.isNaN() || it.isInfinite()) 0f else it })
    }
}
