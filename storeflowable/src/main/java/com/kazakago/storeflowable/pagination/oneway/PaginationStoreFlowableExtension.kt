package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.logic.StoreFlowableImpl
import com.kazakago.storeflowable.origin.InternalFetched
import com.kazakago.storeflowable.origin.OriginDataManager

/**
 * Create [PaginationStoreFlowable] class from [PaginationStoreFlowableFactory].
 *
 * @return Created [PaginationStoreFlowable].
 */
fun <KEY, DATA> PaginationStoreFlowableFactory<KEY, DATA>.create(): PaginationStoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load() = loadDataFromCache()
            override suspend fun save(newData: DATA?) = saveDataToCache(newData)
            override suspend fun saveNext(cachedData: DATA, newData: DATA) = saveNextDataToCache(cachedData, newData)
            override suspend fun savePrev(cachedData: DATA, newData: DATA) = throw NotImplementedError()
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): InternalFetched<DATA> {
                val result = fetchDataFromOrigin()
                return InternalFetched(result.data, nextKey = result.nextKey, prevKey = null)
            }

            override suspend fun fetchNext(nextKey: String): InternalFetched<DATA> {
                val result = fetchNextDataFromOrigin(nextKey)
                return InternalFetched(result.data, nextKey = result.nextKey, prevKey = null)
            }

            override suspend fun fetchPrev(prevKey: String) = throw NotImplementedError()
        },
        needRefresh = { needRefresh(it) }
    )
}
