package com.lbe.imsdk.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lbe.imsdk.repository.remote.model.IMMsgModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
 *
 * @Date 2025-08-19
 */
class MessagePagingSource() : PagingSource<Int, IMMsgModel>() {
    override fun getRefreshKey(state: PagingState<Int, IMMsgModel>): Int? {

        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, IMMsgModel> =
        withContext(Dispatchers.IO) {
            LoadResult.Page(
                data = emptyList(),
                prevKey = 0,
                nextKey = null,
            )
        }
}