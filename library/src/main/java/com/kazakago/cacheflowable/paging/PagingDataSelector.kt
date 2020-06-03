package com.kazakago.cacheflowable.paging

import com.kazakago.cacheflowable.DataState
import com.kazakago.cacheflowable.DataStateManager

class PagingDataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: PagingCacheDataManager<DATA>,
    private val originDataManager: PagingOriginDataManager<DATA>,
    private val needRefresh: (suspend (data: List<DATA>) -> Boolean)
) {

    suspend fun load(): List<DATA>? {
        return cacheDataManager.load()
    }

    suspend fun update(newData: List<DATA>?, additionalRequest: Boolean = false) {
        val data = cacheDataManager.load()
        val mergedData = if (additionalRequest) (data ?: emptyList()) + (newData ?: emptyList()) else (newData ?: emptyList())
        cacheDataManager.save(mergedData, additionalRequest)
        val isReachLast = mergedData.isEmpty()
        dataStateManager.save(key, DataState.Fixed(isReachLast))
    }

    suspend fun doStateAction(forceRefresh: Boolean, clearCache: Boolean, fetchOnError: Boolean, additionalRequest: Boolean) {
        val state = dataStateManager.load(key)
        val data = cacheDataManager.load()
        when (state) {
            is DataState.Fixed -> doDataAction(data, forceRefresh, clearCache, additionalRequest, state.isReachLast)
            is DataState.Loading -> Unit
            is DataState.Error -> if (fetchOnError) fetchNewData(data, clearCache, additionalRequest)
        }
    }

    private suspend fun doDataAction(data: List<DATA>?, forceRefresh: Boolean, clearCache: Boolean, additionalRequest: Boolean, currentIsReachLast: Boolean) {
        if (data == null || forceRefresh || needRefresh(data) || (additionalRequest && !currentIsReachLast)) {
            fetchNewData(data, clearCache, additionalRequest)
        }
    }

    private suspend fun fetchNewData(data: List<DATA>?, clearCache: Boolean, additionalRequest: Boolean) {
        try {
            if (clearCache) cacheDataManager.save(null, additionalRequest)
            dataStateManager.save(key, DataState.Loading)
            val fetchedData = originDataManager.fetch(data, additionalRequest)
            val mergedData = if (additionalRequest) (data ?: emptyList()) + fetchedData else fetchedData
            cacheDataManager.save(mergedData, additionalRequest)
            val isReachLast = fetchedData.isEmpty()
            dataStateManager.save(key, DataState.Fixed(isReachLast))
        } catch (exception: Exception) {
            dataStateManager.save(key, DataState.Error(exception))
        }
    }

}
