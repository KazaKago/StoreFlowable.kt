package com.kazakago.storeflowable

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class DataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: CacheDataManager<DATA>,
    private val originDataManager: OriginDataManager<DATA>,
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

    suspend fun doStateAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean, awaitFetching: Boolean) {
        when (dataStateManager.loadState(key)) {
            is DataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching)
            is DataState.Loading -> Unit
            is DataState.Error -> if (continueWhenError) doDataAction(forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching)
        }
    }

    private suspend fun doDataAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean) {
        val cachedData = cacheDataManager.loadDataFromCache()
        if (cachedData == null || forceRefresh || needRefresh(cachedData)) {
            prepareFetch(clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching)
        }
    }

    private suspend fun prepareFetch(clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean) {
        if (clearCacheBeforeFetching) cacheDataManager.saveDataToCache(null)
        dataStateManager.saveState(key, DataState.Loading())
        if (awaitFetching) {
            fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails)
        } else {
            CoroutineScope(defaultDispatcher).launch { fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails) }
        }
    }

    private suspend fun fetchNewData(clearCacheWhenFetchFails: Boolean) {
        try {
            val fetchingResult = originDataManager.fetchDataFromOrigin()
            cacheDataManager.saveDataToCache(fetchingResult.data)
            dataStateManager.saveState(key, DataState.Fixed())
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.saveDataToCache(null)
            dataStateManager.saveState(key, DataState.Error(exception))
        }
    }
}
