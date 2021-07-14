package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.origin.InternalFetchingResult
import com.kazakago.storeflowable.origin.OriginDataManager

/**
 * Create [OneWayStoreFlowable] class from [OneWayStoreFlowableFactory].
 *
 * @return Created [OneWayStoreFlowable].
 */
fun <KEY, DATA> OneWayStoreFlowableFactory<KEY, DATA>.create(): OneWayStoreFlowable<KEY, DATA> {
    return OneWayStoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache()
            override suspend fun save(newData: DATA?) = saveDataToCache(newData)
            override suspend fun saveNext(cachedData: DATA?, newData: DATA) = saveNextDataToCache(cachedData, newData)
            override suspend fun savePrev(cachedData: DATA?, newData: DATA) = throw NotImplementedError()
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetchingResult<DATA> {
                val result = fetchDataFromOrigin()
                return InternalFetchingResult(result.data, nextKey = result.nextKey, prevKey = null)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetchingResult<DATA> {
                val result = fetchNextDataFromOrigin(nextKey)
                return InternalFetchingResult(result.data, nextKey = result.nextKey, prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String) = throw NotImplementedError()
        },
        needRefresh = { needRefresh(it) }
    )
}