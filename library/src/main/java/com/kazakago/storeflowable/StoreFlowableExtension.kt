package com.kazakago.storeflowable

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.origin.InternalFetchingResult
import com.kazakago.storeflowable.origin.OriginDataManager

/**
 * Create [StoreFlowable] class from [StoreFlowableFactory].
 *
 * @return Created StateFlowable.
 */
fun <KEY, DATA> StoreFlowableFactory<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache()
            override suspend fun save(newData: DATA?) = saveDataToCache(newData)
            override suspend fun saveAppending(cachedData: DATA?, newData: DATA) = throw NotImplementedError()
            override suspend fun savePrepending(cachedData: DATA?, newData: DATA) = throw NotImplementedError()
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetchingResult<DATA> {
                val data = fetchDataFromOrigin()
                return InternalFetchingResult(data = data, noMoreAppendingData = false, noMorePrependingData = false)
            }

            override suspend fun fetchAppending(cachedData: DATA?) = throw NotImplementedError()
            override suspend fun fetchPrepending(cachedData: DATA?) = throw NotImplementedError()
        },
        needRefresh = { needRefresh(it) }
    )
}
