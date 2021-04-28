package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.DataStateManager
import kotlinx.coroutines.*

internal class PaginatingDataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: PaginatingCacheDataManager<DATA>,
    private val originDataManager: PaginatingOriginDataManager<DATA>,
    private val needRefresh: (suspend (cachedData: DATA) -> Boolean),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun load(): DATA? {
        return cacheDataManager.loadDataFromCache()
    }

    suspend fun update(newData: DATA?) {
        cacheDataManager.saveDataToCache(newData)
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
        val cachedData = cacheDataManager.loadDataFromCache()
        if (cachedData == null || forceRefresh || (!additionalRequest && needRefresh(cachedData)) || (additionalRequest && !noMoreAdditionalData)) {
            prepareFetch(cachedData = cachedData, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, additionalRequest = additionalRequest)
        }
    }

    private suspend fun prepareFetch(cachedData: DATA?, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, additionalRequest: Boolean) {
        if (clearCacheBeforeFetching) cacheDataManager.saveDataToCache(null)
        dataStateManager.saveState(key, DataState.Loading())
        if (awaitFetching) {
            fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, additionalRequest = additionalRequest)
        } else {
            CoroutineScope(defaultDispatcher).launch { fetchNewData(cachedData = cachedData, clearCacheWhenFetchFails = clearCacheWhenFetchFails, additionalRequest = additionalRequest) }
        }
    }

    private suspend fun fetchNewData(cachedData: DATA?, clearCacheWhenFetchFails: Boolean, additionalRequest: Boolean) {
        try {
            val fetchingResult = if (additionalRequest) {
                originDataManager.fetchAdditionalDataFromOrigin(cachedData)
            } else {
                originDataManager.fetchDataFromOrigin()
            }
            if (additionalRequest) {
                cacheDataManager.saveAdditionalDataToCache(cachedData, fetchingResult.data)
            } else {
                cacheDataManager.saveDataToCache(fetchingResult.data)
            }
            dataStateManager.saveState(key, DataState.Fixed(fetchingResult.noMoreAdditionalData))
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.saveDataToCache(null)
            dataStateManager.saveState(key, DataState.Error(exception))
        }
    }
}
