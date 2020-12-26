package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.DataState
import com.kazakago.storeflowable.DataStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class PagingDataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: PagingCacheDataManager<DATA>,
    private val originDataManager: PagingOriginDataManager<DATA>,
    private val needRefresh: (suspend (data: List<DATA>) -> Boolean)
) {

    suspend fun load(): List<DATA>? {
        return cacheDataManager.loadData()
    }

    suspend fun update(newData: List<DATA>?) {
        cacheDataManager.saveData(newData, false)
        dataStateManager.saveState(key, DataState.Fixed())
    }

    suspend fun doStateAction(forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean, awaitFetching: Boolean, additionalRequest: Boolean) {
        val state = dataStateManager.loadState(key)
        val data = cacheDataManager.loadData()
        when (state) {
            is DataState.Fixed -> doDataAction(data = data, forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, additionalRequest = additionalRequest, currentIsReachLast = state.isReachLast)
            is DataState.Loading -> Unit
            is DataState.Error -> if (continueWhenError) doDataAction(data = data, forceRefresh = forceRefresh, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, additionalRequest = additionalRequest, currentIsReachLast = false)
        }
    }

    private suspend fun doDataAction(data: List<DATA>?, forceRefresh: Boolean, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, additionalRequest: Boolean, currentIsReachLast: Boolean) {
        if (data == null || forceRefresh || needRefresh(data) || (additionalRequest && !currentIsReachLast)) {
            prepareFetch(data = data, clearCacheBeforeFetching = clearCacheBeforeFetching, clearCacheWhenFetchFails = clearCacheWhenFetchFails, awaitFetching = awaitFetching, additionalRequest = additionalRequest)
        }
    }

    private suspend fun prepareFetch(data: List<DATA>?, clearCacheBeforeFetching: Boolean, clearCacheWhenFetchFails: Boolean, awaitFetching: Boolean, additionalRequest: Boolean) {
        if (clearCacheBeforeFetching) cacheDataManager.saveData(null, additionalRequest)
        dataStateManager.saveState(key, DataState.Loading())
        if (awaitFetching) {
            fetchNewData(data = data, clearCacheWhenFetchFails = clearCacheWhenFetchFails, additionalRequest = additionalRequest)
        } else {
            CoroutineScope(Dispatchers.IO).launch { fetchNewData(data = data, clearCacheWhenFetchFails = clearCacheWhenFetchFails, additionalRequest = additionalRequest) }
        }
    }

    private suspend fun fetchNewData(data: List<DATA>?, clearCacheWhenFetchFails: Boolean, additionalRequest: Boolean) {
        try {
            val fetchedData = originDataManager.fetchOrigin(data, additionalRequest)
            val mergedData = if (additionalRequest) (data ?: emptyList()) + fetchedData else fetchedData
            cacheDataManager.saveData(mergedData, additionalRequest)
            val isReachLast = fetchedData.isEmpty()
            dataStateManager.saveState(key, DataState.Fixed(isReachLast))
        } catch (exception: Exception) {
            if (clearCacheWhenFetchFails) cacheDataManager.saveData(null, additionalRequest)
            dataStateManager.saveState(key, DataState.Error(exception))
        }
    }
}
