package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.logic.StoreFlowableImpl
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager

/**
 * Create [TwoWayStoreFlowable] class from [TwoWayStoreFlowableFactory].
 *
 * @return Created [TwoWayStoreFlowable].
 */
fun <KEY, DATA> TwoWayStoreFlowableFactory<KEY, DATA>.create(): TwoWayStoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache()
            override suspend fun save(newData: DATA?) = saveDataToCache(newData)
            override suspend fun saveNext(cachedData: DATA, newData: DATA) = saveNextDataToCache(cachedData, newData)
            override suspend fun savePrev(cachedData: DATA, newData: DATA) = savePrevDataToCache(cachedData, newData)
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetched<DATA> {
                val result = fetchDataFromOrigin()
                return InternalFetched(result.data, nextKey = result.nextKey, prevKey = result.prevKey)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<DATA> {
                val result = fetchNextDataFromOrigin(nextKey)
                return InternalFetched(result.data, nextKey = result.nextKey, prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String): InternalFetched<DATA> {
                val result = fetchPrevDataFromOrigin(prevKey)
                return InternalFetched(result.data, nextKey = null, prevKey = result.prevKey)
            }
        },
        needRefresh = { needRefresh(it) }
    )
}
