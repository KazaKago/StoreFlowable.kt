package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.origin.InternalFetchingResult
import com.kazakago.storeflowable.origin.OriginDataManager

/**
 * Create [PaginatingStoreFlowable] class from [PaginatingStoreFlowableFactory].
 *
 * @return Created PaginatingStoreFlowable.
 */
fun <KEY, DATA> PaginatingStoreFlowableFactory<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache()
            override suspend fun save(newData: DATA?) = saveDataToCache(newData)
            override suspend fun saveAppending(cachedData: DATA?, newData: DATA) = saveAppendingDataToCache(cachedData, newData)
            override suspend fun savePrepending(cachedData: DATA?, newData: DATA) = throw NotImplementedError()
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetchingResult<DATA> {
                val result = fetchDataFromOrigin()
                return InternalFetchingResult(result.data, noMoreAppendingData = result.noMoreAdditionalData, noMorePrependingData = true)
            }

            override suspend fun fetchAppending(cachedData: DATA?): InternalFetchingResult<DATA> {
                val result = fetchAppendingDataFromOrigin(cachedData)
                return InternalFetchingResult(result.data, noMoreAppendingData = result.noMoreAdditionalData, noMorePrependingData = true)
            }

            override suspend fun fetchPrepending(cachedData: DATA?) = throw NotImplementedError()
        },
        needRefresh = { needRefresh(it) }
    )
}
