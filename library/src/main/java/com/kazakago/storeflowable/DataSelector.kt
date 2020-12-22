package com.kazakago.storeflowable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class DataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: CacheDataManager<DATA>,
    private val originDataManager: OriginDataManager<DATA>,
    private val needRefresh: (suspend (data: DATA) -> Boolean)
) {

    suspend fun load(): DATA? {
        return cacheDataManager.loadData()
    }

    suspend fun update(newData: DATA?) {
        cacheDataManager.saveData(newData)
        dataStateManager.saveState(key, DataState.Fixed())
    }

    suspend fun doStateAction(forceRefresh: Boolean, clearCache: Boolean, fetchAtError: Boolean, fetchAsync: Boolean) {
        when (dataStateManager.loadState(key)) {
            is DataState.Fixed -> doDataAction(forceRefresh = forceRefresh, clearCache = clearCache, fetchAsync = fetchAsync)
            is DataState.Loading -> Unit
            is DataState.Error -> if (fetchAtError) prepareFetch(clearCache = clearCache, fetchAsync = fetchAsync)
        }
    }

    private suspend fun doDataAction(forceRefresh: Boolean, clearCache: Boolean, fetchAsync: Boolean) {
        val data = cacheDataManager.loadData()
        if (data == null || forceRefresh || needRefresh(data)) {
            prepareFetch(clearCache = clearCache, fetchAsync = fetchAsync)
        }
    }

    private suspend fun prepareFetch(clearCache: Boolean, fetchAsync: Boolean) {
        if (clearCache) cacheDataManager.saveData(null)
        dataStateManager.saveState(key, DataState.Loading())
        if (fetchAsync) {
            CoroutineScope(Dispatchers.IO).launch { fetchNewData() }
        } else {
            fetchNewData()
        }
    }

    private suspend fun fetchNewData() {
        try {
            val fetchedData = originDataManager.fetchOrigin()
            cacheDataManager.saveData(fetchedData)
            dataStateManager.saveState(key, DataState.Fixed())
        } catch (exception: Exception) {
            dataStateManager.saveState(key, DataState.Error(exception))
        }
    }
}
