package com.kazakago.storeflowable

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.origin.InternalFetched
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
            override suspend fun saveNext(cachedData: DATA, newData: DATA) = throw NotImplementedError()
            override suspend fun savePrev(cachedData: DATA, newData: DATA) = throw NotImplementedError()
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetched<DATA> {
                val data = fetchDataFromOrigin()
                return InternalFetched(data = data, nextKey = null, prevKey = null)
            }

            override suspend fun fetchNext(nextKey: String) = throw NotImplementedError()
            override suspend fun fetchPrev(prevKey: String) = throw NotImplementedError()
        },
        needRefresh = { needRefresh(it) }
    )
}
