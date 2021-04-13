package com.kazakago.storeflowable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class DataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: CacheDataManager<DATA>,
    private val originDataManager: OriginDataManager<DATA>,
    private val needRefresh: (suspend (cachedData: DATA) -> Boolean)
) {

    suspend fun load(): DATA? {
        return cacheDataManager.loadData()
    }

    suspend fun update(newData: DATA?) {
        cacheDataManager.saveData(newData)
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
        val cachedData = cacheDataManager.loadData()
        if (cachedData == null || forceRefresh || needRefresh(cachedData)) {
            prepareFetch(clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching)
        }
    }

    private suspend fun prepareFetch(clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean) {
        if (clearCacheBeforeFetching) cacheDataManager.saveData(null)
        dataStateManager.saveState(key, DataState.Loading())
        if (awaitFetching) {
            fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails)
        } else {
            CoroutineScope(Dispatchers.IO).launch { fetchNewData(clearCacheWhenFetchFails = clearCacheWhenFetchFails) }
        }
    }

    private suspend fun fetchNewData(clearCacheWhenFetchFails: Boolean) {
        try {
            val fetchingResult = originDataManager.fetchOrigin()
            cacheDataManager.saveData(fetchingResult.data)
            dataStateManager.saveState(key, DataState.Fixed())
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.saveData(null)
            dataStateManager.saveState(key, DataState.Error(exception))
        }
    }
}
