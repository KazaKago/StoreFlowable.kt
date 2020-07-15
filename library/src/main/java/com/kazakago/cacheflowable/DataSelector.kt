package com.kazakago.cacheflowable

internal class DataSelector<KEY, DATA>(
    private val key: KEY,
    private val dataStateManager: DataStateManager<KEY>,
    private val cacheDataManager: CacheDataManager<DATA>,
    private val originDataManager: OriginDataManager<DATA>,
    private val needRefresh: (suspend (data: DATA) -> Boolean)
) {

    suspend fun load(): DATA? {
        return cacheDataManager.load()
    }

    suspend fun update(newData: DATA?) {
        cacheDataManager.save(newData)
        dataStateManager.save(key, DataState.Fixed())
    }

    suspend fun doStateAction(forceRefresh: Boolean, clearCache: Boolean, fetchOnError: Boolean) {
        when (dataStateManager.load(key)) {
            is DataState.Fixed -> doDataAction(forceRefresh, clearCache)
            is DataState.Loading -> Unit
            is DataState.Error -> if (fetchOnError) fetchNewData(clearCache)
        }
    }

    private suspend fun doDataAction(forceRefresh: Boolean, clearCache: Boolean) {
        val data = cacheDataManager.load()
        if (data == null || forceRefresh || needRefresh(data)) {
            fetchNewData(clearCache)
        }
    }

    private suspend fun fetchNewData(clearCache: Boolean) {
        try {
            if (clearCache) cacheDataManager.save(null)
            dataStateManager.save(key, DataState.Loading)
            val fetchedData = originDataManager.fetch()
            cacheDataManager.save(fetchedData)
            dataStateManager.save(key, DataState.Fixed())
        } catch (exception: Exception) {
            dataStateManager.save(key, DataState.Error(exception))
        }
    }
}