package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.origin.InternalFetchingResult
import com.kazakago.storeflowable.origin.OriginDataManager

/**
 * Create [TwoWayPaginatingStoreFlowable] class from [TwoWayPaginatingStoreFlowableFactory].
 *
 * @return Created TwoWayPaginatingStoreFlowable.
 */
fun <KEY, DATA> TwoWayPaginatingStoreFlowableFactory<KEY, DATA>.create(): TwoWayPaginatingStoreFlowable<KEY, DATA> {
    return TwoWayPaginatingStoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache()
            override suspend fun save(newData: DATA?) = saveDataToCache(newData)
            override suspend fun saveAppending(cachedData: DATA?, newData: DATA) = saveAppendingDataToCache(cachedData, newData)
            override suspend fun savePrepending(cachedData: DATA?, newData: DATA) = savePrependingDataToCache(cachedData, newData)
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetchingResult<DATA> {
                val result = fetchDataFromOrigin()
                return InternalFetchingResult(result.data, noMoreAppendingData = result.noMoreAppendingData, noMorePrependingData = result.noMorePrependingData)
            }

            override suspend fun fetchAppending(cachedData: DATA?): InternalFetchingResult<DATA> {
                val result = fetchAppendingDataFromOrigin(cachedData)
                return InternalFetchingResult(result.data, noMoreAppendingData = result.noMoreAdditionalData, noMorePrependingData = false)
            }

            override suspend fun fetchPrepending(cachedData: DATA?): InternalFetchingResult<DATA> {
                val result = fetchPrependingDataFromOrigin(cachedData)
                return InternalFetchingResult(result.data, noMoreAppendingData = false, noMorePrependingData = result.noMoreAdditionalData)
            }
        },
        needRefresh = { needRefresh(it) }
    )
}
