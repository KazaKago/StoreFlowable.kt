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

    suspend fun update(newData: List<DATA>?, additionalRequest: Boolean = false) {
        val data = cacheDataManager.loadData()
        val mergedData = if (additionalRequest) (data ?: emptyList()) + (newData ?: emptyList()) else (newData ?: emptyList())
        cacheDataManager.saveData(mergedData, additionalRequest)
        val isReachLast = mergedData.isEmpty()
        dataStateManager.saveState(key, DataState.Fixed(isReachLast))
    }

    suspend fun doStateAction(forceRefresh: Boolean, clearCache: Boolean, fetchAtError: Boolean, fetchAsync: Boolean, additionalRequest: Boolean) {
        val state = dataStateManager.loadState(key)
        val data = cacheDataManager.loadData()
        when (state) {
            is DataState.Fixed -> doDataAction(data = data, forceRefresh = forceRefresh, clearCache = clearCache, fetchAsync = fetchAsync, additionalRequest = additionalRequest, currentIsReachLast = state.isReachLast)
            is DataState.Loading -> Unit
            is DataState.Error -> if (fetchAtError) prepareFetch(data = data, clearCache = clearCache, fetchAsync = fetchAsync, additionalRequest = additionalRequest)
        }
    }

    private suspend fun doDataAction(data: List<DATA>?, forceRefresh: Boolean, clearCache: Boolean, fetchAsync: Boolean, additionalRequest: Boolean, currentIsReachLast: Boolean) {
        if (data == null || forceRefresh || needRefresh(data) || (additionalRequest && !currentIsReachLast)) {
            prepareFetch(data = data, clearCache = clearCache, fetchAsync = fetchAsync, additionalRequest = additionalRequest)
        }
    }

    private suspend fun prepareFetch(data: List<DATA>?, clearCache: Boolean, fetchAsync: Boolean, additionalRequest: Boolean) {
        if (clearCache) cacheDataManager.saveData(null, additionalRequest)
        dataStateManager.saveState(key, DataState.Loading())
        if (fetchAsync) {
            CoroutineScope(Dispatchers.IO).launch { fetchNewData(data = data, additionalRequest = additionalRequest) }
        } else {
            fetchNewData(data = data, additionalRequest = additionalRequest)
        }
    }

    private suspend fun fetchNewData(data: List<DATA>?, additionalRequest: Boolean) {
        try {
            val fetchedData = originDataManager.fetchOrigin(data, additionalRequest)
            val mergedData = if (additionalRequest) (data ?: emptyList()) + fetchedData else fetchedData
            cacheDataManager.saveData(mergedData, additionalRequest)
            val isReachLast = fetchedData.isEmpty()
            dataStateManager.saveState(key, DataState.Fixed(isReachLast))
        } catch (exception: Exception) {
            dataStateManager.saveState(key, DataState.Error(exception))
        }
    }
}
