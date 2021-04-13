package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.DataStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class PaginatingDataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: PaginatingCacheDataManager<DATA>,
    private val originDataManager: PaginatingOriginDataManager<DATA>,
    private val needRefresh: (suspend (cachedData: DATA) -> Boolean)
) {

    suspend fun load(): DATA? {
        return cacheDataManager.loadData()
    }

    suspend fun update(newData: DATA?) {
        cacheDataManager.saveData(newData)
        dataStateManager.saveState(key, DataState.Fixed())
    }

    suspend fun doStateAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean, awaitFetching: Boolean, additionalRequest: Boolean) {
        when (val state = dataStateManager.loadState(key)) {
            is DataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, additionalRequest = additionalRequest, noMoreAdditionalData = state.noMoreAdditionalData)
            is DataState.Loading -> Unit
            is DataState.Error -> if (continueWhenError) doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, additionalRequest = additionalRequest, noMoreAdditionalData = false)
        }
    }

    private suspend fun doDataAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, additionalRequest: Boolean, noMoreAdditionalData: Boolean) {
        val cachedData = cacheDataManager.loadData()
        if (cachedData == null || forceRefresh || needRefresh(cachedData) || (additionalRequest && !noMoreAdditionalData)) {
            prepareFetch(cachedData = cachedData, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, additionalRequest = additionalRequest)
        }
    }

    private suspend fun prepareFetch(cachedData: DATA?, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, additionalRequest: Boolean) {
        if (clearCacheBeforeFetching) cacheDataManager.saveData(null)
        dataStateManager.saveState(key, DataState.Loading())
        if (awaitFetching) {
            fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, additionalRequest = additionalRequest)
        } else {
            CoroutineScope(Dispatchers.IO).launch { fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, additionalRequest = additionalRequest) }
        }
    }

    private suspend fun fetchNewData(cachedData: DATA?, clearCacheWhenFetchFails: Boolean, additionalRequest: Boolean) {
        try {
            val fetchedResult = if (additionalRequest) {
                originDataManager.fetchAdditionalOrigin(cachedData)
            } else {
                originDataManager.fetchOrigin()
            }
            if (additionalRequest) {
                cacheDataManager.saveAdditionalData(cachedData, fetchedResult.data)
            } else {
                cacheDataManager.saveData(fetchedResult.data)
            }
            dataStateManager.saveState(key, DataState.Fixed(fetchedResult.noMoreAdditionalData))
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.saveData(null)
            dataStateManager.saveState(key, DataState.Error(exception))
        }
    }
}
